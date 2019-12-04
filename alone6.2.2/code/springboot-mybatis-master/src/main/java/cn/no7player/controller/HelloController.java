//package cn.no7player.controller;
//
//import cn.no7player.model.Article;
//import cn.no7player.service.ArticleSearchRepository;
//import com.alibaba.fastjson.JSON;
//import org.apache.commons.collections.IteratorUtils;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.QueryStringQueryBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.sort.SortOrder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
//import org.springframework.data.elasticsearch.core.query.SearchQuery;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Date;
//import java.util.Iterator;
//import java.util.List;
//
//@Controller
//public class HelloController {
//
//    @Autowired
//    private ArticleSearchRepository articleSearchRepository;
//
//    @RequestMapping("/hello")
//    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
//        model.addAttribute("name", name);
//        return "hello";
//    }
//
//    /**
//     {
//     "id":3,
//     "title":"test3"
//     }
//     * @param article
//     * @return
//     */
//    @RequestMapping(value = "/testAdd",method = RequestMethod.POST,headers = "Accept=application/json")
//    @ResponseBody
//    public String  testAdd(@RequestBody Article article) {
////        Article article = new Article();
////        article.setId(1L);
////        article.setTitle("springboot integreate elasticsearch");
////        article.setAbstracts("springboot integreate elasticsearch is very easy");
////        article.setContent("elasticsearch based on lucene,"
////                + "spring-data-elastichsearch based on elaticsearch"
////                + ",this tutorial tell you how to integrete springboot with spring-data-elasticsearch");
//        article.setPostTime(new Date());
////        article.setClickCount(1L);
//        articleSearchRepository.save(article);
//        return "1";
//    }
//
//
//    @RequestMapping("/query")
//    @ResponseBody
//    public String query(@RequestParam String name,@RequestParam String text) {
////        String queryString = "test";//搜索关键字
////        QueryStringQueryBuilder builder = new QueryStringQueryBuilder(queryString);
////        Iterable<Article> searchResult = articleSearchRepository.search(builder);
////        articleSearchRepository.findAll();
////        Iterator<Article> iterator = searchResult.iterator();
////        //System.out.println("=======result======> "+ JSON.toJSONString(iterator.next()));
////        List<Article> articles = IteratorUtils.toList(iterator);
////        while (iterator.hasNext()) {
////            System.out.println(iterator.next());
////        }
////        return JSON.toJSONString(articles);
//
//        Pageable page = new PageRequest(0,10);
//        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//        //  "address","Beijing"
////        queryBuilder.must(QueryBuilders.matchQuery(name,text));
//        queryBuilder.must(QueryBuilders.matchQuery(name,text));
//        SearchQuery query =
//                new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(page).build();
//        Page<Article> pages = articleSearchRepository.search(query);
//        return JSON.toJSONString(pages.getContent());
//    }
//
//
//    @RequestMapping("/querylike")
//    @ResponseBody
//    public String querylike(@RequestParam String name,@RequestParam String text) {
//
//        Pageable page = new PageRequest(0,10);
//        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
//        //  "address","Beijing"
////        queryBuilder.must(QueryBuilders.matchQuery(name,text));
//        queryBuilder.must(QueryBuilders.matchPhrasePrefixQuery(name,text));
//        SearchQuery query =
//                new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(page).withSort(SortBuilders.fieldSort("postTime").order(SortOrder.ASC)).build();
//        Page<Article> pages = articleSearchRepository.search(query);
//        return JSON.toJSONString(pages.getContent());
//    }
//
//    @RequestMapping("/delete")
//    @ResponseBody
//    public String delete() {
//        Article article = new Article();
//        article.setId(1L);
//        articleSearchRepository.delete(article);
//        return "delete success";
//    }
//
//}
