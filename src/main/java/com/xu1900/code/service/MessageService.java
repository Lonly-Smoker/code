package com.xu1900.code.service;

import com.xu1900.code.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

/**
 * 用户消息service
 */
public interface MessageService {
    /**
     * 根据条件分页查询消息
     * @param userId         用户id
     * @param page             当前页
     * @param pageSize          每页记录数
     * @param direction        排序规则
     * @param properties        参数列表
     */
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties);
    /**
     * 查询某个用户下的所有消息总数（未查看）
     */
    public Long getCountByUserId(Integer userId);
    /**
     * 看所有消息
     */
    public void updateState(Integer userId);
    /*
     * 根据条件获取记录数
             */
    public Long getCount(Integer userId);
    /**
     * 添加或修改信息
     */
    public void save(Message message);
}
