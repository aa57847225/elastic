package com.whl.demo.module;

import org.hibernate.validator.internal.xml.binding.FieldType;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;

/**
 * @program: esspringboot
 * @description: 使用外部mapping 和 setting  注意:boot启动时创构建好索引了。所以删除索引后要重新启动。
 * @author: Mr.Wang
 * @create: 2018-11-19 10:26
 **/
@Document(indexName="car",type="bmw")
@Setting(settingPath = "es/car_setting.json")
@Mapping(mappingPath = "es/car.json")
public class Car {

    @Id
    private String id;

    private String name;

    private String color;

    private Double price;

    private String createTime;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) { this.price = price; }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
