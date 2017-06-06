package hamargyuri.rss_notifier.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */
@Root(name = "rss", strict = false)
public class RSSFeed {
    @Attribute
    private String version;

    @Element
    private RSSChannel channel;

    public RSSChannel getChannel() {
        return channel;
    }
}
