package org.githup.es.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.githup.es.constants.ESWebStatusEnum;
import org.githup.es.constants.ResponseVo;
import org.githup.es.model.SuggestModel;
import org.githup.es.page.BootstrapTablePaginationVo;
import org.githup.es.param.BasicSearchParam;
import org.githup.es.service.ESAggsSearchService;
import org.githup.es.service.ESSearchService;
import org.githup.es.service.ESSuggestSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

/**
 * 搜索服务
 * 
 * 备注：具体的服务在ESSearchService这个service里，请关注这个service
 * 
 * @author sdc
 *
 */
@RestController
@RequestMapping("/search")
public class SearchRestController extends BaseController{
	
	private static final Logger log = LoggerFactory.getLogger(SearchRestController.class);

	@Autowired
	private ESSearchService esSearchService;
	
	@Autowired
	private ESAggsSearchService eSAggsSearchService;
	
	@Autowired
	private ESSuggestSearchService esSuggestSearchService;

	@RequestMapping(value = "/test")
	public ResponseVo<?> test() throws Exception{
		//判空
		List<String> searchList = esSearchService.searchMessageByKeyWord("bbs_post_index", "content", "测试", 10, 0);
		return generateResponseVo(ESWebStatusEnum.SUCCESS, searchList);
	}
	
	/**
	 * 构建索引
	 * 1.想要高亮建议搜索的话 先创建索引  /buildIndex?index=bbs_post_index
	 * 2 然后执行 最后一个控制器          /buildSuggest
	 * 3.增加数据   addJSONDataDoc
	 * 4.然后搜索                         /getSearchByParam  就能显示效果了
 	 * @param index
	 * @return
	 */
	@RequestMapping(value = "/buildIndex")
	public ResponseVo<?> buildIndex(
			@RequestParam(value = "index", required = false) String index,
			@RequestParam(value = "type", required = false) String type
			) {
		//判空
		if(index == null) {
			return generateResponseVo(ESWebStatusEnum.FAILED, null);
		}
		esSearchService.buildIndex(index,type);
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
	}

	/**
	 * 构建post索引
	 * @param
	 * @return
	 */
	@RequestMapping(value = "/buildPostIndex")
	public ResponseVo<?> buildPostIndex() {
		//判空
		esSearchService.buildPostIndex();
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
	}

	/**
	 * 删除索引
	 * @param index
	 * @return
	 */
	@RequestMapping(value = "/deleteIndex")
	public ResponseVo<?> deleteIndex(
			@RequestParam(value = "index", required = false) String index
	) {
		//判空
		if(index == null) {
			return generateResponseVo(ESWebStatusEnum.FAILED, null);
		}
		esSearchService.deleteIndex(index);
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
	}
	
	/*@RequestMapping(value = "/delIndex")
	public ResponseVo<?> delIndex(
			) {
		
		for(int j=1; j<7; j++) {
			for(int i=1; i<=30; i++) {
				StringBuilder sb = new StringBuilder("forum2018-0");
				sb.append(j);
				sb.append("-");
				
				if(i < 10) {
					sb.append("0" + i);
				}else {
					sb.append(i);
				}
				try { 
					esSearchService.delIndex(sb.toString());
				}catch(Exception e) {
					System.out.println("继续");
				}
			}
		}
		
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
	}*/
	
	/**
	 *  查询数据
	 *  
	 * @param index
	 * @param type
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/data")
	@ResponseBody
	public ResponseVo<?> search(
			@RequestParam(value = "index", required = false) String index,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "id", required = false) String id
			) {
		//判空
		if(index == null || type == null || id == null) {
			return generateResponseVo(ESWebStatusEnum.FAILED, null);
		}
		//搜索具体的数据来源
		Map<String, Object> returnMap = esSearchService.searchDataByParam("bond2018-03-15", "bond", "AWIoxzdzUfSIA3djz-ZK");
		return generateResponseVo(ESWebStatusEnum.SUCCESS, returnMap);
	}
	
	/**
	 * 增加索引
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/build_suggest_index")
	@ResponseBody
	public ResponseVo<?> build_suggest_index(
			) throws Exception {
		//搜索具体的数据来源
		
//		String index = "search_suggest_index";
		String term_index = "suggest_term_index";
		esSuggestSearchService.buildIndexByParam(term_index);
		
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
		
	}
	
	/**
	 * 加入数据
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/addDataToIndex")
	@ResponseBody
	public ResponseVo<?> addDataToIndexForSuggest(
			) throws Exception {
		String term_index = "suggest_term_index";
		
		//搜索具体的数据来源
		SuggestModel data = new SuggestModel();
		data.setContent("联合信用股份有限公司");//北京联合信用投资咨询有限公司，联合信用投资咨询有限公司
		data.setId(1l);
		data.setData(12);
		
		esSuggestSearchService.addDataDocForSuggest( term_index, "tech", data);
		
		SuggestModel data1 = new SuggestModel();
		data1.setContent("北京联合信用投资咨询有限公司");//，联合信用投资咨询有限公司
		data1.setId(1l);
		data1.setData(12);
		
		esSuggestSearchService.addDataDocForSuggest( term_index, "tech", data1);
		
		SuggestModel data2 = new SuggestModel();
		data2.setContent("联合信用投资咨询有限公司");//，
		data2.setId(1l);
		data2.setData(12);
		
		esSuggestSearchService.addDataDocForSuggest( term_index, "tech", data2);
		
		return generateResponseVo(ESWebStatusEnum.SUCCESS, null);
	}
	
	/**
	 * JSON格式化插入数据
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/addJSONDataDoc")
	@ResponseBody
	public ResponseVo<?> addJSONDataDoc(
			) throws Exception {
		String index = "bbs_post_index";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date createTime =  sdf.parse("2018-09-03 13:49:51");
		Date updateTime =  sdf.parse("2018-09-05 13:49:51");

		ModelMap map = new ModelMap();
		map.put("collectCount", 0);
		map.put("commentCount", 0);
		map.put("content", "压力测试，<a data-name=\"themeName\" href=\"#searchList?search=债券市场&type=2\" data-value=\"债券市场\" contenteditable=\"false\" class=\"comment-a\">#债券市场#</a> ，<a data-name=\"bondName\" href=\"#bondInfo/b6028d34b4b16ed2bf3513dcca91daa0\" data-value=\"b6028d34b4b16ed2bf3513dcca91daa0\" contenteditable=\"false\" class=\"comment-a\">$12进出12(120312.IB)$</a> 是发的这只债券吗？ okokok，<a data-name=\"entityName\" href=\"#entityInfo/2029149\" data-value=\"2029149\" contenteditable=\"false\" class=\"comment-a\">$浙江省手工业合作社联合社$</a> 是不是和公司");
		map.put("createTime", createTime);
		map.put("downloadCount", 0);
		map.put("forwardCount", 0);
		map.put("id", 773);
		map.put("isAnonymity", 0);
		map.put("originalContent", "压力测试，#债券市场# ，$12进出12(120312.IB)$ 是发的这只债券吗？ okokok，$浙江省手工业合作社联合社$ 是不是和公司");
		map.put("postVariety", 0);
		map.put("readCount", 0);
		map.put("type", 1);
		map.put("updateTime", updateTime);
		map.put("userId", 241);
		map.put("valid", 1);
		Map map2 = new HashMap();
		String aa = "压力测试，<a data-name=\"themeName\" href=\"#searchList?search=债券市场&type=2\" data-value=\"债券市场\" contenteditable=\"false\" class=\"comment-a\">#债券市场#</a> ，<a data-name=\"bondName\" href=\"#bondInfo/b6028d34b4b16ed2bf3513dcca91daa0\" data-value=\"b6028d34b4b16ed2bf3513dcca91daa0\" contenteditable=\"false\" class=\"comment-a\">$12进出12(120312.IB)$</a> 是发的这只债券吗？ okokok，<a data-name=\"entityName\" href=\"#entityInfo/2029149\" data-value=\"2029149\" contenteditable=\"false\" class=\"comment-a\">$浙江省手工业合作社联合社$</a> 是不是和公司";
		map2.put("input",aa);
		map.put("suggest", map2);
		
		String esId = esSearchService.addJSONDataDoc(index, "post", map);
		
		return generateResponseVo(ESWebStatusEnum.SUCCESS, esId);
	}
	
	/**
	 * 封装参数进行查询
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getSearchByParam")
	@ResponseBody
	public ResponseVo<?> getSearchByParam(
			) throws Exception {
		//搜索具体的数据来源
		
		BasicSearchParam param = new BasicSearchParam();
		param.setIndex("bbs_post_index");
		param.setField("content");
		param.setDistictField("id");
		param.setKeyWord("压");
		param.setLimit(10);
		param.setOffset(0);
		
		/*List<String> list = esSearchService.searchMsgByParam(param);
		Long count = esSearchService.searchMsgCountByParam(param);
		System.out.println(JSONObject.toJSONString(list));
		System.out.println(count);*/
		
		BootstrapTablePaginationVo<Map<String,Object>> vo = eSAggsSearchService.searchMsgByParam(param);
		
		return generateResponseVo(ESWebStatusEnum.SUCCESS, vo);
	}


	/**
	 * 封装参数进行查询
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/getSearchBySuggest")
	@ResponseBody
	public String getSearchBySuggest(
	) throws Exception {
		//搜索具体的数据来源

		BasicSearchParam param = new BasicSearchParam();
		param.setIndex("bbs_post_index");
		param.setField("content");
		param.setDistictField("id");
		param.setKeyWord("游");
		param.setLimit(10);
		param.setOffset(0);

		BootstrapTablePaginationVo<Map<String,Object>> vo = eSAggsSearchService.searchMsgByParam(param);

		return eSAggsSearchService.getSearchBySuggest("bbs_post_index","大话");
	}

	@RequestMapping(value = "/bulkBatch")
	@ResponseBody
	public String bulkBatch(
	) throws Exception {
		//搜索具体的数据来源
		return eSAggsSearchService.bulkBatch();
	}

	@RequestMapping(value = "/buildSuggest")
	@ResponseBody
	public String buildSuggest(
	) throws Exception {
		//搜索具体的数据来源
		return eSAggsSearchService.buildSuggest();
	}
	
}
