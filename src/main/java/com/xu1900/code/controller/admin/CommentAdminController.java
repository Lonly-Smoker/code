package com.xu1900.code.controller.admin;

import com.xu1900.code.entity.Comment;
import com.xu1900.code.service.CommentService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员评论kongzhiq
 */
@RestController
@RequestMapping(value = "/admin/comment")
public class CommentAdminController {
    @Autowired
    private CommentService commentService;
    /**
     * 根据条件分页查询评论信息
     */
    @RequestMapping(value = "/list")
    @RequiresPermissions(value = "分页查询评论信息")
    public Map<String,Object> list(Comment s_comment, @RequestParam(value = "page",required = false)Integer page,@RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String,Object> resultMap=new HashMap<>();
        Page<Comment> data = commentService.list(s_comment, page, pageSize, Sort.Direction.DESC, "commentDate");
        resultMap.put("data",data.getContent());
        Long total = commentService.getTotal(s_comment);
        resultMap.put("total",total);
        resultMap.put("errorNo",0);
        return resultMap;
    }
    @RequestMapping(value = "/updateState")
    @RequiresPermissions(value = "修改评论状态")
    public Map<String,Object> updateState(Integer commentId,boolean state){
        Comment comment=commentService.get(commentId);
        if(state){//审核通过
            comment.setState(1);
        }
        else {//审核不通过
            comment.setState(2);
        }
        commentService.save(comment);
        Map<String ,Object> map=new HashMap<>();
        map.put("success",true);
        return map;
    }
    /**
     * 删除一条或多条评论
     */
    @RequestMapping(value = "/delete")
    @RequiresPermissions(value = "删除评论")
    public Map<String,Object> delete(@RequestParam(value = "commentId") String ids){
        Map<String,Object> map=new HashMap<>();
        String [] idsStr=ids.split(",");
        for (int i=0;i<idsStr.length;i++){
            commentService.delete(Integer.parseInt(idsStr[i]));
        }
        map.put("errorNo",0);
        return map;
    }

}
