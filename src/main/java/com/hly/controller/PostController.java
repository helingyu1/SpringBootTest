package com.hly.controller;

import com.hly.entity.Post;
import com.hly.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HHH
 * @date 2017/11/17
 */

@RestController
@RequestMapping(value = "/post")
public class PostController {

    @Autowired
    PostService postService;

    @PostMapping(value = "/add")
    public String addPost(@RequestBody Post post){
        String ret = postService.addPost(post);
        return ret;
    }

}
