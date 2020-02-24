package com.xu1900.code.service.impl;

import com.xu1900.code.entity.Message;
import com.xu1900.code.repository.MessageRepository;
import com.xu1900.code.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

/**
 * 用户消息实现类
 */
@Service("messageService")
@Transactional
public class MessageServiceImpl implements MessageService {
    @Autowired
    private MessageRepository messageRepository;
    @Override
    public Page<Message> list(Integer userId, Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        return messageRepository.findAll(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate=criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        }, PageRequest.of(page-1,pageSize,direction,properties));
    }

    @Override
    public Long getCount(Integer userId) {
        return messageRepository.count(new Specification<Message>() {
            @Override
            public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                Predicate predicate=criteriaBuilder.conjunction();
                if(userId!=null){
                    predicate.getExpressions().add(criteriaBuilder.equal(root.get("user").get("userId"),userId));
                }
                return predicate;
            }
        });
    }

    @Override
    public void updateState(Integer userId) {
         messageRepository.updateState(userId);
    }

    @Override
    public Long getCountByUserId(Integer userId) {
        return messageRepository.getCountByUserId(userId);
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }
}
