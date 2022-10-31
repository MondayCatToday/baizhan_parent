package com.bjsxt.cart.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * SpringMVC拦截器。
 *
 */
@Component
public class CartInterceptor implements HandlerInterceptor {
    /**
     * 前置拦截
     * 校验用户是否已登录。
     * 已登录返回true。不做特殊处理
     * 未登录返回false。通过响应，向客户端返回一个基本信息。响应状态码是403
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("登录拦截器");
        HttpSession session = request.getSession();
        Object obj = session.getAttribute("user");
        if(null == obj){
            // 未登录
            try {
                /*String resultData = "{\"status\":400, \"msg\":\"请登录\"}";
                response.getWriter().print(resultData);
                response.getWriter().flush();*/
                // sendError方法，不能再响应已提交后调用。
                // 响应提交，代表在响应流中输出数据到客户端。
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "abc");
            }catch (Exception e){
                e.printStackTrace();
            }

            return false;
        }

        // 已登录
        return true;
    }
}
