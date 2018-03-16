package com.optic.simpleblog.model;

/**
 * Created by User on 24/01/2018.
 */

public class Comments {
    private String comment;
    private String posted_by;
    private long time;

    public Comments(){

    }

    public Comments(String comment, String posted_by, long time) {
        this.comment = comment;
        this.posted_by = posted_by;
        this.time = time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPosted_by() {
        return posted_by;
    }

    public void setPosted_by(String posted_by) {
        this.posted_by = posted_by;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
