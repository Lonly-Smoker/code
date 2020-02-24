package com.xu1900.code.controller.admin;

import com.xu1900.code.entity.Article;
import com.xu1900.code.entity.Message;
import com.xu1900.code.lucene.ArticleIndex;
import com.xu1900.code.service.ArticleService;
import com.xu1900.code.service.CommentService;
import com.xu1900.code.service.MessageService;
import com.xu1900.code.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员资源管理控制器
 */
@RestController
@RequestMapping("/admin/article")
public class ArcticleAdminController {
    @Autowired
    private ArticleService articleService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ArticleIndex articleIndex;
    @Autowired
    private CommentService commentService;
    @Autowired
    private MessageService messageService;
    /**
     * 根据条件分页查询资源信息列表
     *
     * @param s_article    条件
     * @param nickname     昵称
     * @param publishDates 发布时间段
     * @param page         当前页
     * @param pageSize     每页记录数
     * @return
     */
    @RequestMapping("list")
    @RequiresPermissions(value = "分页查询资源信息列表")
    public Map<String, Object> list(Article s_article, @RequestParam(value = "nickname", required = false) String nickname,
                                    @RequestParam(value = "publishDates", required = false) String publishDates,
                                    @RequestParam(value = "page", required = false) Integer page,
                                    @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Map<String, Object> resultMap = new HashMap<>();
        String s_bpublishDate = null;//开始时间
        String s_epublishDaye = null;//结束时间
        if (StringUtil.isNotEmpty(publishDates)) {
            String[] str = publishDates.split("-");
            s_bpublishDate = str[1];
            s_epublishDaye = str[2];
        }
        List<Article> data = articleService.list(s_article, nickname, s_bpublishDate, s_epublishDaye, page, pageSize, Sort.Direction.DESC, "publishDate");
        resultMap.put("data", data);
        Long count = articleService.getCount(s_article, nickname, s_bpublishDate, s_epublishDaye);
        resultMap.put("total", count);
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 批量删除资源
     */
    @RequestMapping("delete")
    @RequiresPermissions(value = "删除资源")
    public Map<String, Object> delete(@RequestParam(value = "articleId") String ids) {
        Map<String, Object> resultMap = new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i = 0; i < idsStr.length; i++) {
            commentService.deleteByArticleId(Integer.parseInt(idsStr[i]));
            articleService.delete(Integer.parseInt(idsStr[i]));
            articleIndex.deleteIndex(idsStr[i]);    //删除索引
        }
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 查看或审核页面所需的数据
     */
    @RequestMapping("findById")
    @RequiresPermissions(value = "修改资源")
    public Map<String, Object> toModifyArticlePage(Integer articleId) {
        Article article = articleService.getById(articleId);
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> tempMap = new HashMap<>();
        tempMap.put("articleID", article.getArticleId());
        tempMap.put("name", article.getName());
        tempMap.put("arcType", article.getArcType().getArcTypeId());
        tempMap.put("points", article.getPoints());
        tempMap.put("content", article.getContent());
        tempMap.put("download", article.getDownload());
        tempMap.put("password", article.getPassword());
        tempMap.put("click", article.getClick());
        tempMap.put("keywords", article.getKeywords());
        tempMap.put("description", article.getDescription());
        resultMap.put("data", tempMap);
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 审核资源
     */
    @RequestMapping("updateState")
    @RequiresPermissions(value = "审核资源")
    public Map<String, Object> updateState(Article article) {
        Article oldArticle = articleService.getById(article.getArticleId());
        Message message =new Message();
        message.setUser(oldArticle.getUser());
        message.setArticle(oldArticle);
        message.setPublishDate(new Date());
        oldArticle.setCheckDate(new Date());
        if (article.getState() == 2) {
            message.setContent("【<font color='#00ff7f'>审核成功</font> 】您发布的【"+oldArticle.getName()+"】资源审核成功！");
            oldArticle.setState(2);
        } else if (article.getState() == 3) {
            message.setContent("【<font color='#00ff7f'>审核成功</font> 您发布的【"+oldArticle.getName()+"】资源未通过审核！");
            message.setCause(article.getReason());
            oldArticle.setState(3);
            oldArticle.setReason(article.getReason());
        }

        messageService.save(message);
        articleService.save(oldArticle);
        if(oldArticle.getState()==2){
            oldArticle.setContentNoTag(StringUtil.stripHtml(oldArticle.getContent()));
            articleIndex.addIndex(oldArticle);//添加索引
            redisTemplate.opsForList().leftPush("allarticleId", oldArticle.getArticleId());
            redisTemplate.opsForList().leftPush("article_type_" , oldArticle.getArcType().getArcTypeId());
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("errorNo",0);
        return resultMap;
    }
    /**
     * 生成所有资源的索引
     */
    @ResponseBody
    @RequestMapping("/genAllIndex")
    @RequiresPermissions(value = "生成所有资源索引")
    public boolean genAllIndex(){
        List<Article> articleList=articleService.listStatePass();
        if(articleList==null||articleList.size()==0){
            return false;
        }
        for (Article article:articleList) {
            try {
                article.setContentNoTag(StringUtil.stripHtml(article.getContent()));
                articleIndex.addIndex(article);
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    /**
     * 修改热门资源状态
     */
    @RequestMapping("/updateHotState")
    @RequiresPermissions("修改热门资源状态")
    public Map<String,Object> updateHotState(Integer articleId,boolean isHot){
        Article oldArticle=articleService.getById(articleId);
        oldArticle.setHot(isHot);
        articleService.save(oldArticle);
        Map<String,Object> map=new HashMap<>();
        map.put("success",true);
        return map;
    }
    /**
     * 修改免费资源状态
     */
    @RequestMapping("/updateFreeState")
    @RequiresPermissions("修改免费资源状态")
    public Map<String,Object> updateFreeState(Integer articleId,boolean isFree){
        Article oldArticle=articleService.getById(articleId);
        oldArticle.setFree(isFree);
        articleService.save(oldArticle);
        Map<String,Object> map=new HashMap<>();
        map.put("success",true);
        return map;
    }
}
