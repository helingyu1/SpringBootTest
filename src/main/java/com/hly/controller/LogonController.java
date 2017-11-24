package com.hly.controller;

import com.hly.entity.User;
import org.springframework.web.bind.annotation.*;

/**
 * @author HHH
 * @date 2017/11/24
 */

@CrossOrigin(origins = "http://localhost:63342")
@RestController
public class LogonController {
    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public String handle(@RequestParam String username, @RequestParam String password){
    public String handle(@RequestBody User user){
//        if("helingyu".equals(username) && "helingyu1".equals(password)) {
//            return "ok";
//        }else{
//            return "sorry";
//        }
        if("helingyu".equals(user.getUsername()) && "helingyu1".equals(user.getPassword())) {
            return "ok";
        }else{
            return "sorry";
        }
    }
}
