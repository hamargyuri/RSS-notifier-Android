package hamargyuri.rss_notifier.view;

import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.greendao.database.Database;

import java.util.Date;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.model.DaoMaster;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.model.FeedDao;
import hamargyuri.rss_notifier.model.FeedItem;
import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedListActivity extends AppCompatActivity {
    private static final String TEMP_RSS_TITLE = "upwork"; //TODO: remove
    private static final String DATABASE = "RSS feeds";
    private SwipeRefreshLayout feedSwipeRefresh;
    private DaoSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE);
        Database db = helper.getWritableDb();
        session = new DaoMaster(db).newSession();
        fetchAndRefreshFeed();
        feedSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.feed_swipe_refresh);
        feedSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAndRefreshFeed();
            }
        });
    }

    public void saveFeed(View view) {
        EditText feedUrlInput = (EditText) findViewById(R.id.feedUrl);
        final String feedUrl = feedUrlInput.getText().toString();

        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(FeedDao.Properties.Title.eq(TEMP_RSS_TITLE)).unique();
        if (feed == null) {
            feed = new Feed();
            feed.setTitle("upwork");
        }
        feed.setUrl(feedUrl);
        feedDao.save(feed);
        session.clear();

        Toast.makeText(this, "feed saved", Toast.LENGTH_LONG).show();
        fetchAndRefreshFeed();
    }

    private void refreshLatestFeedItem(FeedItem feedItem) {
        TextView date = (TextView) findViewById(R.id.latest_feed_date);
        Date latestItemDate = feedItem.getParsedDate();

        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(FeedDao.Properties.Title.eq(TEMP_RSS_TITLE)).unique();
        Date previousDate = feed.getLatestItemDate();

        if (previousDate == null || latestItemDate.after(previousDate)) {
            feed.setLatestItemDate(latestItemDate);
            feedDao.save(feed);
            sendNotification(feed.getLatestItemDate().toString());
        }

        date.setText(latestItemDate.toString());
        session.clear();
        feedSwipeRefresh.setRefreshing(false);
    }

    private void fetchAndRefreshFeed() {
        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(FeedDao.Properties.Title.eq(TEMP_RSS_TITLE)).unique();
        if (feed == null) {
            return;
        }

        String url = feed.getUrl();
        if (!url.startsWith("http")) url = "https://" + url;
        session.clear();

        Call<RSSFeed> call = RSSFactory.create().getFeed(url);
        Callback<RSSFeed> callback = new Callback<RSSFeed>() {
            @Override
            public void onResponse(Call<RSSFeed> call, Response<RSSFeed> response) {
                FeedItem latestItem = response.body().getChannel().getItems().get(0);
                refreshLatestFeedItem(latestItem);
            }

            @Override
            public void onFailure(Call<RSSFeed> call, Throwable t) {
                Log.d("callback failure", t.getMessage());
                Toast.makeText(FeedListActivity.this, "callback failure", Toast.LENGTH_LONG).show();
            }
        };
        call.enqueue(callback);
    }

    public void sendNotification(String jobDate) {
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(this)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.uw_rss_icon)
                        .setContentTitle("New job on Upwork!")
                        .setContentText(jobDate);

        int notificationId = 1;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
