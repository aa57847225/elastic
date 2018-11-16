package com.whl.demo.module;

import java.util.Date;
import java.util.List;

/**
 * @program: esspringboot
 * @description:只支持IK搜索
 * @author: Mr.Wang
 * @create: 2018-11-07 18:37
 **/
public class Book {

    private String id;
    private String title;
    private String content;
    private String createTime;
    private Long readCount;
    private double price;
    private List<String> authorList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Long getReadCount() {
        return readCount;
    }

    public void setReadCount(Long readCount) {
        this.readCount = readCount;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<String> authorList) {
        this.authorList = authorList;
    }
}
