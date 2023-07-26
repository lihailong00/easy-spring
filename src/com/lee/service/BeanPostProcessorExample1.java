package com.lee.service;

import com.lee.spring.MyBeanPostProcessor;
import com.lee.spring.MyComponent;

@MyComponent
public class BeanPostProcessorExample1 implements MyBeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {
        if (bean instanceof UserService) {
            System.out.println("example 1");
            System.out.println("前置操作");
            System.out.println("针对userService bean\n");
        }
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        if (bean instanceof UserService) {
            System.out.println("example 1");
            System.out.println("后置操作");
            System.out.println("针对userService bean\n");
        }
    }
}
