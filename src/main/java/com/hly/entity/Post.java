package com.hly.entity;

import com.hly.elasticsearch.ESearchTypeColumn;

import java.util.Date;

/**
 * @author HHH
 * @date 2017/11/17
 */

/**
 * {
 "id":1234,
 "username": "helingyu",
 "title": "this is a title",
 "postDate": 0,
 "content": "this is a content",
 "abstracts":"this is a contract"
 }
 */
public class Post {

    private Long id;

    @ESearchTypeColumn
    private String title;

    @ESearchTypeColumn
    private String username;

    @ESearchTypeColumn
    private Date postDate;

    @ESearchTypeColumn
    private Long views;

    @ESearchTypeColumn
    private Long focus;

    @ESearchTypeColumn(analyze = true)
    private String content;

    @ESearchTypeColumn
    private String abstracts;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    public Long getViews() {
        return views;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public Long getFocus() {
        return focus;
    }

    public void setFocus(Long focus) {
        this.focus = focus;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAbstracts() {
        return abstracts;
    }

    public void setAbstracts(String abstracts) {
        this.abstracts = abstracts;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", username='" + username + '\'' +
                ", postDate=" + postDate +
                ", views=" + views +
                ", focus=" + focus +
                ", content='" + content + '\'' +
                ", abstracts='" + abstracts + '\'' +
                '}';
    }
}
