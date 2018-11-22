package com.whl.demo.aop;

import com.whl.demo.module.Car;
import com.whl.demo.service.MyLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-22 09:31
 **/
@Aspect
@Component
public class OperateAspect {

    @Pointcut("@annotation(com.whl.demo.service.MyLog)")
        public void annotationPointCut() {
    }

    @Before("annotationPointCut()")
    public void before(JoinPoint joinPoint) {
        MethodSignature sign = (MethodSignature) joinPoint.getSignature();
        Method method = sign.getMethod();
        MyLog annotation = method.getAnnotation(MyLog.class);
        Object[] args = joinPoint.getArgs();
        for(Object arg:args){
            if(arg instanceof Car){
                Car car = (Car) arg;
//                car.setName("黑色");
//                car.setColor("hei se");
            }
            System.out.println(arg);
        }
        System.out.print("打印：" + annotation.value() + " 前置日志");
    }
}
