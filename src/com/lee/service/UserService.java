package com.lee.service;

import com.lee.spring.*;

@MyComponent()
@MyScope("prototype")
public class UserService implements MyBeanNameAware, MyInitializingBean {
    @MyAutowired
    private OrderService orderService;

    private String beanName;

    /**
     * 获取bean的上下文信息
     * @param beanName
     */
    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("执行额外的初始化方法\n");
    }

    public void test() {
        System.out.println(orderService);
        System.out.println("beanName=" + beanName);
    }
}
