package com.whl.demo.module;

/**
 * @program: esspringboot
 * @description: 搜索属性
 * @author: Mr.Wang
 * @create: 2018-11-23 14:36
 **/
public class CarDto extends Car{

    private Double minPrice;

    private Double maxPrice;

    private String startCreateTime;

    private String endCreateTime;

    public Double getMinPrice() { return minPrice; }

    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }

    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public String getStartCreateTime() { return startCreateTime; }

    public void setStartCreateTime(String startCreateTime) { this.startCreateTime = startCreateTime; }

    public String getEndCreateTime() { return endCreateTime; }

    public void setEndCreateTime(String endCreateTime) { this.endCreateTime = endCreateTime; }
}
