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

import hamargyuri.rss_notifier.R;
import hamargyuri.rss_notifier.model.Feed;
import hamargyuri.rss_notifier.view.FeedDetailsActivity;

public class FeedAdapter extends ArrayAdapter<Feed> {

    public FeedAdapter(Context context, int layoutId, ArrayList<Feed> feeds) {
        super(context, layoutId, feeds);
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

        ImageButton editButton = (ImageButton) convertView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParent.getContext(), FeedDetailsActivity.class);
                intent.putExtra("feed", feed);
                mParent.getContext().startActivity(intent);
            }
        });
        return convertView;
    }

    public void updateFeeds(ArrayList<Feed> feeds){
        this.clear();
        this.addAll(feeds);
        notifyDataSetChanged();
    }
}