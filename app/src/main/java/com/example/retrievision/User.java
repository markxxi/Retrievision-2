package com.example.retrievision;

public class User {
     String email;
     String name;
     String profilePictureUrl;
     String uid;
     public User() {}

    public User(String uid, String name, String profilePictureUrl, String email) {
        this.email = email;
        this.name = name;
        this.profilePictureUrl = profilePictureUrl;
        this.uid=uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }
}
