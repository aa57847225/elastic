package com.whl.demo.controller;

import com.whl.demo.constants.ESWebStatusEnum;
import com.whl.demo.constants.ResponseVo;
import com.whl.demo.dao.BaseRepository;
import com.whl.demo.dao.PersonRepository;
import com.whl.demo.module.Book;
import com.whl.demo.module.Person;
import com.whl.demo.page.BootstrapTablePaginationVo;
import com.whl.demo.param.BasicSearchParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: esspringboot
 * @description:只支持IK搜索
 * @author: Mr.Wang
 * @create: 2018-11-08 09:23
 **/
@RestController
@RequestMapping("/person")
public class PersonController extends BaseController{

    private static final Logger log = LoggerFactory.getLogger(PersonController.class);

    @Resource
    private PersonRepository personRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @RequestMapping(value = "/deletePersonIndex")
    public ResponseVo<?> deleteBookIndex(){
        boolean isBuild =  personRepository.deleteIndex("person");
        if(isBuild){
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }else{
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/buildPersonIndex")
    public ResponseVo<?> buildBookIndex(){
        boolean isBuild =  personRepository.buildPersonIndex();
        if(isBuild){
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }else{
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/addPersonDataBatch")
    public ResponseVo<?> addPersonDataBatch(){
        List<Person> persons = new ArrayList<>();

        Person b1 = new Person();
        b1.setId(UUID.randomUUID().toString());
        b1.setName("周黑鸭");
        persons.add(b1);

        Person b2 = new Person();
        b2.setId(UUID.randomUUID().toString());
        b2.setName("周黑鸡");
        persons.add(b2);

        String buildIndex =  personRepository.addPersonIndexDataBatch(persons);
        if(!StringUtils.isEmpty(buildIndex)){
            return generateResponseVo(ESWebStatusEnum.SUCCESS, buildIndex);
        }else{
            return generateResponseVo(ESWebStatusEnum.FAILED, buildIndex);
        }
    }

    @RequestMapping(value = "/updateBookDataSingle")
    public ResponseVo<?> updateBookDataSingle(){
        try{

            Book b2 = new Book();
            b2.setId("37e533f3-a44e-42cc-9d97-458936284e96");
            b2.setTitle("西门吹雪大战叶孤城2");
            b2.setContent("西门吹雪是中国网络武侠小说");
            b2.setCreateTime(sdf.format(new Date()));
            b2.setReadCount(10000L);
            b2.setPrice(30.21);
            List<String> author2 = new ArrayList<>();
            author2.add("作者11");
            author2.add("作者21");
            author2.add("作者31");
            b2.setAuthorList(author2);

            personRepository.updateNovelBookIndexDataSingle(b2);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, "success");
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, "error");
        }
    }

    @RequestMapping(value = "/updateBookDataBatch")
    public ResponseVo<?> updateBookDataBatch(){
        try{

            List<Book> books = new ArrayList<>();

            Book b1 = new Book();
            b1.setId("e5ed8df9-952e-4072-a55c-8ca301bef230");
            b1.setTitle("西游记");
            b1.setContent("西游记是中国古代第一部浪漫主义章回体长篇神魔小说");
            b1.setCreateTime(sdf.format(new Date()));
            b1.setReadCount(10000000L);
            b1.setPrice(100.21);
            List<String> author1 = new ArrayList<>();
            author1.add("作者1");
            author1.add("作者2");
            author1.add("作者3");
            b1.setAuthorList(author1);
            books.add(b1);

            Book b2 = new Book();
            b2.setId("37e533f3-a44e-42cc-9d97-458936284e96");
            b2.setTitle("西门吹雪");
            b2.setContent("西门吹雪是中国网络武侠小说");
            b2.setCreateTime(sdf.format(new Date()));
            b2.setReadCount(10000L);
            b2.setPrice(30.21);
            List<String> author2 = new ArrayList<>();
            author2.add("作者11");
            author2.add("作者21");
            author2.add("作者31");
            b2.setAuthorList(author2);
            books.add(b2);

            Boolean isSuccess =  personRepository.updateNovelBookIndexDataBatch(books);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, isSuccess);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, false);
        }
    }

    @RequestMapping(value = "/deleteBookDataSingle")
    public ResponseVo<?> deleteBookDataSingle(){
        try{
            personRepository.deleteNovelBookIndexDataSingle("7e533f3-a44e-42cc-9d97-458936284e96");
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/deleteBookDataBatch")
    public ResponseVo<?> deleteBookDataBatch(){
        try{
            List<Book> bookList = new ArrayList<>();
            Book b1 = new Book();
            b1.setId("37e533f3-a44e-42cc-9d97-458936284e96");
            Book b2 = new Book();
            b2.setId("d66f6a9e-3122-4cb1-b29a-7464325dcadc");
            Book b3 = new Book();
            b3.setId("29c1427d-e37d-4f89-ac01-9ba30d8b2eaa");
            bookList.add(b1);
            bookList.add(b2);
            bookList.add(b3);
            personRepository.deleteNovelBookIndexDataBatch(bookList);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/searchByParam")
    public ResponseVo<?> searchByParam(){
        try {
            BasicSearchParam param = new BasicSearchParam();
//            param.setKeyWord("ya");
            param.setKeyWord("鸭");
            param.setField("name.my_pinyin");
            param.setIndex("person");
            param.setLimit(10);
            param.setOffset(0);

            Map<String,String> sortMap = new HashMap<>();
//            sortMap.put("createTime","1");
            param.setSortMap(sortMap);
            BootstrapTablePaginationVo vo =  personRepository.searchMsgByParam(param);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, vo);
        }catch (Exception e){
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/searchBySuggest")
    public ResponseVo<?> searchBySuggest(){
        try {
            String vo =  personRepository.getSearchBySuggest("book","novel","title","西");
            return generateResponseVo(ESWebStatusEnum.SUCCESS, vo);
        }catch (Exception e){
            log.info(e.getMessage(),e);
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    @RequestMapping(value = "/searchByAggregation")
    public ResponseVo<?> searchByAggregation(){
        try {
            BasicSearchParam param = new BasicSearchParam();
            param.setKeyWord("西吹");
            param.setField("title");
            param.setIndex("book");
            param.setLimit(10);
            param.setOffset(0);

            Map<String,String> sortMap = new HashMap<>();
            sortMap.put("createTime","1");
            param.setSortMap(sortMap);
            BootstrapTablePaginationVo vo =  personRepository.searchByAggregation(param);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, vo);
        }catch (Exception e){
            e.printStackTrace();
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }
}
