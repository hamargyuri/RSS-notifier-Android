package hamargyuri.rss_notifier.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by hamargyuri on 2017. 06. 05..
 */

@Root(name = "channel", strict = false)
public class RSSChannel {
    @Element
    private String title;

    @Element
    private String link;

    @ElementList(name = "item", inline = true)
    private List<RSSItem> items;

    public List<RSSItem> getItems() {
        return items;
    }
}
