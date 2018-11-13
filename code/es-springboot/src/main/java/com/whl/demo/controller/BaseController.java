package com.whl.demo.controller;

import com.whl.demo.constants.ESWebStatusEnum;
import com.whl.demo.constants.ResponseVo;

/**
 * 基础数据建设
 * @author sdc
 *
 */
public class BaseController {
	
	 /**
     * 生成统一的返回响应对象
     *
     * @param webStatusEnum 状态码枚举
     * @param data 数据对象
     * @param <T> 数据对象类型参数
     * @return
     */
    public <T> ResponseVo generateResponseVo(ESWebStatusEnum webStatusEnum, T data) {
        return new ResponseVo(webStatusEnum.getCode(), webStatusEnum.getDesc(), data);
    }
    
    
}
