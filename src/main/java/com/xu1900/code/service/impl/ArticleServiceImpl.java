package com.xu1900.code.service.impl;

import com.xu1900.code.entity.ArcType;
import com.xu1900.code.entity.Article;
import com.xu1900.code.lucene.ArticleIndex;
import com.xu1900.code.repository.ArticleRepository;
import com.xu1900.code.run.StartupRunner;
import com.xu1900.code.service.ArcTypeService;
import com.xu1900.code.service.ArticleService;
import com.xu1900.code.util.Consts;
import com.xu1900.code.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.*;

/**
 * 资源Service实现类
 */
@Transactional
@Service("articleService")
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private ArcTypeService arcTypeService;
    @Autowired
    private ArticleIndex articleIndex;
    @Autowired
    private StartupRunner startupRunner;
    private RedisSerializer redisSerializer = new StringRedisSerializer();

    @Override
    public List<Article> list(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<Article> pageArticle = articleRepository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root, criteriaBuilder, s_bpublishDate, s_epublishDate, nickname, s_article);
            }
        }, PageRequest.of(page - 1, pageSize, direction, properties));
        return pageArticle.getContent();
    }

    @Override
    public Long getCount(Article s_article, String nickname, String s_bpublishDate, String s_epublishDate) {
        Long count = articleRepository.count(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                return getPredicate(root, criteriaBuilder, s_bpublishDate, s_epublishDate, nickname, s_article);
            }
        });
        return count;
    }

    @Override
    public void save(Article article) {
        if (article.getState() == 2) {          //把审核通过的资源放到redis中
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.opsForValue().set("article_" + article.getArticleId(), article);
            startupRunner.loadData();       //刷新缓存
        }
        articleRepository.save(article);
    }

    @Override
    public void delete(Integer id) {
        redisTemplate.opsForList().remove("allarticleId",0,id);
        int arcTypeId=articleRepository.getArcTypeIdByArticleId(id);
        redisTemplate.opsForList().remove("article_type_"+arcTypeId,0,id);
        redisTemplate.delete("article_" + id);
        articleRepository.deleteById(id);
    }

    @Override
    public Article getById(Integer id) {
        if (redisTemplate.hasKey("article_" + id)) {
            return (Article) redisTemplate.opsForValue().get("article_" + id);
        } else {
            Article article = articleRepository.getOne(id);
            if (article.getState() == 2) {
                redisTemplate.setKeySerializer(redisSerializer);
                redisTemplate.opsForValue().set("article_" + article.getArticleId(), article);
            }
            return article;
        }
    }
    @Override
    public Map<String, Object> list(String type, Integer page, Integer pageSize) {
        //1、初始化redis模板,初始化返回值
        redisTemplate.setKeySerializer(redisSerializer);
        ValueOperations<Object, Object> opsForValue = redisTemplate.opsForValue();
        ListOperations<Object, Object> opsForList = redisTemplate.opsForList();
        Map<String,Object> resultMap=new HashMap<>();
        List<Article> tempList=new ArrayList<>();
        //2、判断redis有没有资源列表
        Boolean flag = redisTemplate.hasKey("allarticleId");
        //3、如果redis里没有资源列表，去数据库查询
        if(!flag) {
            //3.1、遍历资源列表
            List<Article> listStatePass = listStatePass();
            for(Article article:listStatePass) {
                //3.2、将每一个资源放入redis中
                opsForValue.set("article_"+article.getArticleId(),article);
                //3.3、将每一个资源的id推入redis的allarticleId列表
                opsForList.rightPush("allarticleId",article.getArticleId());
                //3.4、遍历资源类型列表，将该资源推入响应的redis资源类型列表
                List<ArcType> arcTypeList = (List<ArcType>) arcTypeService.listAll(Sort.Direction.ASC,"sort");
                for (ArcType arcType:arcTypeList) {
                    int articleTypeID=article.getArcType().getArcTypeId().intValue();
                    int getArticleTypeID=arcType.getArcTypeId().intValue();
                    if(articleTypeID==getArticleTypeID){
                        opsForList.rightPush("article_type"+arcType.getArcTypeId(),article.getArticleId());
                    }
                }
            }
        }
        //4、分页资源列表并且返回当前页
        long start=(page-1)*pageSize;
        long end=pageSize*page-1;
        List idList;
        long count;
        if("all".equals(type)){
            idList=opsForList.range("allarticleId",start,end);
            count=opsForList.size("allarticleId");
        }else{
            idList=opsForList.range("article_type"+type,start,end);
            count=opsForList.size("article_type"+type);
        }
        for (Object id : idList){
            tempList.add((Article) opsForValue.get("article_"+id));
        }
        resultMap.put("data",tempList);
        resultMap.put("count",count);
        return resultMap;
    }

    @Override
    public List<Article> listStatePass() {
        return articleRepository.findAll(new Specification<Article>() {
            @Override
            public Predicate toPredicate(Root<Article> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate = criteriaBuilder.conjunction();
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"),2));
                return predicate;
            }
        },Sort.by(Sort.Direction.DESC,"publishDate"));
    }

    @Override
    public void updateClick(Integer articleId) {
        articleRepository.updateClick(articleId);
        Article article = articleRepository.getOne(articleId);
        if(article.getState()==2){
            redisTemplate.setKeySerializer(redisSerializer);
            redisTemplate.opsForValue().set("article_"+article.getArticleId(),article);
        }
    }

    @Override
    public Integer todayPublish() {
        return articleRepository.todayPublish();
    }

    @Override
    public Integer noAudit() {
        return articleRepository.noAudit();
    }

    @Override
    public List<Article> getNewArticle(Integer n) {
        return articleRepository.getNewArticle(n);
    }

    @Override
    public List<Article> getClickeArticle(Integer n) {
        return articleRepository.getClickeArticle(n);
    }

    @Override
    public List<Article> getRandowArticle(Integer n) {
        return articleRepository.getRandowArticle(n);
    }

    /**
     * 查询条件
     *
     * @param root
     * @param criteriaBuilder
     * @param s_bpublishDate
     * @param s_epublishDate
     * @param nickname
     * @param s_article
     * @return
     */
    private Predicate getPredicate(Root<Article> root, CriteriaBuilder criteriaBuilder, String s_bpublishDate, String s_epublishDate, String nickname, Article s_article) {
        Predicate predicate = criteriaBuilder.conjunction();
        if (StringUtil.isNotEmpty(s_bpublishDate)) {
            predicate.getExpressions().add(criteriaBuilder.greaterThanOrEqualTo(root.get("publishDate").as(String.class), s_bpublishDate));
        }
        if (StringUtil.isNotEmpty(s_epublishDate)) {
            predicate.getExpressions().add(criteriaBuilder.lessThanOrEqualTo(root.get("publishDate").as(String.class), s_epublishDate));
        }
        if (StringUtil.isNotEmpty(nickname)) {
            predicate.getExpressions().add(criteriaBuilder.like(root.get("user").get("nickname"), "%" + nickname + "%"));
        }
        if (s_article != null) {
            if (StringUtil.isNotEmpty(s_article.getName())) { //标题
                predicate.getExpressions().add(criteriaBuilder.like(root.get("name"), "%" + s_article.getName() + "%"));
            }
            if (s_article.isHot()) {          //是否热门
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isHot"), 1));
            }
            if (s_article.getArcType() != null && s_article.getArcType().getArcTypeId() != null) {  //类型
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("arcType").get("arcTypeId"), s_article.getArcType().getArcTypeId()));
            }
            if (s_article.getUser() != null && s_article.getUser().getUserId() != null) {   //用户
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"), s_article.getUser().getUserId()));
            }
            if (s_article.getState() != null) {     //审核 状态
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("state"), s_article.getState()));
            }
            if (!s_article.isUseful()) {
                predicate.getExpressions().add(criteriaBuilder.equal(root.get("isUseful"), false));
            }
        }
        return predicate;
    }
}
