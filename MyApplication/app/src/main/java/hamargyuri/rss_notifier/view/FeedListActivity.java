package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.Date;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.adapter.FeedAdapter;
import hamargyuri.rss_notifier.model.FeedDao;
import hamargyuri.rss_notifier.model.RSSItem;
import hamargyuri.rss_notifier.model.RSSChannel;
import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import hamargyuri.rss_notifier.service.NewFeedNotifierService;
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

    private boolean isScrolling = false;

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

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        listView.getChildCount() == 0 ? 0 : listView.getChildAt(0).getTop();
//                feedSwipeRefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                if (isScrolling()) {
                    feedSwipeRefresh.setEnabled(false);
                } else {
                    feedSwipeRefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
                }
            }
        });

    }

    public void setScrolling(boolean scrolling) {
        isScrolling = scrolling;
    }

    public boolean isScrolling() {
        return isScrolling;
    }

    public SwipeRefreshLayout getFeedSwipeRefresh() {
        return feedSwipeRefresh;
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
        adapter.updateFeeds(getAllFeeds(), listView);
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
        session.clear();
        return feeds;
    }
}
