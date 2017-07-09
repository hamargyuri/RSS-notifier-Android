package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.listener.MyOnScrollListener;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.adapter.FeedAdapter;
import hamargyuri.rss_notifier.model.FeedDao;
import hamargyuri.rss_notifier.model.RSSItem;
import hamargyuri.rss_notifier.model.RSSChannel;
import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static hamargyuri.rss_notifier.model.FeedDao.Properties.Title;

public class FeedListActivity extends AppCompatActivity {


    private SwipeRefreshLayout feedSwipeRefresh;
    private DaoSession session = RSSNotifierApp.getSession();
    private FeedAdapter adapter;
    private DynamicListView listView;
    private ArrayList<Feed> feedList;
    private MyOnScrollListener listener;
    private boolean disableSwipeRefresh = false;

    public void setDisableSwipeRefresh(boolean disableSwipeRefresh) {
        this.disableSwipeRefresh = disableSwipeRefresh;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedList = getAllFeeds();
        setContentView(R.layout.activity_feed_list);
        setTitle(R.string.title_feed_list);
        fetchAndRefreshFeeds();
        feedSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.feed_swipe_refresh);
        feedSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchAndRefreshFeeds();
            }
        });
        adapter = new FeedAdapter(this,R.id.feed_list,feedList);

        listView = (DynamicListView) findViewById(R.id.feed_list);
        listView.setFeedList(feedList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listener = new MyOnScrollListener();
        listener.setListView(listView);
        listener.setFeedSwipeRefresh(feedSwipeRefresh);
        listView.setOnScrollListener(listener);
    }

    public void toggleSwipeRefresh(boolean toggle) {
        Log.d("TAG", "onScroll: TOGGLED: " + toggle);
        feedSwipeRefresh.setEnabled(toggle);
        listener.setDisableSwipeRefresh(!toggle);
    }

    public void addNewFeed(View view){
        Intent intent = new Intent(this, FeedDetailsActivity.class);
        startActivity(intent);
    }

    private void refreshLatestFeedItem(RSSItem rssItem, String title, boolean sendNotification) {
        Date latestItemDate = rssItem.getParsedDate();

        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(Title.eq(title)).unique();
        Date previousDate = feed.getLatestItemDate();

        if (previousDate == null || latestItemDate.after(previousDate)) {
            feed.setLatestItemDate(latestItemDate);
            feedDao.save(feed);
        }
        session.clear();
        adapter.updateFeeds(getAllFeeds());
        feedSwipeRefresh.setRefreshing(false);
    }

    private void fetchAndRefreshFeeds() {
        ArrayList<Feed> feeds = getAllFeeds();
        if (feeds == null) {
            return;
        }
        for (Feed feed : feeds) {
            fetch(feed, feed.getTitle());
        }
    }

    public void fetch(final Feed feed, final String title) {
        String url = feed.getUrl();
        if (!url.startsWith("http")) url = "https://" + url;

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
                refreshLatestFeedItem(latestItem, title, feed.getNotificationEnabled());
            }

            @Override
            public void onFailure(Call<RSSFeed> call, Throwable t) {
                Log.d("callback failure", t.getMessage());
            }
        };
        call.enqueue(callback);
    }

    public ArrayList<Feed> getAllFeeds(){
        FeedDao feedDao = session.getFeedDao();
        ArrayList<Feed> feeds = new ArrayList<>(feedDao.loadAll());
//        for (int i = 0; i < feeds.size(); i++) {
//            Log.d("TAG", "getAllFeeds: " + feeds.get(i).getTitle() + feeds.get(i).getPosition());
//        }
        Collections.sort(feeds, new Comparator<Feed>() {
            @Override
            public int compare(Feed o1, Feed o2) {
                return Integer.compare(o1.getPosition(), o2.getPosition());
            }
        });

//        for (int i = 0; i < feeds.size(); i++) {
//            Log.d("TAG", "getAllFeeds: " + feeds.get(i).getTitle() + feeds.get(i).getPosition());
//        }
        session.clear();
        return feeds;
    }
}
