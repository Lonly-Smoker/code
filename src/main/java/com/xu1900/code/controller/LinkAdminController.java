package com.xu1900.code.controller;

import com.xu1900.code.entity.Link;
import com.xu1900.code.run.StartupRunner;
import com.xu1900.code.service.LinkService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-友情链接控制器
 */
@RestController
@RequestMapping("/admin/link")
public class LinkAdminController {
    @Autowired
    private LinkService linkService;
    @Autowired
    private StartupRunner startupRunner;
    /**
     * 分页查询
     */
    @RequestMapping("/list")
    @RequiresPermissions(value = "分页查询友情链接")
    public Map<String,Object> list(@RequestParam(value = "page" ,required = false)Integer page,@RequestParam(value = "pageSize",required = false)Integer pageSize){
        Map<String,Object> map=new HashMap<>();
        List<Link> list = linkService.list(page, pageSize, Sort.Direction.ASC, "sort");
        map.put("data",list);
        Long count = linkService.getCount();
        map.put("total",count);
        map.put("errorNo",0);
        return map;
    }
    /**
     * 根据id查询实体
     */
    @RequestMapping("/findById")
    @RequiresPermissions(value = "根据id查询友情链接")
    public Map<String,Object> findById(Integer linkId){
        Map<String,Object> map=new HashMap<>();
        map.put("data",linkService.getById(linkId));
        map.put("errorNo",0);
        return map;
    }
    /**
     * 添加或修改友情链接
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = "添加或修改友情链接")
    public Map<String,Object> save(Link link){
        Map<String,Object> map=new HashMap<>();
        linkService.save(link);
        startupRunner.loadData();       //刷新缓存数据
        map.put("errorNo",0);
        return map;
    }
    /**
     * 批量删除友情链接
     */
    @RequestMapping("/delete")
    @RequiresPermissions("批量删除友情链接")
    public Map<String,Object> delete(@RequestParam(value = "linkId")String ids){
        Map<String,Object> map=new HashMap<>();
        String [] idsStr=ids.split(",");
        for(int i=0;i<idsStr.length;i++){
            linkService.delete(Integer.parseInt(idsStr[i]));
        }
        map.put("errorNo",0);
        return map;
    }
}
