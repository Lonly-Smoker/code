package com.xu1900.code.controller;

import com.xu1900.code.entity.Comment;
import com.xu1900.code.entity.User;
import com.xu1900.code.service.CommentService;
import com.xu1900.code.util.Consts;
import com.xu1900.code.util.HTMLUtil;
import com.xu1900.code.util.StringUtil;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 评论Controller
 */
@RequestMapping(value = "/comment")
@Controller
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 前端提交保存评论信息
     * @param comment
     * @param session
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
    public Map<String,Object> add(Comment comment, HttpSession session){
        Map<String ,Object> map=new HashMap<>();
        comment.setContent(StringUtil.esc(comment.getContent()));
        comment.setCommentDate(new Date());
        comment.setState(0);
        comment.setUser((User) session.getAttribute(Consts.CURRENT_USER));
        commentService.save(comment);
        map.put("success",true);
        return map;
    }
    /**
     * 分页查询某个资源的评论信息
     */
    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> list(Comment s_comment, @RequestParam(value = "page" ,required = false)Integer page ){
        s_comment.setState(1);
        Page<Comment> commentPage=commentService.list(s_comment,page,5, Sort.Direction.DESC,"commentDate");
        Map<String ,Object> map=new HashMap<>();
        map.put("data", HTMLUtil.getCommentPageStr(commentPage.getContent()));
        map.put("total",commentPage.getTotalPages());
        return map;
    }
}
