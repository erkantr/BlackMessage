package com.blackmessage.Model;

import com.google.android.gms.location.LocationServices;

public class Background {

    private String imagepath;
    private Long color;
    private String myid;
    private String userid;

    public Background(String imagepath, Long color, String myid, String userid) {
        this.imagepath = imagepath;
        this.color = color;
        this.myid = myid;
        this.userid = userid;
    }

    public Background(){

    }

    public String getImagePath() {
        return imagepath;
    }

    public void setImagePath(String imagePath) {
        this.imagepath = imagePath;
    }

    public Long getColor() {
        return color;
    }

    public void setColor(Long color) {
        this.color = color;
    }

    public String getMyid() {
        return myid;
    }

    public void setMyid(String myid) {
        this.myid = myid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
