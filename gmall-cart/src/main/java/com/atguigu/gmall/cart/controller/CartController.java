package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {

//    @Autowired
//    private LoginInterceptor loginInterceptor;
    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){

        System.out.println(LoginInterceptor.getUserInfo());
//        System.out.println(request.getAttribute("userKey"));
//        System.out.println(loginInterceptor.userKey);
        return "test";
    }
}
