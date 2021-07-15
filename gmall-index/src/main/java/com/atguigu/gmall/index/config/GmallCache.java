package com.atguigu.gmall.index.config;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
//@Inherited //是否可继承
@Documented
public @interface GmallCache {

    /**
     * 自定义缓存的前缀
     * @return
     */
    String prefix() default "gmall:";

    /**
     * 缓存的过期时间
     * 单位是分钟
     * @return
     */
    int timeout() default 30;

    /**
     * 为了防止缓存雪崩  给缓存时间添加随机值
     * 这里是随机值范围  单位是分钟
     * @return
     */
    int random() default 30;


    /**
     * 为了防止缓存的击穿   给缓存添加分布式锁
     * 这里可以指定分布式锁的前缀
     * @return
     */
    String lock() default "lock:";
}
