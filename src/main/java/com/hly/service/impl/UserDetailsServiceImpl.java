package com.hly.service.impl;

import com.hly.detail.CustomUserDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.util.Collections.emptyList;

/**
 * Created by YuQing on 2017/11/24.
 */

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static Logger LOGGER = LogManager.getLogger();

    BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDetailsServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder){
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        com.hly.entity.User user = new com.hly.entity.User();
        user.setUsername("helingyu");
//        user.setPassword("helingyu1");
        user.setPassword(bCryptPasswordEncoder.encode("helingyu1"));
        LOGGER.info("---------------------查出来的密码是：" + bCryptPasswordEncoder.encode("helingyu1"));


        CustomUserDetail customUserDetail = new CustomUserDetail();
        customUserDetail.setUser(user);
        return customUserDetail;
    }
}
