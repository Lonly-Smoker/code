package com.xu1900.code.controller;

import com.qq.connect.QQConnectException;
import com.qq.connect.oauth.Oauth;
import com.xu1900.code.service.ArticleService;
import com.xu1900.code.service.MessageService;
import com.xu1900.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * qq登录请求处理
 */
@Controller
@RequestMapping(value = "/QQ")
public class QQController {
    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ArticleService articleService;

    @Value("${imgFilePath}")
    private String imgFilePath;
    /**
     * QQ登录页面跳转
     */
    @RequestMapping("qqLogin")
    public void qqLogin(HttpServletRequest request, HttpServletResponse response){
        response.setContentType("text/html;charset=utf-8");
            Oauth oauth = new Oauth();
        try {
            response.sendRedirect(oauth.getAuthorizeURL(request));
        } catch (QQConnectException      e) {
            new QQConnectException("跳转QQ登录失败！");
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

}
