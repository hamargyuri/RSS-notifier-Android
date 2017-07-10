package hamargyuri.rss_notifier.listener;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.AbsListView;

import hamargyuri.rss_notifier.view.DynamicListView;


public class MyOnScrollListener implements AbsListView.OnScrollListener {
    private boolean disableSwipeRefresh = false;
    private DynamicListView listView;
    private SwipeRefreshLayout feedSwipeRefresh;
    private int firstVisibleItem;

    public int getFirstVisibleItem() { return firstVisibleItem; }

    public void setDisableSwipeRefresh(boolean disableSwipeRefresh){ this.disableSwipeRefresh = disableSwipeRefresh; }

    public void setListView(DynamicListView listView) { this.listView = listView; }

    public void setFeedSwipeRefresh(SwipeRefreshLayout feedSwipeRefresh) { this.feedSwipeRefresh = feedSwipeRefresh; }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.firstVisibleItem = firstVisibleItem;
        int topRowVerticalPosition =
                listView.getChildCount() == 0 ? 0 : listView.getChildAt(0).getTop();
        if (disableSwipeRefresh) {
            feedSwipeRefresh.setEnabled(false);
            Log.d("MyOnScrollListener", "onScroll: FALSE");
        } else {
            feedSwipeRefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            Log.d("MyOnScrollListener", "onScroll: "+ (firstVisibleItem == 0 && topRowVerticalPosition >= 0));
        }
    }

    public void evaluateToggle(int firstVisibleItem) {
        int topRowVerticalPosition =
                listView.getChildCount() == 0 ? 0 : listView.getChildAt(0).getTop();
        feedSwipeRefresh.setEnabled((firstVisibleItem == 0 && topRowVerticalPosition >= 0) && !disableSwipeRefresh);
    }
}
