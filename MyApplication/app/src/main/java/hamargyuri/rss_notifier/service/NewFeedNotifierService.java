package hamargyuri.rss_notifier.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;

import hamargyuri.rss_notifier.model.RSSFeed;
import hamargyuri.rss_notifier.network.RSSFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewFeedNotifierService extends Service {
    public NewFeedNotifierService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
