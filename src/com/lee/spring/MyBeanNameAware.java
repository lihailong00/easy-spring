package com.lee.spring;

/**
 * 创建bean的时候，获取bean的名字
 * @author 晓龙coding
 */
public interface MyBeanNameAware {
    void setBeanName(String beanName);
}
