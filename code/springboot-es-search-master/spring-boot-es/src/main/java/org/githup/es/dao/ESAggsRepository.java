package org.githup.es.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation.SingleValue;
import org.elasticsearch.search.aggregations.metrics.cardinality.CardinalityAggregationBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.githup.es.page.BootstrapTablePaginationVo;
import org.githup.es.param.BasicSearchParam;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * ES 的统计 聚合 dao层
 * 
 * ElasticSearch Aggregations
 * 
 * @author sdc
 *
 */
@Component
public class ESAggsRepository extends BaseRepository {

	private static final Logger LOG = LoggerFactory.getLogger(ESAggsRepository.class);

	@Autowired
	private TransportClient client;

	/**
	 * 搜索参数
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public BootstrapTablePaginationVo<Map<String,Object>> searchMsgByParam(BasicSearchParam param) throws Exception {
		String keyWord = param.getKeyWord();
		String filed = param.getField();
		String index = param.getIndex();

		Assert.assertNotNull(client);
		Assert.assertNotNull(filed);
		Assert.assertNotNull(index);
		Assert.assertNotNull(keyWord);

		// 校验索引是否成功
		if (!isIndexExist(index)) {
			return null;
		}

		BootstrapTablePaginationVo<Map<String,Object>> vo = new BootstrapTablePaginationVo<Map<String,Object>>();

		// 响应信息
		List<String> responseStrList = new ArrayList<String>();
		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(filed, keyWord);
		SearchRequestBuilder sbr = client.prepareSearch(index).setQuery(matchQueryBuilder);
		// 去重的字段
//		if (param.getDistictField() != null) {
//			// 去重的信息
//			CollapseBuilder cb = new CollapseBuilder(param.getDistictField());
//			sbr.setCollapse(cb);
//		}
//		CardinalityAggregationBuilder acb = AggregationBuilders.cardinality("count_id").field(param.getDistictField());

		// 列表参数
//		SearchResponse response = sbr.addAggregation(acb).setFrom(param.getOffset()).addSort("createTime", SortOrder.DESC).setSize(param.getLimit()).get();

		//高亮
		HighlightBuilder highlightBuilder = new HighlightBuilder().field("*").requireFieldMatch(false);
		highlightBuilder.preTags("<span style=\"color:red\">");
		highlightBuilder.postTags("</span>");
		sbr.highlighter(highlightBuilder);

		//  设置搜索建议
//		CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("content.suggest")
//				.prefix(keyWord).size(10);
//		SuggestBuilder suggestBuilder = new SuggestBuilder().addSuggestion("content.suggest", completionSuggestionBuilder);
//		sbr.suggest(suggestBuilder);

		SearchResponse response = sbr.setFrom(param.getOffset()).addSort(SortBuilders.fieldSort("createTime").unmappedType("post").order(SortOrder.DESC)).setSize(param.getLimit()).get();


//		SearchResponse response = sbr.addAggregation(acb).setFrom(param.getOffset()).addSort(SortBuilders.fieldSort("createTime").unmappedType("post").order(SortOrder.DESC)).setSize(param.getLimit()).get();

		//接受结果
		List<Map<String,Object>> result = new ArrayList<>();

		SearchHits shList = response.getHits();
		for (SearchHit searchHit : shList) {
//			responseStrList.add(searchHit.getSourceAsString());
			Map<String, Object> source = searchHit.getSource();
			//处理高亮片段
			Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
			HighlightField nameField = highlightFields.get("content");
			if(nameField!=null){
				Text[] fragments = nameField.fragments();
				String nameTmp ="";
				for(Text text:fragments){
					nameTmp+=text;
				}
				//将高亮片段组装到结果中去
				source.put("content",nameTmp);
			}
			result.add(source);
		}
		vo.setRows(result);

		// 统计模块
//		SingleValue responseAgg = response.getAggregations().get("count_id");
//		int count = 0;
//		if (responseAgg != null) {
//			double value = responseAgg.value();
//			count = getInt(value);
//		}
		vo.setTotal(response.getHits().totalHits);

		return vo;
	}

	public String getSearchBySuggest(String indices, String prefix){
		//返回的map，进行数据封装
		Map<String,Object> msgMap = new HashMap<String,Object>();
		//创建需要搜索的inde和type
		SearchRequestBuilder requestBuilder = client.prepareSearch("news_website").setTypes("news");
		//设置搜索建议
		CompletionSuggestionBuilder completionSuggestionBuilder = new CompletionSuggestionBuilder("title.suggest")
				.prefix(prefix).size(10);
		SuggestBuilder suggestBuilder = new SuggestBuilder().addSuggestion("title.suggest", completionSuggestionBuilder);

		requestBuilder.suggest(suggestBuilder);
		//进行搜索
		SearchResponse suggestResponse = requestBuilder.execute().actionGet();

		//用来处理的接受结果
		List<String> result = new ArrayList<>();

		List<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> entries = suggestResponse.getSuggest()
				.getSuggestion("title.suggest").getEntries();
		//处理结果
		for(Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> op:entries){
			List<? extends Suggest.Suggestion.Entry.Option> options = op.getOptions();
			for(Suggest.Suggestion.Entry.Option pp : options){
				result.add(pp.getText().toString());
			}
		}
		msgMap.put("result",result);
		return JSON.toJSONString(msgMap);
	}

	public static int getInt(double number) {
		BigDecimal bd = new BigDecimal(number).setScale(0, BigDecimal.ROUND_HALF_UP);
		return Integer.parseInt(bd.toString());
	}

	//不要删除，留着备用
		/*public static void getArticleInfoByTags(String tags, List<String> filterList)
	            throws Exception {
	        //
	        SearchRequestBuilder responsebuilder = client.prepareSearch("info")
	                .setTypes("articles")
	                .setSearchType(SearchType.QUERY_THEN_FETCH);

	        // 1. pics过滤
	        NestedQueryBuilder picsFilter = QueryBuilders.nestedQuery(
	                "pics",
	                QueryBuilders.boolQuery()
	                        .must(QueryBuilders.matchQuery("pics.is_down", "1"))
	                        .must(QueryBuilders.matchQuery("pics.is_qr", "0")),
	                ScoreMode.Avg);

	        // 2. 关键词权重计算过滤,过滤ID
	        // ---------keyword查询
	        NestedQueryBuilder kwIDsQuery = QueryBuilders.nestedQuery(
	                "keywords02",
	                QueryBuilders
	                        .boolQuery()
	                        .must(QueryBuilders.matchQuery("keywords02.keyword",
	                                tags).analyzer("xxxx_xxx_analyzer"))
	                        .mustNot(QueryBuilders.termsQuery("_id", filterList)),
	                ScoreMode.Avg);

	        // ---------权重分数函数
	        FilterFunctionBuilder[] keyWeithFunctionBuilders = { new FunctionScoreQueryBuilder.FilterFunctionBuilder(
	                // 这里把_score * doc['keywords02.weight'].value修改成了随机化            
	                ScoreFunctionBuilders.randomFunction(Math.round(Math.random() * 100))
	        ) };

	        // ---kw函数权重查询
	        FunctionScoreQueryBuilder query = QueryBuilders.functionScoreQuery(
	                kwIDsQuery, keyWeithFunctionBuilders);

	        // 综合查询
	        SearchResponse myresponse = responsebuilder.setQuery(query)
	                .setPostFilter(picsFilter)
	                .setCollapse(new CollapseBuilder("keywords01.raw")).setFrom(0)
	                .setSize(5).get();
	        System.out.println(myresponse.toString());
	        SearchHits hits = myresponse.getHits();
	        System.out.println(hits.totalHits);
	        for (int i = 0; i < hits.getHits().length; i++) {
	            String sourceAsString = hits.getHits()[i].getSourceAsString();
	            System.out.println(sourceAsString);
	        }
	    }*/

	public String buildSuggest(){
		try {
//			XContentBuilder mapping = XContentFactory.jsonBuilder()
//					.startObject()
//					.startObject("properties")
//					//      .startObject("m_id").field("type","keyword").endObject()
//					.startObject("poi_index").field("type","integer").endObject()
//					.startObject("poi_title").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_address").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_tags").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_phone").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_lng").field("type","text").endObject()
//					.startObject("poi_lat").field("type","text").endObject()
//					.endObject()
//					.endObject();

			XContentBuilder mapping = XContentFactory.jsonBuilder()
					.startObject()
					.startObject("properties")
					//      .startObject("m_id").field("type","keyword").endObject()
					.startObject("title").field("type","string").startObject("fields").startObject("suggest")
					.field("type","completion").endObject().endObject().endObject()
					.startObject("ccontent").field("type","string").endObject()
//					.startObject("title.suggest").field("type","completion").endObject()
					.endObject()
					.endObject();
			//pois：索引名   cxyword：类型名（可以自己定义）
			PutMappingRequest putmap = Requests.putMappingRequest("news_website").type("news").source(mapping);
			//创建索引
			client.admin().indices().prepareCreate("news_website").execute().actionGet();
			//为索引添加映射
			client.admin().indices().putMapping(putmap).actionGet();
		}catch (Exception e){
			System.out.println("=========error=====");
			e.printStackTrace();
			System.out.println(e.getMessage());
			return "0";
		}
		return "1";
	}
}
