package com.xu1900.code.run;

import com.xu1900.code.service.ArcTypeService;
import com.xu1900.code.service.ArticleService;
import com.xu1900.code.service.LinkService;
import com.xu1900.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;

/**
 * 启动服务器是加载数据
 */
@Component
public class StartupRunner implements CommandLineRunner {
    @Autowired
    private ServletContext application;
    @Autowired
    private ArcTypeService arcTypeService;
    @Autowired
    private LinkService linkService;
    @Autowired
    private ArticleService articleService;
    @Override
    public void run(String... args) throws Exception {
        loadData();
    }
    /**
     * 加载数据到application缓存中
     */
    public void loadData(){
        //所有资源分类
        application.setAttribute(Consts.ARC_TYPE_LIST,arcTypeService.listAll(Sort.Direction.ASC,"sort"));
        //所有友情链接
        application.setAttribute(Consts.LINK_LIST,linkService.listAll(Sort.Direction.ASC,"sort"));
        //10条最新资源
        application.setAttribute(Consts.NEW_ARTICLE,articleService.getNewArticle(10));
        //10条热门资源
        application.setAttribute(Consts.CLICK_ARTICLE,articleService.getClickeArticle(10));
        //10条随机资源（热搜推荐）
        application.setAttribute(Consts.RANDOM_ARTICLE,articleService.getRandowArticle(10));
    }
}
