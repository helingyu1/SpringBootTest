package com.hly.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hly.detail.CustomUserDetail;
import com.hly.entity.User;
import com.sun.org.apache.bcel.internal.util.BCELifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by YuQing on 2017/11/24.
 */
public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter{

    private static final Logger LOGGER = LogManager.getLogger();

    private AuthenticationManager authenticationManager;

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public JWTLoginFilter(AuthenticationManager authenticationManager, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);

            LOGGER.info("--------------------------------------" + user.getUsername() + "-----------------" +user.getPassword());
            LOGGER.info("-----------------------------------传上来的密码是：" + bCryptPasswordEncoder.encode(user.getPassword()));
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
//                            bCryptPasswordEncoder.encode(user.getPassword()),
                            user.getPassword(),
                            new ArrayList<GrantedAuthority>())
                    );
        } catch (IOException e){
            LOGGER.error("认证异常", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        LOGGER.info("登陆成功啦！@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        String username = ((CustomUserDetail)authResult.getPrincipal()).getUsername();
        LOGGER.info("0000000000000000000000000000000000000000000000000000000000000          " + username);
        String token = Jwts.builder()
//                .setSubject(((User) authResult.getPrincipal()).getUsername())
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 1000))
                .signWith(SignatureAlgorithm.HS512, "MyJwtSecret")
                .compact();

        LOGGER.info("------------------生成的token是-----------------------------" + token);

        response.addHeader("Authorization", "Bearer " + token);
        LOGGER.info("------------reqeust----------------------------------:" + request);
        chain.doFilter(request,response);
    }
}
