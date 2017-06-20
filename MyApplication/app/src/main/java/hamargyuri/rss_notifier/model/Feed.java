package hamargyuri.rss_notifier.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

@Entity
public class Feed {
    @Id(autoincrement = true)
    private Long id;

    private String title;
    private String url;
    private Date latestItemDate;

    @Generated(hash = 223791982)
    public Feed(Long id, String title, String url, Date latestItemDate) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.latestItemDate = latestItemDate;
    }

    @Generated(hash = 1810414124)
    public Feed() {
    }

    public Long getId() {
        return id;
    }

    public Date getLatestItemDate() {
        return latestItemDate;
    }

    public void setLatestItemDate(Date latestItemDate) {
        this.latestItemDate = latestItemDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setId(Long id) {
        this.id = id;
    }
}