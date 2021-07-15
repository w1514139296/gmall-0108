package com.atguigu.gmall.cart.interceptor;

import com.atguigu.gmall.cart.config.JwtProperties;
import com.atguigu.gmall.cart.pojo.UserInfo;
import com.atguigu.gmall.common.utils.CookieUtils;
import com.atguigu.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@EnableConfigurationProperties(JwtProperties.class)
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties properties;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    //    public String userKey;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取userKey
        String userKey = CookieUtils.getCookieValue(request, this.properties.getUserKey());
        if (StringUtils.isBlank(userKey)) {
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.properties.getUserKey(), userKey, this.properties.getExpire());
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setUserKey(userKey);
        //获取token 解析token 获取userId
        String token = CookieUtils.getCookieValue(request, this.properties.getCookieName());
        if (StringUtils.isNotBlank(token)) {
            Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());
            Long userId = (Long) map.get("userId");
            userInfo.setUserId(userId);
        }

        THREAD_LOCAL.set(userInfo);
//        request.setAttribute("userKey", userKey);

        return true;
    }

    //可以获取载荷获取userInfo的方法 获取ThreadLocal
    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        System.out.println("这是后置方法");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        System.out.println("这是完成方法");
        //由于使用的是tomcat线程池  所以请求结束了线程并没有结束 只是回到了线程池 如果不手动释放资源 则导致内存泄漏  会发生oom导致服务器宕机
        THREAD_LOCAL.remove();
    }
}
