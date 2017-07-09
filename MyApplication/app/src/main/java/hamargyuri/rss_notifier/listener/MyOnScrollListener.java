package hamargyuri.rss_notifier.listener;

import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.AbsListView;

import hamargyuri.rss_notifier.view.DynamicListView;


public class MyOnScrollListener implements AbsListView.OnScrollListener {
    private boolean isScrolling = false;
    private DynamicListView listView;
    private SwipeRefreshLayout feedSwipeRefresh;

    public void setIsScrolling(boolean isScrolling){ this.isScrolling = isScrolling; }

    public void setListView(DynamicListView listView) { this.listView = listView; }

    public void setFeedSwipeRefresh(SwipeRefreshLayout feedSwipeRefresh) { this.feedSwipeRefresh = feedSwipeRefresh; }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int topRowVerticalPosition =
                listView.getChildCount() == 0 ? 0 : listView.getChildAt(0).getTop();
        feedSwipeRefresh.setEnabled((firstVisibleItem == 0 && topRowVerticalPosition >= 0) && !isScrolling);
        Log.d("TAG", "onScroll: DISABLE / ENABLE: "+ ((firstVisibleItem == 0 && topRowVerticalPosition >= 0) && !isScrolling));

    }
}
