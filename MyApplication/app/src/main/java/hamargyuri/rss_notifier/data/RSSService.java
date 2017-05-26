package hamargyuri.rss_notifier.data;

import hamargyuri.rss_notifier.model.GetResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Path;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

public interface RSSService {

    //if only RSS feeds used the last-modified tag..
    @HEAD("{url}")
    Call<Void> headFeed(@Path("url") String url);

    @GET("{url}")
    Call<GetResponse> getFeed(@Path("url") String url);
}
