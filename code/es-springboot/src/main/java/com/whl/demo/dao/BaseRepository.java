package com.whl.demo.dao;

import com.alibaba.fastjson.JSON;
import com.whl.demo.page.BootstrapTablePaginationVo;
import com.whl.demo.param.BasicSearchParam;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES的操作数据类
 * <p>
 * 备注：对es的一些操作做了一些封装，抽出来一些操作，就是传统的dao层，数据服务
 *
 * @author whl
 * 只支持IK搜索
 */
@Component
public class BaseRepository {

    private static final Logger logger = LoggerFactory.getLogger(BaseRepository.class);

    @Autowired
    private TransportClient client;

    /**
     * 删除单个数据
     *
     * @param id
     * @return
     */
    public String deleteIndexDataSingle(String index,String type,String id) {
        try {
            DeleteResponse response = client.prepareDelete(index, type, id).execute().actionGet();
            return response.toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量删除数据
     *
     * @param idList
     * @return
     */
    public boolean deleteIndexDataBatch(String index,String type,List<String> idList) {

        boolean isUpdate = false;
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for(String id:idList){
                bulkRequest.add(client.prepareDelete(index, type,id));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                System.out.println("failures..............:" + bulkResponse.buildFailureMessage());
                return false;
            }else{
                return true;
            }
        }catch (Exception e){
            logger.info(e.getMessage(),e);
        }
        return isUpdate;
    }

    /**
     * 删除索引
     *
     * @param index
     * @return
     */
    public boolean deleteIndex(String index) {
        if (!isIndexExist(index)) {
            logger.info(" 索引不存在 ！！！！！!");
        }
        DeleteIndexResponse diResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
        if (diResponse.isAcknowledged()) {
            logger.info("删除索引**成功** index->>>>>>>" + index);
        } else {
            logger.info("删除索引**失败** index->>>>> " + index);
        }
        return diResponse.isAcknowledged();
    }

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     */
    public boolean isIndexExist(String index) {
        IndicesExistsResponse iep = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        if (iep.isExists()) {
            logger.info("此索引 [" + index + "] 已经在ES集群里存在");
        } else {
            logger.info(" 没有此索引 [" + index + "] ");
        }
        return iep.isExists();
    }

    /**
     * 自动补齐查询 好比百度的搜索中的下拉数据
     *
     * @param indices
     * @param type
     * @param field
     * @param prefix
     * @return
     */
    public String getSearchBySuggest(String indices, String type, String field, String prefix) {
        //返回的map，进行数据封装
        Map<String, Object> msgMap = new HashMap<String, Object>();
        //创建需要搜索的inde和type
        SearchRequestBuilder requestBuilder = client.prepareSearch(indices).setTypes(type);
        //设置搜索建议
        CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder(field + ".suggest")
                .prefix(prefix).size(10);
        SuggestBuilder suggestBuilder = new SuggestBuilder().addSuggestion(field + ".suggest", completionSuggestionBuilder);

        requestBuilder.suggest(suggestBuilder);
        //进行搜索
        SearchResponse suggestResponse = requestBuilder.execute().actionGet();

        //用来处理的接受结果
        List<String> result = new ArrayList<>();

        List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries = suggestResponse.getSuggest()
                .getSuggestion(field + ".suggest").getEntries();
        //处理结果
        for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> op : entries) {
            List<? extends Suggest.Suggestion.Entry.Option> options = op.getOptions();
            for (Suggest.Suggestion.Entry.Option pp : options) {
                result.add(pp.getText().toString());
            }
        }
        msgMap.put("result", result);
        return JSON.toJSONString(msgMap);
    }

    /**
     * 搜索参数
     *
     * @param param
     * @return
     * @throws Exception
     */
    public BootstrapTablePaginationVo<Map<String, Object>> searchMsgByParam(BasicSearchParam param) throws Exception {
        String keyWord = param.getKeyWord();
        String filed = param.getField();
        String index = param.getIndex();
        Map<String, String> sortMap = param.getSortMap();

        Assert.assertNotNull(client);
        Assert.assertNotNull(filed);
        Assert.assertNotNull(index);
        Assert.assertNotNull(keyWord);

        // 校验索引是否成功
        if (!isIndexExist(index)) {
            return null;
        }
        BootstrapTablePaginationVo<Map<String, Object>> vo = new BootstrapTablePaginationVo<Map<String, Object>>();

        // 响应信息
        List<String> responseStrList = new ArrayList<String>();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(filed, keyWord);
        SearchRequestBuilder sbr = client.prepareSearch(index).setQuery(matchQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        sbr.highlighter(highlightBuilder);


        // 评分靠前 相似度靠前
        sbr.addSort(SortBuilders.scoreSort().order(SortOrder.DESC));

        // 排序
        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            sbr.addSort(SortBuilders.fieldSort(entry.getKey()).order(entry.getValue().equals("0") ? SortOrder.ASC : SortOrder.DESC));
        }
        // .addSort(SortBuilders.fieldSort("createTime").unmappedType("post").order(SortOrder.DESC))

        SearchResponse response = sbr.setFrom(param.getOffset()).setSize(param.getLimit()).get();
        //接受结果
        List<Map<String, Object>> result = new ArrayList<>();

        SearchHits shList = response.getHits();
        for (SearchHit searchHit : shList) {
            Map<String, Object> source = searchHit.getSource();
            //处理高亮片段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
                HighlightField nameField = entry.getValue();
                Text[] fragments = nameField.fragments();
                String nameTmp = "";
                for (Text text : fragments) {
                    nameTmp += text;
                }
                //将高亮片段组装到结果中去
                source.put(entry.getKey(), nameTmp);
            }
            result.add(source);
        }
        vo.setRows(result);
        vo.setTotal(response.getHits().totalHits);
        return vo;
    }

    /**
     * 聚合查询
     * @param param
     * @return
     * @throws Exception
     */
    public BootstrapTablePaginationVo<Map<String, Object>> searchByAggregation(BasicSearchParam param) throws Exception {
        String keyWord = param.getKeyWord();
        String filed = param.getField();
        String index = param.getIndex();
        Map<String, String> sortMap = param.getSortMap();

        Assert.assertNotNull(client);
        Assert.assertNotNull(filed);
        Assert.assertNotNull(index);
        Assert.assertNotNull(keyWord);

        // 校验索引是否成功
        if (!isIndexExist(index)) {
            return null;
        }
        BootstrapTablePaginationVo<Map<String, Object>> vo = new BootstrapTablePaginationVo<Map<String, Object>>();

        // 响应信息
        List<String> responseStrList = new ArrayList<String>();
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(filed, keyWord);
        SearchRequestBuilder sbr = client.prepareSearch(index).setQuery(matchQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        sbr.highlighter(highlightBuilder);


        // 评分靠前 相似度靠前
        sbr.addSort(SortBuilders.scoreSort().order(SortOrder.DESC));

        // 排序
        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            sbr.addSort(SortBuilders.fieldSort(entry.getKey()).order(entry.getValue().equals("0") ? SortOrder.ASC : SortOrder.DESC));
        }

        // 聚合 不用.keyword  Set fielddata=true
        /**
         *
         * Fielddata默认情况下禁用文本字段，因为Fielddata可以消耗大量的堆空间，
         * 特别是在加载高基数text字段时。一旦fielddata被加载到堆中，它将在该段的生命周期中保持在那里。
         * 此外，加载fielddata是一个昂贵的过程，可以导致用户体验延迟命中。处理以上bug可以参考如下方式：
         * 1、可以使用使用该my_field.keyword字段进行聚合，排序或脚本
           2、启用fielddata（不建议使用）

         大多数字段默认为索引，这使得他们可以搜索。但是，排序，聚合和访问脚本中的字段值需要与搜索不同的访问模式。
         搜索需要回答这个问题：“哪些文件包含这个术语？” ，而排序和聚合需要回答一个不同的问题：“ 这个文档对这个文档有什么价值？” 。
         大多数字段可以使用索引时间，磁盘上doc_values的这种数据访问模式，但text字段不支持doc_values。
         相反，text字段使用名为“查询时内存”的数据结构 fielddata。这种数据结构是在第一次使用字段用于聚合，排序或脚本时构建的。它是通过从磁盘读取每个段的全部倒排索引来构建的，反转术语↔︎
         文档关系，并将结果存储在内存中，存储在JVM堆中。

         Fielddata可以消耗大量的堆空间，特别是在加载高基数text字段时。一旦fielddata被加载到堆中，它将在该段的生命周期中保持在那里。此外，加载fielddata是一个昂贵的过程，可以导致用户体验延迟命中。这就是为什么fielddata默认是禁用的。
         如果您尝试对text 字段上的脚本进行排序，聚合或访问值，则会看到以下异常：
         Fielddata默认情况下禁用文本字段。fielddata=true在[ your_field_name] 上设置，以便通过取消倒置索引来加载内存中的fielddata。请注意，这可以使用大量的内存。
         *
         *
         *
         */
        AggregationBuilder aggregation = AggregationBuilders
                .terms("agg")
                .field("id.keyword")
                .subAggregation(
                        AggregationBuilders.topHits("top").from(0)
                                .size(10)).size(100);
        sbr.addAggregation(aggregation);
        // .addSort(SortBuilders.fieldSort("createTime").unmappedType("post").order(SortOrder.DESC))

        SearchResponse response = sbr.setFrom(param.getOffset()).setSize(param.getLimit()).get();
        //接受结果
        List<Map<String, Object>> result = new ArrayList<>();

        SearchHits shList = response.getHits();
        for (SearchHit searchHit : shList) {
            Map<String, Object> source = searchHit.getSource();
            //处理高亮片段
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, HighlightField> entry : highlightFields.entrySet()) {
                HighlightField nameField = entry.getValue();
                Text[] fragments = nameField.fragments();
                String nameTmp = "";
                for (Text text : fragments) {
                    nameTmp += text;
                }
                //将高亮片段组装到结果中去
                source.put(entry.getKey(), nameTmp);
            }
            result.add(source);
        }
        vo.setRows(result);
        vo.setTotal(response.getHits().totalHits);
        return vo;
    }
}
