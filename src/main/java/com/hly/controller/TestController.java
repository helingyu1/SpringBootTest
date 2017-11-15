package com.hly.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author HHH
 * @date 2017/11/14
 */
@Controller
public class TestController {
    @RequestMapping("/")
    @ResponseBody
    String home(){
        String url = "";

        return "Hello World";
    }
}
