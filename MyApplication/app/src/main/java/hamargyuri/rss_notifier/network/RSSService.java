package hamargyuri.rss_notifier.network;

import hamargyuri.rss_notifier.model.RSSFeed;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Url;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

public interface RSSService {

    //if only RSS feeds used the last-modified tag..
    @HEAD
    Call<Void> headFeed(@Url String url);

    @GET
    Call<RSSFeed> getFeed(@Url String url);
}
