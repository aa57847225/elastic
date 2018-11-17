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

    @Resource
    private BaseRepository baseRepository;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    /***
     * @Author whl
     * @Description //TODO 删除人员索引
     * @Date 上午 10:09 2018/11/17 0017
     * @Param []
     * @return com.whl.demo.constants.ResponseVo<?>
     **/
    @RequestMapping(value = "/deletePersonIndex")
    public ResponseVo<?> deleteBookIndex(){
        boolean isBuild =  baseRepository.deleteIndex("person");
        if(isBuild){
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }else{
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /***
     * @Author whl
     * @Description //TODO 构建人员索引
     * @Date 上午 10:09 2018/11/17 0017
     * @Param []
     * @return com.whl.demo.constants.ResponseVo<?>
     **/
    @RequestMapping(value = "/buildPersonIndex")
    public ResponseVo<?> buildBookIndex(){
        boolean isBuild =  personRepository.buildPersonIndex();
        if(isBuild){
            return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
        }else{
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }

    /***
     * @Author whl
     * @Description //TODO 批量增加人员数据
     * @Date 上午 10:13 2018/11/17 0017
     * @Param []
     * @return com.whl.demo.constants.ResponseVo<?>
     **/
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

    @RequestMapping(value = "/searchByParam")
    public ResponseVo<?> searchByParam(){
        try {
            BasicSearchParam param = new BasicSearchParam();
            param.setKeyWord("ya");
//            param.setKeyWord("鸭");
//            param.setKeyWord("zhou鸭");
            param.setField("name.my_pinyin");
            param.setIndex("person");
            param.setLimit(10);
            param.setOffset(0);

            Map<String,String> sortMap = new HashMap<>();
//            sortMap.put("createTime","1");
            param.setSortMap(sortMap);
            BootstrapTablePaginationVo vo =  baseRepository.searchMsgByParam(param);
            return generateResponseVo(ESWebStatusEnum.SUCCESS, vo);
        }catch (Exception e){
            return generateResponseVo(ESWebStatusEnum.FAILED, null);
        }
    }
}
