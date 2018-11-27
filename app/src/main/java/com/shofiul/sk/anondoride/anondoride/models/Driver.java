package com.shofiul.sk.anondoride.anondoride.models;

public class Driver {

    private String username, email,carnumber,carmodel,rating,userid;
    long phonenumber;

    public Driver(){

    }

    public Driver(String username, String email, String carnumber, String carmodel, String rating, String userid, long phonenumber) {
        this.username = username;
        this.email = email;
        this.carnumber = carnumber;
        this.carmodel = carmodel;
        this.rating = rating;
        this.userid = userid;
        this.phonenumber = phonenumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCarnumber() {
        return carnumber;
    }

    public void setCarnumber(String carnumber) {
        this.carnumber = carnumber;
    }

    public String getCarmodel() {
        return carmodel;
    }

    public void setCarmodel(String carmodel) {
        this.carmodel = carmodel;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public long getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(long phonenumber) {
        this.phonenumber = phonenumber;
    }

    @Override
    public String toString() {
        return "Driver{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", carnumber='" + carnumber + '\'' +
                ", carmodel='" + carmodel + '\'' +
                ", rating='" + rating + '\'' +
                ", userid='" + userid + '\'' +
                ", phonenumber=" + phonenumber +
                '}';
    }
}
