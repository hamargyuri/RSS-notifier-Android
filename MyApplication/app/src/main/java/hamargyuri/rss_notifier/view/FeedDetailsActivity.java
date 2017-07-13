package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import be.appfoundry.progressbutton.ProgressButton;
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
    private final float longClickTime = 1000.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_new_feed);
        setContentView(R.layout.activity_feed_details);
        mFeed = getIntent().getParcelableExtra("feed");

        Switch notificationSwitch = (Switch) findViewById(R.id.notification_switch);

        if (mFeed != null) {
            setTitle(R.string.title_edit_feed);
            isNewEntry = false;
            EditText title = (EditText) findViewById(R.id.input_feed_title);
            EditText url = (EditText) findViewById(R.id.input_feed_url);
            EditText notification = (EditText) findViewById(R.id.input_notification);
            title.setText(mFeed.getTitle());
            url.setText(mFeed.getUrl());
            notification.setText(mFeed.getNotificationTitle());
            notificationSwitch.setChecked(mFeed.getNotificationEnabled());

            prepareDeleteButton();
        }
    }

    public void prepareDeleteButton() {
        final ProgressButton deleteButton = (ProgressButton) findViewById(R.id.progress_button);
        deleteButton.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        deleteButton.setProgressColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        deleteButton.setStrokeColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        deleteButton.setStrokeWidth(8);
        deleteButton.setIndeterminate(true);
        deleteButton.setAnimationDelay(0);
        deleteButton.setStartDegrees(270);
        deleteButton.setRadius(72);
        deleteButton.setIcon(getDrawable(R.drawable.ic_delete_white_24dp));
        deleteButton.setMaxProgress(longClickTime);
        deleteButton.setVisibility(View.VISIBLE);
//        TODO: find a fix for setting up ProgressButton in the XML layout file

        deleteButton.setOnTouchListener(new View.OnTouchListener() {
            boolean run;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Timer timer = new Timer();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        deleteButton.startAnimating();
                        run = true;
                        TimerTask timerTask = new TimerTask() {
                            public void run() {
                                while (System.currentTimeMillis() - scheduledExecutionTime() <= longClickTime && run) {
                                    final float current = System.currentTimeMillis() - scheduledExecutionTime();
                                    if (current < longClickTime) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                deleteButton.setProgress(current);
                                            }
                                        });
                                    }
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (run) {
                                            deleteFeed(mFeed);
                                            launchFeedListActivity();
                                        }
                                        deleteButton.setProgress(0);
                                        deleteButton.stopAnimating();
                                    }
                                });
                                this.cancel();
                            }
                        };
                        timer.schedule(timerTask, 0, (int) longClickTime);
                        deleteButton.stopAnimating();
                        break;
                    case MotionEvent.ACTION_UP:
                        run = false;
                        break;
                }
                return true;
            }
        });
    }

    public void deleteFeed(Feed feed) {
        FeedDao feedDao = session.getFeedDao();
        feedDao.delete(feed);
        Toast.makeText(this, "Feed deleted", Toast.LENGTH_LONG).show();
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

        Switch notificationSwitch = (Switch) findViewById(R.id.notification_switch);
        boolean notificationEnabled = notificationSwitch.isChecked();

        saveFeedInDb(feedTitle, feedUrl, notificationTitle, notificationEnabled);
    }

    public void launchFeedListActivity() {
        Intent intent = new Intent(this, FeedListActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveFeedInDb(String feedTitle, String feedUrl, String notificationTitle, boolean notificationEnabled) {
        FeedDao feedDao = session.getFeedDao();

        if (isNewEntry) {
            mFeed = handleNewFeed(feedDao, feedTitle);
        }

        if (mFeed == null) return;

        mFeed.setTitle(feedTitle);
        mFeed.setUrl(feedUrl);
        mFeed.setNotificationTitle(notificationTitle);
        mFeed.setNotificationEnabled(notificationEnabled);

        feedDao.save(mFeed);
        Toast.makeText(this, "feed saved successfully", Toast.LENGTH_LONG).show();
        session.clear();

        launchFeedListActivity();
    }

    private int getNumberOfFeeds() {
        FeedDao feedDao = session.getFeedDao();
        ArrayList<Feed> feeds = new ArrayList<>(feedDao.loadAll());
        return feeds.size();
    }

    private Feed handleNewFeed(FeedDao feedDao, String feedTitle) {
        if (feedDao.queryBuilder().where(Title.eq(feedTitle)).unique() != null) {
            Toast.makeText(this, "Title already in use", Toast.LENGTH_LONG).show();
            return null;
        }
        Feed feed = new Feed();
        feed.setPosition(getNumberOfFeeds() + 1);
        feed.setRefreshIntervalInMinutes(5);
        return feed;

    }
}
