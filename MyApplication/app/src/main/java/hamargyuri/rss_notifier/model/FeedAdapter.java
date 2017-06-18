package hamargyuri.rss_notifier.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import hamargyuri.rss_notifier.R;

public class FeedAdapter extends ArrayAdapter<Feed> {

    public FeedAdapter(Context context, int layoutId, ArrayList<Feed> feeds) {
        super(context, layoutId, feeds);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Feed feed = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.feed_fragment,
                    parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.feed_title);
        TextView date = (TextView) convertView.findViewById(R.id.feed_update_date);

        title.setText(feed != null ? feed.getTitle() : "no title");
        date.setText(feed != null ? feed.getLatestItemDate().toString() : null);

        return convertView;
    }
}