package com.eve.tp040685.eve.UserClasses;

import java.util.Date;

public class EventPost extends com.eve.tp040685.eve.UserClasses.EventPostId {
    String user_id;
    String event_title;
    String event_start_date;
    String event_end_date;
    String event_venue;
    String image_url;
    String description;
    String image_thumb_url;
    Date timestamp;

    public EventPost() {
    }

    public EventPost(String user_id,String event_title,String event_start_date, String event_end_date, String event_venue, String image_url, String eventDescription, String image_thumb_url, Date timestamp) {
        this.user_id = user_id;
        this.event_title = event_title;
        this.event_start_date = event_start_date;
        this.event_end_date = event_end_date;
        this.event_venue = event_venue;
        this.image_url = image_url;
        this.description = eventDescription;
        this.image_thumb_url = image_thumb_url;
        this.timestamp = timestamp;
    }

    public String getEvent_title() {
        return event_title;
    }

    public void setEventTitle(String event_title) {
        this.event_title = event_title;
    }

    public String getEventStartDate() {
        return event_start_date;
    }

    public void setEvenStartDate(String even_start_date) {
        this.event_start_date = even_start_date;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    public String getEventEndDate() {
        return event_end_date;
    }

    public void setEventEndDate(String event_end_date) {
        this.event_end_date = event_end_date;
    }

    public String getEventVenue() {
        return event_venue;
    }

    public void setEventVenue(String event_venue) {
        this.event_venue = event_venue;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public String getImageUrl() {
        return image_url;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageThumbUrl() {
        return image_thumb_url;
    }

    public void setImageThumbUrl(String image_thumb_url) {
        this.image_thumb_url = image_thumb_url;
    }

}
