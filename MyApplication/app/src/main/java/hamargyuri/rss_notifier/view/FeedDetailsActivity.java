package hamargyuri.rss_notifier.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
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
import static hamargyuri.rss_notifier.model.FeedDao.Properties.Id;

public class FeedDetailsActivity extends AppCompatActivity{
    private DaoSession session = RSSNotifierApp.getSession();
    private boolean isNewEntry = true;
    private Feed mFeed;
    private boolean editMode = true;
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

            swapToViewMode();
            prepareDeleteButton();
            notificationSwitch.setChecked(mFeed.getNotificationEnabled());
            notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mFeed.setNotificationEnabled(true);
                    } else {
                        mFeed.setNotificationEnabled(false);
                    }
                    FeedDao feedDao = session.getFeedDao();
                    feedDao.save(mFeed);
                }
            });
        }
    }

    private void swapToViewMode() {
        editMode = false;
        setTitle(R.string.title_view_feed);
        EditText editTextTitle = (EditText) findViewById(R.id.input_feed_title);
        EditText editTextUrl = (EditText) findViewById(R.id.input_feed_url);
        EditText editTextNotification = (EditText) findViewById(R.id.input_notification_title);
        editTextTitle.setVisibility(View.GONE);
        editTextUrl.setVisibility(View.GONE);
        editTextNotification.setVisibility(View.GONE);

        TextView textViewTitle = (TextView) findViewById(R.id.read_feed_title);
        TextView textViewUrl = (TextView) findViewById(R.id.read_feed_url);
        TextView textViewNotification = (TextView) findViewById(R.id.read_notification_title);
        textViewTitle.setText(mFeed.getTitle());
        textViewUrl.setText(mFeed.getUrl());
        textViewNotification.setText(mFeed.getNotificationTitle());
        textViewTitle.setVisibility(View.VISIBLE);
        textViewUrl.setVisibility(View.VISIBLE);
        textViewNotification.setVisibility(View.VISIBLE);

        Button button = (Button) findViewById(R.id.save_or_edit_button);
        button.setText(R.string.button_edit_feed);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swapToEditMode();
            }
        });
    }

    private void swapToEditMode() {
        editMode = true;
        setTitle(R.string.title_edit_feed);
        TextView textViewTitle = (TextView) findViewById(R.id.read_feed_title);
        TextView textViewUrl = (TextView) findViewById(R.id.read_feed_url);
        TextView textViewNotification = (TextView) findViewById(R.id.read_notification_title);
        textViewTitle.setVisibility(View.GONE);
        textViewUrl.setVisibility(View.GONE);
        textViewNotification.setVisibility(View.GONE);


        final EditText editTextTitle = (EditText) findViewById(R.id.input_feed_title);
        EditText editTextUrl = (EditText) findViewById(R.id.input_feed_url);
        EditText editTextNotification = (EditText) findViewById(R.id.input_notification_title);
        editTextTitle.setText(mFeed.getTitle());
        editTextUrl.setText(mFeed.getUrl());
        editTextNotification.setText(mFeed.getNotificationTitle());
        editTextTitle.setVisibility(View.VISIBLE);
        editTextUrl.setVisibility(View.VISIBLE);
        editTextNotification.setVisibility(View.VISIBLE);


        Button button = (Button) findViewById(R.id.save_or_edit_button);
        button.setText(R.string.button_save_feed);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!feedTitleAlreadyInUse(mFeed.getId(),editTextTitle.getText().toString())) {
                    addOrUpdateFeed(v);
                }
            }
        });
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
        EditText notificationTitleEdit = (EditText) findViewById(R.id.input_notification_title);

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
            mFeed = handleNewFeed(feedTitle);
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

    private Feed handleNewFeed(String feedTitle) {
        if (feedTitleAlreadyInUse(feedTitle)) {
            return null;
        }
        Feed feed = new Feed();
        feed.setPosition(getNumberOfFeeds() + 1);
        return feed;
    }

    private boolean feedTitleAlreadyInUse(String feedTitle) {
        FeedDao feedDao = session.getFeedDao();
        if (feedDao.queryBuilder().where(Title.eq(feedTitle)).unique() != null) {
            Toast.makeText(this, "Title already in use", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private boolean feedTitleAlreadyInUse(long id, String title) {
        FeedDao feedDao = session.getFeedDao();
        if (feedDao.queryBuilder().where(Id.notEq(id))
                .where(Title.eq(title))
                .unique() != null) {
            Toast.makeText(this, "Title already in use", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }


    @Override
    public void onBackPressed() {
        if(editMode && mFeed != null) {
            swapToViewMode();
        } else {
            launchFeedListActivity();
        }
    }
}
