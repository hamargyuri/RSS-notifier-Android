package hamargyuri.rss_notifier.network;

import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

public class RSSFactory {

    public static RSSService create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.google.com/")
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .build();
        return retrofit.create(RSSService.class);
    }
}
