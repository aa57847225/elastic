package com.whl.demo.controller;

import com.whl.demo.constants.ESWebStatusEnum;
import com.whl.demo.constants.ResponseVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-17 10:31
 **/

@ControllerAdvice
@ResponseBody
public class ExceptionController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

    private String NullPointerExceptionStr="空指针异常";
    private String ArrayIndexOutOfBoundsStr="数组越界异常";
    private String ClassCastExceptionStr="类型转换异常";
    private int ERROR_CODE = 400;

    private static final String logExceptionFormat = "Capture Exception By GlobalExceptionHandler: Code: %s Detail: %s";

    //空指针异常
    @ExceptionHandler(NullPointerException.class)
    public ResponseVo nullPointerExceptionHandler(NullPointerException ex) {
        return resultFormat(ERROR_CODE, new Exception(NullPointerExceptionStr));
    }

    //类型转换异常
    @ExceptionHandler(ClassCastException.class)
    public ResponseVo classCastExceptionHandler(ClassCastException ex) {
        return resultFormat(ERROR_CODE,  new Exception(ClassCastExceptionStr));
    }


    //数组越界异常
    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseVo ArrayIndexOutOfBoundsException(ArrayIndexOutOfBoundsException ex) {
        return resultFormat(ERROR_CODE, new Exception(ArrayIndexOutOfBoundsStr));
    }

    //其他错误
    @ExceptionHandler({Exception.class})
    public ResponseVo exception(Exception ex) {
        return generateResponseVo(ESWebStatusEnum.FAILED, ex.getMessage());
    }

    private <T extends Throwable> ResponseVo resultFormat(Integer code, T ex) {
        logger.info(ex.getMessage(),ex);
        return  generateResponseVo(ESWebStatusEnum.FAILED,ex.getMessage());
    }
}
