package hamargyuri.rss_notifier.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hamargyuri on 2017. 06. 05..
 */

@Root(name = "item", strict = false)
public class RSSItem {

    @Element(name = "title", data = true)
    private String title;

    @Element(name = "pubDate")
    private String dateString;

    @Element(name = "description", data = true, required = false)
    private String description;

    public Date getParsedDate() {
        DateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getDateString() {
        return dateString;
    }
}
