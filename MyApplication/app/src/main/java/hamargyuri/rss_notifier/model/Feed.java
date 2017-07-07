package hamargyuri.rss_notifier.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by hamargyuri on 2017. 05. 25..
 */

@Entity
public class Feed implements Parcelable {
    @Id(autoincrement = true)
    private Long id;

    private String title;
    private String url;
    private Date latestItemDate;
    private String notificationTitle;
    private boolean notificationEnabled;

    @Generated(hash = 1667397730)
    public Feed(Long id, String title, String url, Date latestItemDate,
            String notificationTitle, boolean notificationEnabled) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.latestItemDate = latestItemDate;
        this.notificationTitle = notificationTitle;
        this.notificationEnabled = notificationEnabled;
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

    public String getNotificationTitle() {
        return this.notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public boolean isNotificationEnabled() {return this.notificationEnabled;}

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.title);
        dest.writeString(this.url);
        dest.writeLong(this.latestItemDate != null ? this.latestItemDate.getTime() : -1);
        dest.writeString(this.notificationTitle);
        dest.writeByte(this.notificationEnabled ? (byte) 1 : (byte) 0);
    }

    public boolean getNotificationEnabled() {
        return this.notificationEnabled;
    }

    protected Feed(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
        this.url = in.readString();
        long tmpLatestItemDate = in.readLong();
        this.latestItemDate = tmpLatestItemDate == -1 ? null : new Date(tmpLatestItemDate);
        this.notificationTitle = in.readString();
        this.notificationEnabled = in.readByte() != 0;
    }

    public static final Creator<Feed> CREATOR = new Creator<Feed>() {
        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };
}
