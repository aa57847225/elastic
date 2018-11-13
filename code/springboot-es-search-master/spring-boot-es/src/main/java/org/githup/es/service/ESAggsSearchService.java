package org.githup.es.service;

import java.util.List;

import org.githup.es.page.BootstrapTablePaginationVo;
import org.githup.es.param.BasicSearchParam;
import java.util.*;

/**
 * ES服务端
 * 
 * @author sdc
 *
 */
public interface ESAggsSearchService {
	
	/**
	 * 搜索
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public BootstrapTablePaginationVo<Map<String,Object>> searchMsgByParam(BasicSearchParam param) throws Exception;

	public String getSearchBySuggest(String indices, String prefix);

	public String bulkBatch();

	public String buildSuggest();
}
