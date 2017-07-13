package hamargyuri.rss_notifier.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.model.FeedDao;
import hamargyuri.rss_notifier.model.RSSItem;
import hamargyuri.rss_notifier.model.RSSChannel;
import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import hamargyuri.rss_notifier.view.FeedListActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static hamargyuri.rss_notifier.model.FeedDao.Properties.Title;


public class NewFeedNotifierService extends Service {
    private DaoSession session = RSSNotifierApp.getSession();

    public NewFeedNotifierService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                for (final Feed feed : getAllFeeds()) {
                    if (feed.getMinutesSinceLastRefresh() >= feed.getRefreshIntervalInMinutes()) {
                        fetchAndRefreshFeed(feed, feed.getTitle());
                        feed.setMinutesSinceLastRefresh(0);
                    } else {
                        feed.setMinutesSinceLastRefresh(feed.getMinutesSinceLastRefresh()+1);
                    }
                    FeedDao feedDao = session.getFeedDao();
                    feedDao.save(feed);
                    session.clear();
                }
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 6000); //1 min global service run interval

        return super.onStartCommand(intent, flags, startId);
    }

    //TODO: dynamic, updatable, etc.
    public static void sendNotification(Context context, String notificationTitle, String jobDate) {
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.uw_rss_icon)
                        .setContentTitle(notificationTitle)
                        .setContentText(jobDate)
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(context, FeedListActivity.class);

        PendingIntent pending = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pending);

        int notificationId = 1;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void refreshLatestFeedItem(RSSItem rssItem, String title, boolean sendNotification) {
        Date latestItemDate = rssItem.getParsedDate();

        FeedDao feedDao = session.getFeedDao();
        Feed feed = feedDao.queryBuilder().where(Title.eq(title)).unique();
        Date previousDate = feed.getLatestItemDate();

        if (previousDate == null || latestItemDate.after(previousDate)) {
            feed.setLatestItemDate(latestItemDate);
            feedDao.save(feed);
            if (sendNotification) {
                sendNotification(this, feed.getNotificationTitle(), feed.getLatestItemDate().toString());
            }
        }

        session.clear();
    }


    public void fetchAndRefreshFeed(final Feed feed, final String title) {
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
