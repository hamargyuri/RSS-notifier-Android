package hamargyuri.rss_notifier;

import android.app.Application;
import android.content.Intent;

import org.greenrobot.greendao.database.Database;

import hamargyuri.rss_notifier.model.DaoMaster;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.service.NewFeedNotifierService;

import static hamargyuri.rss_notifier.model.FeedDao.createTable;

/**
 * Created by hamargyuri on 2017. 06. 07..
 */

public class RSSNotifierApp extends Application {
    private static final String DATABASE = "RSS feeds";
    private static DaoSession session;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DATABASE);
        Database db = helper.getWritableDb();
        session = new DaoMaster(db).newSession();

//        session.getFeedDao().dropTable(db, true);
//        createTable(db, true);

        startService(new Intent(this, NewFeedNotifierService.class));
    }

    public static DaoSession getSession() {
        return session;
    }
}
