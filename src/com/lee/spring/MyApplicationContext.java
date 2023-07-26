package com.lee.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApplicationContext类的作用是管理bean对象
 * @author 晓龙coding
 */
public class MyApplicationContext {
    private Class<?> configClass;

    private ConcurrentHashMap<String, MyBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Object, Object> singletonObjects = new ConcurrentHashMap<>();

    private ArrayList<MyBeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * @param configClass 配置类
     */
    public MyApplicationContext(Class<?> configClass) {
        // 获取配置类
        this.configClass = configClass;

        // 扫描 扫描ComponentScan路径下的类
        if (configClass.isAnnotationPresent(MyComponentScan.class)) {
            MyComponentScan componentScanAnnotation = configClass.getAnnotation(MyComponentScan.class);
            // 扫描项目路径 com.lee.service
            String path = componentScanAnnotation.value();
            path = path.replace('.', '/');

            // 拿到主类的类加载器，结合项目路径，得到绝对路径
            ClassLoader classLoader = MyApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            assert resource != null;
            File dir = new File(resource.getFile());

            scanClass();
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                assert files != null;
                for (File file : files) {
                    // fileName: D:\mydoc\codes\java\spring-demo\out\production\spring-demo\com\lee\service\AppConfig.class fileName就是主类的地址。
                    // 基于主类，递归往下搜索所有类。

                    String fileName = file.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        // 获取类在项目下的完整路径: com.lee.service.AppConfig
                        String className = fileName.substring(fileName.indexOf(path.replace('/', '\\')), fileName.indexOf(".class"));
                        try {
                            Class<?> clazz = classLoader.loadClass(className.replace('\\', '.'));
                            System.out.println("name=" + className);
                            if (clazz.isAnnotationPresent(MyComponent.class)) {
                                // 判断某个类是否属于 MyBeanPostProcessor 接口的子类
                                if (MyBeanPostProcessor.class.isAssignableFrom(clazz)) {
                                    try {
                                        MyBeanPostProcessor instance = (MyBeanPostProcessor) clazz.newInstance();
                                        beanPostProcessorList.add(instance);
                                    } catch (InstantiationException | IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                // 获取bean的名字
                                MyComponent component = clazz.getAnnotation(MyComponent.class);
                                String beanName = component.value();
                                if ("".equals(beanName)) {
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }

                                // BeanDefinition
                                MyBeanDefinition beanDefinition = new MyBeanDefinition();
                                beanDefinition.setClazz(clazz);
                                if (clazz.isAnnotationPresent(MyScope.class)) {
                                    MyScope scopeAnnotation = clazz.getAnnotation(MyScope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                }
                                else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        // 创建单例bean
        beanDefinitionMap.keySet().forEach(beanName -> {
            MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

            if ("singleton".equals(beanDefinition.getScope())) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        });
    }

    private void scanClass() {

    }

    private Object createBean(String beanName, MyBeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();

        try {
            Object instance = clazz.getConstructor().newInstance();

            // 依赖注入
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(MyAutowired.class)) {
                    // 保证可以修改私有变量
                    field.setAccessible(true);
                    // 将变量名当做beanName，创建对象并赋值
                    field.set(instance, getBean(field.getName()));
                }
            }

            // 获取回调信息
            if (instance instanceof MyBeanNameAware) {
                ((MyBeanNameAware) instance).setBeanName(beanName);
            }

            // 初始化 前置操作
            beanPostProcessorList.forEach(beanPostProcessor -> {
                beanPostProcessor.postProcessBeforeInitialization(beanName, instance);
            });

            // 额外的初始化
            if (instance instanceof MyInitializingBean) {
                ((MyInitializingBean) instance).afterPropertiesSet();
            }

            // 初始化 后置操作
            beanPostProcessorList.forEach(beanPostProcessor -> {
                beanPostProcessor.postProcessAfterInitialization(beanName, instance);
            });

            // 初始化后 AOP !


            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName) {
        MyBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        if (beanDefinition == null) {
            throw new NullPointerException();
        }

        String scope = beanDefinition.getScope();
        if ("singleton".equals(scope)) {
            Object bean = singletonObjects.get(beanName);
            if (bean == null) {
                Object bean1 = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean1);
                return bean1;
            }
            return bean;
        } else if ("prototype".equals(scope)) {
            return createBean(beanName, beanDefinition);
        } else {
            throw new NullPointerException();
        }
    }
}
