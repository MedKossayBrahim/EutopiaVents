package com.esprit.models;

import java.net.URL;

public class User {
    private int userID;
    private String fullname, email, passwd, userName, image;
    private int phone;
    private Boolean isActive;
    private Role role;
    //URL url = getClass().getResource("/Images/default.png");

    public User(String fullname, String userName, String email, String passwd, int phone) {
        this.fullname = fullname;
        this.userName = userName;
        this.email = email;
        this.passwd = passwd;
        this.phone = phone;
        this.isActive = true;
        this.image = "http://localhost/img/default.png";
//        URL url = getClass().getResource("/Images/default.png");
//        this.image = (url != null) ? url.toExternalForm() : null;
    }


    public User(int userID, String fullname, String email, String passwd, String userName, String image, int phone, Boolean isActive, Role role) {
        this.userID = userID;
        this.fullname = fullname;
        this.email = email;
        this.passwd = passwd;
        this.userName = userName;
        this.image = image;
        this.phone = phone;
        this.isActive = isActive;
        this.role = role;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" + "userID=" + userID + ", nom='" + fullname   + '\'' + ", email='" + email + '\'' + ", passwd='" + passwd + '\'' + ", userName='" + userName + '\'' + ", image='" + image + '\'' + ", phone=" + phone + ", isActive=" + isActive + ", role=" + role + '}';
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public int getphone() {
        return phone;
    }

    public void setphone(int phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}