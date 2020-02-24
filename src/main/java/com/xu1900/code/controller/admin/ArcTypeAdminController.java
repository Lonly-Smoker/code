package com.xu1900.code.controller.admin;

import com.xu1900.code.entity.ArcType;
import com.xu1900.code.run.StartupRunner;
import com.xu1900.code.service.ArcTypeService;
import com.xu1900.code.util.Consts;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员资源类型
 */
@RestController
@RequestMapping("/admin/arcType")
public class ArcTypeAdminController {
    @Autowired
    ArcTypeService arcTypeService;
    @Autowired
    private StartupRunner startupRunner;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 待条件的分页查询，查询资源类型列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    @RequestMapping("/list")
    public Map<String, Object> list(@RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Map<String, Object> resultMap = new HashMap<>();
        int count = arcTypeService.getCount().intValue();
        if (page == null && pageSize == null) {
            page = 1;
            pageSize = count > 0 ? count : 1;
        }

        List<ArcType> data = arcTypeService.list(page, pageSize, Sort.Direction.ASC, "sort");
        resultMap.put("data", data);
        resultMap.put("total", count);
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 根据主键id查询资源类型实体
     */
    @RequestMapping("/findById")
    @RequiresPermissions(value = "根据id查询资源类型实体")
    public Map<String, Object> findById(Integer arcTypeId) {
        Map<String, Object> resultMap = new HashMap<>();
        ArcType data = arcTypeService.getById(arcTypeId);
        resultMap.put("data", data);
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 修改用户数据
     */
    @RequestMapping("/save")
    @RequiresPermissions(value = "添加或修改资源类型信息")
    public Map<String, Object> save(ArcType arcType) {
        Map<String, Object> resultMap = new HashMap<>();
        arcTypeService.save(arcType);
        startupRunner.loadData();
        resultMap.put("errorNo", 0);
        return resultMap;
    }

    /**
     * 批量删除类型
     */
    @RequestMapping("delete")
    @RequiresPermissions(value = "删除资源类型信息")
    public Map<String, Object> delete(@RequestParam(value = "arcTypeId") String ids) {
        Map<String, Object> resultMap = new HashMap<>();
        String[] idsStr = ids.split(",");
        for (int i=0;i<idsStr.length;i++) {
            arcTypeService.delete(Integer.parseInt(idsStr[i]));
        }
        redisTemplate.delete(Consts.ALL_ARC_TYPE_NAME);
        startupRunner.loadData();
        resultMap.put("errorNo", 0);
        return resultMap;
    }
}
