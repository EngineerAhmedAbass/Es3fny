package com.es3fny.Request;

/**
 * Created by ahmed on 14-Mar-18.
 */

public class User {
    String name;    String email;    String phone;    String nid;    String city;    String street ; String token_id;
    String longtitude , latitude ;
    int year , month , day ;

    public User() {

    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getToken_id() {
        return token_id;
    }
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getNid() {
        return nid;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setToken_id(String token_id) {
        this.token_id = token_id;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setDay(int day) {
        this.day = day;
    }
}
