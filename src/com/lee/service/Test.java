package com.lee.service;

import com.lee.spring.MyApplicationContext;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {


        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
    }
}
