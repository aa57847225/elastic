package com.whl.demo.controller;

import com.whl.demo.constants.ESWebStatusEnum;
import com.whl.demo.constants.ResponseVo;
import com.whl.demo.dao.BaseRepository;
import com.whl.demo.dao.Init;
import com.whl.demo.dao.PostRepository;
import com.whl.demo.module.Book;
import com.whl.demo.module.Post;
import com.whl.demo.page.BootstrapTablePaginationVo;
import com.whl.demo.param.BasicSearchParam;
import org.elasticsearch.index.query.MatchQueryBuilder;
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
import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-08 09:23
 **/
@RestController
@RequestMapping("/post")
public class PostController{

    private static final Logger log = LoggerFactory.getLogger(PostController.class);

    @Resource
    private Init init;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Resource
    private PostRepository postRepository;

    @Resource
    private BaseRepository baseRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @RequestMapping(value = "/deletePostIndex")
    public String deletePostIndex(){
        try {
            baseRepository.deleteIndex("projectname");
            return "0";
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return "1";
        }
    }

    @RequestMapping("/init")
    public String init(){
        try {
            init.init();
            return "0";
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return "1";
        }
    }

    /**
     * 单字符串模糊查询，默认排序。将从所有字段中查找包含传来的word分词后字符串的数据集
     *
     * http://localhost:8081/post/singleWord?page=1&size=10&word=%E5%AE%8B&sort=id.keyword,asc|desc
     * http://localhost:8081/post/singleWord?page=1&size=10&word=%E5%AE%8B&sort=title.keyword,desc
     */
    @RequestMapping("/singleWord")
    public Object singleTitle(String word,Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(word)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }

    /**
     * 单字符串模糊查询，单字段排序。
     */
    @RequestMapping("/singleWord1")
    public Object singlePost(String word, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        //使用queryStringQuery完成单字符串查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(queryStringQuery(word)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }


    /**
     * 单字段对某字符串模糊查询
     */
    @RequestMapping("/singleMatch")
    public Object singleMatch(String content, Integer userId, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("content", content)).withPageable(pageable).build();
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("userId", userId)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }

    /**
     * 单字段对某短语进行匹配查询，短语分词的顺序会影响结果
     */
    @RequestMapping("/singlePhraseMatch")
    public Object singlePhraseMatch(String content, @PageableDefault Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchPhraseQuery("content", content)).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }

    @RequestMapping("/add")
    public Object add() {
        Post post = new Post();
        post.setTitle("我是");
        post.setContent("我爱中华人民共和国");
        post.setWeight(1);
        post.setUserId(1);
        post.setCreateTime(new Date());
        postRepository.save(post);
        post = new Post();
        post.setTitle("我是");
        post.setContent("中华共和国");
        post.setWeight(2);
        post.setUserId(2);
        post.setCreateTime(new Date());
        return postRepository.save(post);
    }

    @RequestMapping("/update")
    public Object update() {
        Post post = new Post();
        post.setId("AWcQTMw3UMTIxf5fRZ7T");
        post.setTitle("我是ssssssssssssssssss");
        post.setContent("我爱中华人民共和国ssssssssssssssssssssssssss");
        post.setWeight(1);
        post.setUserId(1);
        post.setCreateTime(new Date());
        return postRepository.save(post);
    }

    @RequestMapping("/delete")
    public void delete() {
        try {
            Post post = new Post();
            post.setId("AWcQTMw3UMTIxf5fRZ7T");
            postRepository.delete(post);
        }catch (Exception e){
            e.printStackTrace();
        }
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


    /**
     * 多字段匹配
     */
    @RequestMapping("/multiMatch")
    public Object singleUserId(String title, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(multiMatchQuery(title, "title", "content")).withPageable(pageable).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }


    /**
     * 单字段包含所有输入
     */
    @RequestMapping("/contain")
    public Object contain(String title) {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("title", title).operator(MatchQueryBuilder.Operator.AND)).build();
//        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
        return null;
    }

//    /**
//     * 单字段包含所有输入(按比例包含)
//     */
//    @RequestMapping("/contain")
//    public Object contain(String title) {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchQuery("title", title).operator(MatchQueryBuilder.Operator.AND).minimumShouldMatch("75%")).build();
//        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
//    }


    /**
     * 多字段合并查询
     */
    @RequestMapping("/bool")
    public Object bool(String title, Integer userId, Integer weight) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQuery().must(termQuery("userId", userId))
                .should(rangeQuery("weight").lt(weight)).must(matchQuery("title", title))).build();
        return elasticsearchTemplate.queryForList(searchQuery, Post.class);
    }
}
