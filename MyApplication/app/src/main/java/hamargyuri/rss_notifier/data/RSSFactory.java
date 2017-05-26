package hamargyuri.rss_notifier.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

public class RSSFactory {
    private final static Gson GSON = new GsonBuilder().setLenient().create();

    public static RSSService create() {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build();
        return retrofit.create(RSSService.class);
    }
}
