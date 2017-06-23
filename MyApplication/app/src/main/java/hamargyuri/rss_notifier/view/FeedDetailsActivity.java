package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.model.FeedDao;

import static hamargyuri.rss_notifier.model.FeedDao.Properties.Title;

public class FeedDetailsActivity extends AppCompatActivity{
    private DaoSession session = RSSNotifierApp.getSession();
    private boolean isNewEntry = true;
    private Feed mFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_new_feed);
        setContentView(R.layout.activity_feed_details);
        mFeed = getIntent().getParcelableExtra("feed");

        if (mFeed != null) {
            setTitle(R.string.title_edit_feed);
            isNewEntry = false;
            EditText title = (EditText) findViewById(R.id.input_feed_title);
            EditText url = (EditText) findViewById(R.id.input_feed_url);
            EditText notification = (EditText) findViewById(R.id.input_notification);
            title.setText(mFeed.getTitle());
            url.setText(mFeed.getUrl());
            notification.setText(mFeed.getNotificationTitle());
        }
    }

    public void addOrUpdateFeed(View view) {
        EditText feedTitleEdit = (EditText) findViewById(R.id.input_feed_title);
        EditText feedUrlEdit = (EditText) findViewById(R.id.input_feed_url);
        EditText notificationTitleEdit = (EditText) findViewById(R.id.input_notification);

        String feedTitle = feedTitleEdit.getText().toString();
        String feedUrl = feedUrlEdit.getText().toString();
        String notificationTitle = notificationTitleEdit.getText().toString();

        if (feedTitle.isEmpty() || feedUrl.isEmpty() || notificationTitle.isEmpty()) {
            Toast.makeText(this, "Please fill in every data", Toast.LENGTH_LONG).show();
            return;
        }

        saveFeedInDb(feedTitle, feedUrl, notificationTitle);
    }

    public void launchFeedListActivity() {
        Intent intent = new Intent(this, FeedListActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveFeedInDb(String feedTitle, String feedUrl, String notificationTitle) {
        FeedDao feedDao = session.getFeedDao();

        if (isNewEntry) {
            mFeed = handleNewFeed(feedDao, feedTitle);
        }

        if (mFeed == null) return;

        mFeed.setTitle(feedTitle);
        mFeed.setUrl(feedUrl);
        mFeed.setNotificationTitle(notificationTitle);

        feedDao.save(mFeed);
        Toast.makeText(this, "feed saved successfully", Toast.LENGTH_LONG).show();
        session.clear();

        launchFeedListActivity();
    }

    private Feed handleNewFeed(FeedDao feedDao, String feedTitle) {
        if (feedDao.queryBuilder().where(Title.eq(feedTitle)).unique() != null) {
            Toast.makeText(this, "Title already in use", Toast.LENGTH_LONG).show();
            return null;
        }

        return new Feed();
    }
}
