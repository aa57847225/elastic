package com.whl.demo.module;

/**
 * @program: esspringboot
 * @description: 支持IK 和 pinyin 同时
 * @author: Mr.Wang
 * @create: 2018-11-16 15:20
 **/
public class Person {

    private String  id;
    private String name;
    private String createTime;
    private int age;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
