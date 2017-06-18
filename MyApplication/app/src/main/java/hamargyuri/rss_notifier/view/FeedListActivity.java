package hamargyuri.rss_notifier.view;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Date;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.model.FeedAdapter;
import hamargyuri.rss_notifier.model.FeedDao;
import hamargyuri.rss_notifier.model.RSSItem;
import hamargyuri.rss_notifier.model.RSSChannel;
import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static hamargyuri.rss_notifier.RSSNotifierApp.TEMP_RSS_TITLE;

public class FeedListActivity extends AppCompatActivity {
    private SwipeRefreshLayout feedSwipeRefresh;
    private DaoSession session = RSSNotifierApp.getSession();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);
        fetchAndRefreshFeed();
        feedSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.feed_swipe_refresh);
        feedSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAndRefreshFeed();
            }
        });

        ArrayList<Feed> feeds = getAllFeeds();
        for (int i = 0; i < 20; i++) {
            feeds.add(feeds.get(0));
            //TODO: remove when multiple feeds can be saved in database
        }

        final ListView listView = (ListView) findViewById(R.id.feed_list);
        listView.setAdapter(new FeedAdapter(this,R.id.feed_list,feeds));

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (listView == null || listView.getChildCount() == 0) ?
                                0 : listView.getChildAt(0).getTop();
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.feed_swipe_refresh);
                swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
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
            feed.setTitle(TEMP_RSS_TITLE);
        }
        feed.setUrl(feedUrl);
        feedDao.save(feed);
        session.clear();

        Toast.makeText(this, "feed saved", Toast.LENGTH_LONG).show();
        fetchAndRefreshFeed();
    }

    private void refreshLatestFeedItem(RSSItem rssItem) {
        TextView date = (TextView) findViewById(R.id.latest_feed_date);
        Date latestItemDate = rssItem.getParsedDate();

        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(FeedDao.Properties.Title.eq(TEMP_RSS_TITLE)).unique();
        Date previousDate = feed.getLatestItemDate();

        if (previousDate == null || latestItemDate.after(previousDate)) {
            feed.setLatestItemDate(latestItemDate);
            feedDao.save(feed);
            sendNotification(feed.getLatestItemDate().toString());
        }

//        date.setText(latestItemDate.toString());
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
                RSSFeed rssFeed = response.body();
                if (rssFeed == null) {
                    Log.d("fetching body", "body is null");
                    return;
                }
                RSSChannel channel = rssFeed.getChannel();
                if (channel == null) {
                    Log.d("fetching channel", "channel is null");
                    return;
                }
                RSSItem latestItem = channel.getItems().get(0);
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

    //TODO: dynamic, updatable, etc.
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

    public ArrayList<Feed> getAllFeeds(){
        FeedDao feedDao = session.getFeedDao();
        ArrayList<Feed> feeds = new ArrayList<Feed>(feedDao.loadAll());
        session.clear();
        return feeds;
    }
}
