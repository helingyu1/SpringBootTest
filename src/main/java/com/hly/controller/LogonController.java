package com.hly.controller;

import com.hly.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

/**
 * @author HHH
 * @date 2017/11/24
 */

//@CrossOrigin(origins = "http://localhost:63342")
@CrossOrigin(origins = "*")
@RestController
public class LogonController {

    private static Logger LOGGER = LogManager.getLogger();

    @RequestMapping(value = "/login", method = RequestMethod.POST)
//    public String handle(@RequestParam String username, @RequestParam String password){
    public String handle(@RequestBody User user){

        LOGGER.info("========================================进入controller");

        if("helingyu".equals(user.getUsername()) && "helingyu1".equals(user.getPassword())) {
            return "ok";
        }else{
            return "sorry";
        }
    }
}
