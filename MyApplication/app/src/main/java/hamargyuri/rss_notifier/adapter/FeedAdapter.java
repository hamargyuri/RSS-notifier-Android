package hamargyuri.rss_notifier.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

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

        title.setText(feed.getTitle() != null ? feed.getTitle() : "no title");
        date.setText(feed.getLatestItemDate() != null ? feed.getLatestItemDate().toString() : null);

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
}