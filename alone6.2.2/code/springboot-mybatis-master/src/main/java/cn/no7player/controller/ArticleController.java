package cn.no7player.controller;

import cn.no7player.model.Article;
import cn.no7player.service.ArticleSearchRepository;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "article")
public class ArticleController {

    @Autowired
    private ArticleSearchRepository articleSearchRepository;

    /**
     {
     "id":3,
     "title":"test3"
     }
     添加索引
     * @param article
     * @return
     */
    @RequestMapping(value = "/add",method = RequestMethod.POST,headers = "Accept=application/json")
    @ResponseBody
    public String  add(@RequestBody Article article) {

        Map<String,String> resultMap = new HashMap<>();
        try{
            article.setPostTime(new Date());
            articleSearchRepository.save(article);
            resultMap.put("code","1");
            resultMap.put("msg","add success");
        }catch (Exception e){
            resultMap.put("code","2");
            resultMap.put("msg","add error");
        }
        return JSON.toJSONString(resultMap);
    }


    /**
     * 分页查询，排序 ，模糊搜素
     * @param name
     * @param text
     * @return
     */
    @RequestMapping("/query")
    @ResponseBody
    public String query(@RequestParam String name, @RequestParam String text) {
        Pageable page = new PageRequest(0,10);
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchPhrasePrefixQuery(name,text));
        SearchQuery query =
                new NativeSearchQueryBuilder().withQuery(queryBuilder).withPageable(page).withSort(SortBuilders.fieldSort("postTime").order(SortOrder.ASC)).build();
        Page<Article> pages = articleSearchRepository.search(query);
        return JSON.toJSONString(pages.getContent());
    }

    /**
     * 删除索引
     * @return
     */
    @RequestMapping("/delete")
    @ResponseBody
    public String delete() {
        Map<String,String> resultMap = new HashMap<>();
        try{
            Article article = new Article();
            article.setId(1L);
            articleSearchRepository.delete(article);
            resultMap.put("code","1");
            resultMap.put("msg","delete success");
        }catch (Exception e){
            resultMap.put("code","2");
            resultMap.put("msg","delete error");
        }
        return JSON.toJSONString(resultMap);
    }
}
