package com.blackmessage.Model;

public class User {

    private String id;
    private String username;
    private String number;
    private String imageURL;
    private String status;
    private String search;
    private String hakkimda;

    public User(String id, String username, String number, String imageURL, String status, String search, String hakkimda) {
        this.id = id;
        this.username = username;
        this.number = number;
        this.imageURL = imageURL;
        this.status = status;
        this.search = search;
        this.hakkimda = hakkimda;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getHakkimda() {
        return hakkimda;
    }

    public void setHakkimda(String hakkimda) {
        this.hakkimda = hakkimda;
    }
}
