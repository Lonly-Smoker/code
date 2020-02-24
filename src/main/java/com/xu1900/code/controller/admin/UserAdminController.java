package com.xu1900.code.controller.admin;

import com.xu1900.code.entity.User;
import com.xu1900.code.service.UserService;
import com.xu1900.code.util.Consts;
import com.xu1900.code.util.CryptographyUtil;
import com.xu1900.code.util.StringUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/admin/user")
@Controller
public class UserAdminController {
    @Autowired
    private UserService userService;
    /**
     * 根据条件分页查询用户信息
     */
    @ResponseBody
    @RequestMapping("/list")
    @RequiresPermissions("分页查询用户信息")
    public Map<String,Object> list(User s_user, @RequestParam(value = "latelyLoginTimes",required = false)String latelyLoginTimes,
                                   @RequestParam(value = "page",required = false)Integer page,
                                   @RequestParam(value = "pageSize") Integer pageSize){
        String s_blatelyLogingTime=null;
        String s_elatelyLogingTime=null;
        if(StringUtil.isNotEmpty(latelyLoginTimes)){
            String[] strs=latelyLoginTimes.split(",");
            s_blatelyLogingTime=strs[0];
            s_elatelyLogingTime=strs[1];
        }
        Map<String,Object> map=new HashMap<>();
        map.put("data",userService.list(s_user,s_blatelyLogingTime,s_elatelyLogingTime,page,pageSize, Sort.Direction.DESC,"registrationDate"));
        map.put("total",userService.getCount(s_user,s_blatelyLogingTime,s_elatelyLogingTime));
        map.put("errorNo",0);
        return map;
    }
    /**
     * 修改用户VIP状态
     */
    @ResponseBody
    @RequestMapping("/updateVipState")
    @RequiresPermissions("修改用户IVP状态")
    public Map<String,Object> updateVipState(Integer userId,boolean isVip){
        User oldUser=userService.getById(userId);
        oldUser.setVip(isVip);
        userService.save(oldUser);
        Map<String,Object> map=new HashMap<>();
        map.put("success",true);
        return map;
    }
    @ResponseBody
    @RequestMapping("/updateUserState")
    @RequiresPermissions("修改用户封禁状态")
    public Map<String,Object> updateUserState(Integer userId,boolean isOff){
        User oldUser=userService.getById(userId);
        oldUser.setOff(isOff);
        userService.save(oldUser);
        Map<String,Object> map=new HashMap<>();
        map.put("success",true);
        return map;
    }
    @ResponseBody
    @RequestMapping("/resetPassword")
    @RequiresPermissions("重置密码")
    public Map<String,Object> resetPassword(Integer userId){
        User oldUser=userService.getById(userId);
        oldUser.setPassword(CryptographyUtil.md5("123456",CryptographyUtil.SALT));
        userService.save(oldUser);
        Map<String,Object> map=new HashMap<>();
        map.put("errorNo",0);
        return map;
    }

    /**
     * 修改用户VIP等级
     * @return
     */
    @ResponseBody
    @RequestMapping("/updateVipGrade")
    @RequiresPermissions("修改用户VIP等级")
    public Map<String,Object> updateVipGrade(User user){
        User oldUser=userService.getById(user.getUserId());
        oldUser.setVipGrade(user.getVipGrade());
        userService.save(oldUser);
        Map<String,Object> map=new HashMap<>();
        map.put("errorNo",0);
        return map;
    }
    /**
     * 积分充值
     * @return
     */
    @ResponseBody
    @RequestMapping("/addPoints")
    @RequiresPermissions("充值积分")
    public Map<String,Object> addPoints(User user){
        User oldUser=userService.getById(user.getUserId());
        oldUser.setPoints(user.getPoints()+oldUser.getPoints());
        userService.save(oldUser);
        Map<String,Object> map=new HashMap<>();
        map.put("errorNo",0);
        return map;
    }
    /**
     * 管理员自己的密码修改
     */
    @ResponseBody
    @RequiresPermissions(value = "修改管理员密码")
    @RequestMapping("/modifyPassword")
    public Map<String,Object> modifyPassword(String oldPassword,String newPassword,HttpSession session){
        User user=(User)session.getAttribute(Consts.CURRENT_USER);
        Map<String,Object> map=new HashMap<>();
        if(!user.getPassword().equals(CryptographyUtil.md5(oldPassword,CryptographyUtil.SALT))){
            map.put("success",false);
            map.put("errorInfo","原密码错误！");
            return map;
        }
        User oldUser=userService.getById(user.getUserId());
        oldUser.setPassword(CryptographyUtil.md5(newPassword,CryptographyUtil.SALT));
        userService.save(oldUser);
        map.put("success",true);
        return map;
    }
    /**
     * 安全退出
     */
    @RequiresPermissions(value = "安全退出")
    @RequestMapping("/logout")
    public String logout(){
        SecurityUtils.getSubject().logout();
        return "redirect:/admin/login.html";
    }

}
