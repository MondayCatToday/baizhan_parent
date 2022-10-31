package com.bjsxt.passport.controller;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.passport.service.PassportService;
import com.bjsxt.pojo.TbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前台后端系统 - 用户登录注册系统 - 控制器
 * 记得增加日志处理逻辑。
 */
@RestController
public class PassportController {
    @Autowired
    private PassportService passportService;

    /**
     * 退出登录
     * 商业项目中的退出，执行的逻辑内容不只是销毁会话。
     * 如：记录用户退出时间，记录用户退出日志，清空用户临时缓存等。
     * @param session
     * @return
     */
    @PostMapping("/user/logout")
    public BaizhanResult logout(HttpSession session){
        // 销毁会话
        session.invalidate();
        return BaizhanResult.ok();
    }

    /**
     * 登录。
     * @param username 身份。 用户名、手机号、电子邮箱
     * @param password 密码的明文
     * @return
     */
    @PostMapping("/user/userLogin")
    public BaizhanResult login(String username, String password, HttpSession session){
        try {
            BaizhanResult result = passportService.login(username, password);
            if(result.getStatus() == 200){
                // 登录成功
                System.out.println(result.getData());
                session.setAttribute("user", result.getData());
            }
            return result;
        }catch (Exception e){
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/user/userRegister")
    public BaizhanResult register(TbUser user){
        try {
            return passportService.register(user);
        }catch (Exception e){
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }

    /**
     * 校验用户身份唯一。
     * @return
     */
    @GetMapping("/user/checkUserInfo/{value}/{flag}")
    public BaizhanResult checkUserInfo(@PathVariable String value,
                                       @PathVariable String flag){
        try {
            return passportService.checkUserInfo(value, flag);
        }catch (Exception e){
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
