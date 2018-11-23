package com.whl.demo.controller;

import com.alibaba.fastjson.JSON;
import com.whl.demo.constants.ESWebStatusEnum;
import com.whl.demo.constants.ResponseVo;
import com.whl.demo.dao.BaseRepository;
import com.whl.demo.dao.CarRepository;
import com.whl.demo.dao.Init;
import com.whl.demo.dao.PostRepository;
import com.whl.demo.module.Car;
import com.whl.demo.module.CarDto;
import com.whl.demo.module.Post;
import com.whl.demo.service.MyLog;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-08 09:23
 **/
@RestController
@RequestMapping("/car")
public class CarController extends BaseController{

    private static final Logger log = LoggerFactory.getLogger(CarController.class);

    @Resource
    private Init init;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Resource
    private CarRepository carRepository;

    @Resource
    private BaseRepository baseRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /**
     *  删除索引
     */
    @RequestMapping(value = "/deleteCarIndex")
    public ResponseVo<?> deleteCarIndex() {
        try {
            baseRepository.deleteIndex("car");
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /**
     * 初始化数据
     */
    @RequestMapping("/init")
    public ResponseVo<?> init() {
        try {
            List<Car> cars = new ArrayList<>();
            Car car1 = new Car();
            car1.setId(UUID.randomUUID().toString());
            car1.setColor("黑色");
            car1.setCreateTime(sdf.format(new Date()));
            car1.setName("宝马3系");
            car1.setPrice(350000.00);
            cars.add(car1);

            Car car2 = new Car();
            car2.setId(UUID.randomUUID().toString());
            car2.setColor("红色");
            car2.setCreateTime(sdf.format(new Date()));
            car2.setName("宝马4系");
            car2.setPrice(450000.00);
            cars.add(car2);
            carRepository.saveAll(cars);

            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /**
     *  增加数据
     */
    @RequestMapping("/add")
    public ResponseVo<?> add() {
        try {
            Car car1 = new Car();
            car1.setId(UUID.randomUUID().toString());
            car1.setColor("黑色");
            car1.setCreateTime(sdf.format(new Date()));
            car1.setName("宝马3系");
            car1.setPrice(350000.00);
            carRepository.save(car1);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /**
     * 修改数据
     */
    @RequestMapping("/update")
    public ResponseVo<?> update() {
        try {
            Car car1 = new Car();
            car1.setId("a2d20c20-8bce-46b5-832d-e4f4eed75876");
            car1.setColor("黑色");
            car1.setCreateTime(sdf.format(new Date()));
            car1.setName("宝马www3系");
            car1.setPrice(350000.00);
            carRepository.save(car1);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /**
     * 删除数据
     */
    @RequestMapping("/delete")
    public ResponseVo<?> delete() {
        try {
            Car car1 = new Car();
            car1.setId("a2d20c20-8bce-46b5-832d-e4f4eed75876");
            carRepository.delete(car1);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /**
     * 多字段合并查询
     * http://localhost:8081/car/queryByParam?name=%E5%AE%9D%E9%A9%AC4      宝马3
     * color 没有ik分词只有拼音首字母                                       hs
     *http://localhost:8081/car/queryByParam?startCreateTime=2018-11-22%2003:39:56
     */
    @MyLog("测试")
    @RequestMapping("/queryByParam")
    public Object queryByParam(CarDto car, @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {

        System.out.println("===========car============"+JSON.toJSONString(car));
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        MatchPhraseQueryBuilder nameQb;
        MatchPhraseQueryBuilder colorQb;
        if(car != null){
            if(!StringUtils.isEmpty(car.getName())){
                nameQb = QueryBuilders.matchPhraseQuery("name", car.getName()).slop(1);
//                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", car.getName());
                boolQueryBuilder.must(nameQb);
            }
            if(!StringUtils.isEmpty(car.getColor())){
                colorQb = QueryBuilders.matchPhraseQuery("color", car.getColor()).slop(1);
//                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("color", car.getColor());
                boolQueryBuilder.must(colorQb);
            }

            RangeQueryBuilder rangeQueryBuilder = null;
            if(!StringUtils.isEmpty(car.getMinPrice())){
                rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(car.getMinPrice());
            }

            if(!StringUtils.isEmpty(car.getMaxPrice())){
                if(rangeQueryBuilder != null){
                    rangeQueryBuilder.lte(car.getMaxPrice());
                }
                else {
                    rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(car.getMaxPrice());
                }
            }
            if(rangeQueryBuilder != null){boolQueryBuilder.must(rangeQueryBuilder);}

            RangeQueryBuilder timeRangeQueryBuilder = null;
            if(!StringUtils.isEmpty(car.getStartCreateTime())){
                timeRangeQueryBuilder = QueryBuilders.rangeQuery("createTime").from(car.getStartCreateTime());
            }

            if(!StringUtils.isEmpty(car.getEndCreateTime())){
                if(timeRangeQueryBuilder != null){
                    timeRangeQueryBuilder.to(car.getEndCreateTime());
                }
                else {
                    timeRangeQueryBuilder = QueryBuilders.rangeQuery("createTime").to(car.getEndCreateTime());
                }
            }
            if(timeRangeQueryBuilder != null){boolQueryBuilder.must(timeRangeQueryBuilder);}

        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withSort(SortBuilders.scoreSort().order(SortOrder.DESC)).withQuery(boolQueryBuilder).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Car.class);
    }

    /**
     * 单字符串模糊查询，默认排序。将从所有字段中查找包含传来的word分词后字符串的数据集
     * <p>
     * http://localhost:8081/post/singleWord?page=1&size=10&word=%E5%AE%8B&sort=id.keyword,asc|desc
     * http://localhost:8081/post/singleWord?page=1&size=10&word=%E5%AE%8B&sort=title.keyword,desc
     */
    @RequestMapping("/singleWord")
    public Object singleTitle(String name, Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(name)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Car.class);
    }

    /**
     * 单字符串模糊查询，单字段排序。
     */
    @RequestMapping("/singleWord1")
    public Object singlePost(String word, @PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(word)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Car.class);
    }


    /**
     * 单字段对某字符串模糊查询
     */
    @RequestMapping("/singleMatch")
    public Object singleMatch(String content, Integer userId, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("content", content)).withPageable(pageable).build();
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("userId", userId)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Car.class);
    }

    /**
     * 单字段对某短语进行匹配查询，短语分词的顺序会影响结果
     */
    @RequestMapping("/singlePhraseMatch")
    public Object singlePhraseMatch(String content, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchPhraseQuery("content", content)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Car.class);
    }

    /**
     * term匹配，即不分词匹配，你传来什么值就会拿你传的值去做完全匹配
     */
    @RequestMapping("/singleTerm")
    public Object singleTerm(Integer userId, @PageableDefault Pageable pageable) {
        //不对传来的值分词，去找完全匹配的
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(termQuery("userId", userId)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }
}
