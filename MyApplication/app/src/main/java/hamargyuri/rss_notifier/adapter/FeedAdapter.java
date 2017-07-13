package hamargyuri.rss_notifier.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.RSSNotifierApp;
import hamargyuri.rss_notifier.model.DaoSession;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.view.FeedDetailsActivity;


public class FeedAdapter extends ArrayAdapter<Feed> {
    private DaoSession session = RSSNotifierApp.getSession();

    final int INVALID_ID = -1;

    HashMap<Feed, Integer> mIdMap = new HashMap<Feed, Integer>();


    public FeedAdapter(Context context, int layoutId, ArrayList<Feed> feeds) {
        super(context, layoutId, feeds);
        for (int i = 0; i < feeds.size(); ++i) {
            mIdMap.put(feeds.get(i), i);
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Feed feed = getItem(position);
        final ViewGroup mParent = parent;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_fragment,
                    parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.feed_title);
        TextView date = (TextView) convertView.findViewById(R.id.feed_update_date);
        TextView relativeTime = (TextView) convertView.findViewById(R.id.feed_relative_time);


        title.setText(feed.getTitle() != null ? feed.getTitle() : "no title");
        date.setText(feed.getLatestItemDate() != null ? feed.getLatestItemDate().toString() : null);
        relativeTime.setText(feed.getLatestItemDate() != null ? convertToRelativeTime(feed.getLatestItemDate()) : null);


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParent.getContext(), FeedDetailsActivity.class);
                intent.putExtra("feed", feed);
                mParent.getContext().startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= mIdMap.size()) {
            return INVALID_ID;
        }
        Feed item = getItem(position);
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void updateFeedsInDB(ArrayList<Feed> feeds) {
        mIdMap.clear();
        for (int i = 0; i < feeds.size(); i++) {
            mIdMap.put(feeds.get(i), i);
            feeds.get(i).setPosition(i);
            session.getFeedDao().save(feeds.get(i));
        }
    }

    public void updateFeeds(ArrayList<Feed> feeds){
        mIdMap.clear();
        for (int i = 0; i < feeds.size(); ++i) {
            mIdMap.put(feeds.get(i), i);
            feeds.get(i).setPosition(i);
            session.getFeedDao().save(feeds.get(i));
        }
        clear();
        addAll(feeds);

        notifyDataSetChanged();
    }

    private String convertToRelativeTime(Date date) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = getContext().getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = getContext().getResources().getConfiguration().locale;
        }
        PrettyTime p = new PrettyTime(locale);

        return p.format(date);
    }
}