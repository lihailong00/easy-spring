package com.lee.spring;

public interface MyBeanPostProcessor {
    void postProcessBeforeInitialization(String beanName, Object bean);

    void postProcessAfterInitialization(String beanName, Object bean);
}
