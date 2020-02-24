package com.xu1900.code.shiro.config;

import com.xu1900.code.shiro.realm.MyRealm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shiro配置类
 * FilterChaind定义说明
 * 1、一个URL可以配置多个Filter，使用都好分割
 * 2、当设置多个过滤器的时候，全部验证通过，才视为通过
 * 3、部分过滤器可以指定参数，比如perms，roles
 *
 */
@Configuration
public class ShiroConfig {
    @Bean
    public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        //必须设置
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //如果不设置默认会自动寻找Web工程根目录下的"login.jssp"
        shiroFilterFactoryBean.setLoginUrl("/");
        //拦截器
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        //过滤链定义，从上往下执行，一般将/**放下面 这是个坑，一不小心代码就不好使了
        //authc:所有的url必须认证通过才可以访问    anon:所有url都可以匿名访问
        //配置不会被拦截的链接 顺序判断
        filterChainDefinitionMap.put("/", "anon");
        filterChainDefinitionMap.put("/detail", "anon");
        filterChainDefinitionMap.put("/static/**", "anon");
        filterChainDefinitionMap.put("/ueditor/**", "anon");
        filterChainDefinitionMap.put("/upload/**", "anon");
        filterChainDefinitionMap.put("/user/register.html", "anon");
        filterChainDefinitionMap.put("/user/login.html", "anon");
        filterChainDefinitionMap.put("/user/findPassword.html", "anon");
        filterChainDefinitionMap.put("/user/header.html", "anon");
        filterChainDefinitionMap.put("/user/register", "anon");
        filterChainDefinitionMap.put("/user/login", "anon");
        filterChainDefinitionMap.put("/user/sendEmail", "anon");
        filterChainDefinitionMap.put("/user/checkYzm", "anon");
        filterChainDefinitionMap.put("/user/saveArticle", "anon");
        filterChainDefinitionMap.put("/user/userDownLoadExist", "anon");
        filterChainDefinitionMap.put("/article/**", "anon");
        filterChainDefinitionMap.put("/comment/**", "anon");

        filterChainDefinitionMap.put("/admin/login.html", "anon");
        //配置退出的过滤器，其中的具体退出代码Shiro已经实现了
        filterChainDefinitionMap.put("/user/logout", "logout");
        filterChainDefinitionMap.put("/**", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(myRealm());
        return securityManager;
    }

    /**
     * 身份认证的realm
     *
     * @return
     */
    @Bean
    public MyRealm myRealm() {
        MyRealm myRealm = new MyRealm();
        return myRealm;
    }
    /**
     * Shrio生命周期
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();
    }
    /**
     * 开启Shiro注解（如@RequireRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的风雷，并在必要的时候进行安全逻辑验证
     * 配置以下两个Bean即可实现这个功能
     */
    @Bean
    @DependsOn("lifecycleBeanPostProcessor")
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator(){
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator=new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(){
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor=new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }
}
