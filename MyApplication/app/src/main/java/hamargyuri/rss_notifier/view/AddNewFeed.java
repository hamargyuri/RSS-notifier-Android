package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.model.FeedDao;

import static hamargyuri.rss_notifier.RSSNotifierApp.TEMP_RSS_TITLE;

public class AddNewFeed extends AppCompatActivity{
    private DaoSession session = RSSNotifierApp.getSession();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_feed);
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
        feedListActivity(findViewById(android.R.id.content));
    }

    public void feedListActivity(View view){
        Intent intent = new Intent(this, FeedListActivity.class);
        startActivity(intent);
    }
}
