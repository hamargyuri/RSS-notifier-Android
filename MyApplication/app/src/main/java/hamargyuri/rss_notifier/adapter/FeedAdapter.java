package hamargyuri.rss_notifier.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.view.FeedListActivity;

public class FeedAdapter extends ArrayAdapter<Feed> {

    private Context mContext;

    public FeedAdapter(Context context, int layoutId, ArrayList<Feed> feeds) {
        super(context, layoutId, feeds);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final Feed feed = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_fragment,
                    parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.feed_title);
        TextView date = (TextView) convertView.findViewById(R.id.feed_update_date);

        title.setText(feed.getTitle() != null ? feed.getTitle() : "no title");
        date.setText(feed.getLatestItemDate() != null ? feed.getLatestItemDate().toString() : null);

        return convertView;
    }

    public void updateFeeds(ArrayList<Feed> feeds){
        this.clear();
        this.addAll(feeds);
        notifyDataSetChanged();
    }
}