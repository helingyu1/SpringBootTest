package com.hly.entity;

import org.springframework.web.bind.annotation.RestController;

/**
 * @author HHH
 * @date 2017/11/24
 */
public class User {

    private String username;

    private String password;

    public User() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
