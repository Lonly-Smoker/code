package com.xu1900.code.service;

import com.xu1900.code.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 评论service
 */
public interface CommentService {
    /**
     * 保存评论
     * @param comment
     */
    public void save(Comment comment);
    /**
     * 根据条件分页查询品论信息
     */
    public Page<Comment> list(Comment s_comment, Integer page, Integer pageSize, Sort.Direction direction, String... properties);
    /**
     * 根据条件获取总记录数
     */
    public Long getTotal(Comment s_comment);
    /**
     * 根据id获取评论对象
     */
    public Comment get(Integer id);
    /**
     * 删除评论
     */
    public void delete(Integer id);
    /**
     * 删除指定资源下的所有评论
     */
    public void deleteByArticleId(Integer articleId);
}
