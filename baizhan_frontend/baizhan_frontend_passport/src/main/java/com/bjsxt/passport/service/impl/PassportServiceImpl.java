package com.bjsxt.passport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bjsxt.commons.exception.DaoException;
import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.mapper.TbUserMapper;
import com.bjsxt.passport.service.PassportService;
import com.bjsxt.pojo.TbUser;
import com.bjsxt.utils.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;

@Service
public class PassportServiceImpl implements PassportService {
    @Autowired
    private TbUserMapper userMapper;

    /**
     * 登录。
     * 用户传递请求参数的时候。有没有可能，一个身份，既可以匹配用户名，又可以匹配手机号？
     * 系统平台中身份多字段的时候，回人为定义身份顺序。
     * 如：第一身份是手机号、第二身份是用户名、第三身份是电子邮箱。
     * @param username
     * @param password
     * @return
     */
    @Override
    public BaizhanResult login(String username, String password) {
        try {
            // 加密密码
            password = DigestUtils.md5DigestAsHex(password.getBytes());
            QueryWrapper<TbUser> queryWrapper = null;
            // 第一身份，手机号+密码
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", username).eq("password", password);
            // 查询
            TbUser user = userMapper.selectOne(queryWrapper);
            if (user != null) {
                // 第一身份查询成功。返回。
                // 删除会话对象中要保存的数据的敏感数据。数据脱敏。
                // 如：用户的登录密码，用户的余额，用户的支付密码、手机号码等。
                user.setPassword(null);
                user.setPhone(phone2Digest(user.getPhone()));
                return BaizhanResult.ok(user);
            }

            // 第二身份， 用户名+密码。 必须创建新的QueryWrapper对象。维护新的查询条件。
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username).eq("password", password);
            user = userMapper.selectOne(queryWrapper);
            if (user != null) {
                // 第二身份查询成功。返回
                user.setPassword(null);
                user.setPhone(phone2Digest(user.getPhone()));
                return BaizhanResult.ok(user);
            }

            // 第三身份， 电子邮箱+密码
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("email", username).eq("password", password);
            user = userMapper.selectOne(queryWrapper);
            if (user != null) {
                // 第三身份查询成功。 返回
                user.setPassword(null);
                user.setPhone(phone2Digest(user.getPhone()));
                return BaizhanResult.ok(user);
            }

            // 无查询结果。用户名或密码错误
            return BaizhanResult.error("用户名不存在或密码错误");
        }catch (Exception e){
            throw new DaoException("用户登录错误：" + e.getMessage());
        }
    }

    /**
     * 手机号脱敏
     * @return
     */
    private String phone2Digest(String source){
        // replaceAll替换所有服务第一个参数正则的数据为第二个参数。
        // (正则) 可以标记为 $第几个括号位置，引用使用原文。
        return source.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /*public static void main(String[] args) {
        System.out.println(phone2Digest("13512344321"));
    }*/

    /**
     * 注册
     * @param user
     * @return
     */
    @Override
    @Transactional
    public BaizhanResult register(TbUser user) {
        try {
            // 处理用户数据。
            user.setId(IDUtils.genItemId());
            Date now = new Date();
            user.setCreated(now);
            user.setUpdated(now);

            // 密码加密， 使用MD5加密模式
            String source = user.getPassword();
            // 使用Spring提供的MD5加密工具，加密明文密码。
            // 返回结果是MD5加密后的字节数组，转换的16进制数字字符串。
            String digestPassword = DigestUtils.md5DigestAsHex(source.getBytes());
            user.setPassword(digestPassword);

            // 新增数据到数据库
            int rows = userMapper.insert(user);
            if (rows != 1) {
                // 保存错误
                throw new DaoException("用户注册错误");
            }

            return BaizhanResult.ok();
        }catch (DaoException e){
            throw e;
        }catch (Exception e){
            throw new DaoException("注册用户发生错误：" + e.getMessage());
        }
    }

    /**
     * 检查用户身份是否唯一。
     * @param value 要校验的数据
     * @param flag 1 - 用户名； 2 - 手机号； 3 - 电子邮箱
     * @return
     */
    @Override
    public BaizhanResult checkUserInfo(String value, String flag) {
        try {
            // 数据库中不可能有同名同手机号或同电子邮箱的用户。
            QueryWrapper<TbUser> queryWrapper = new QueryWrapper<>();
            // 判断要校验的数据类型
            StringBuilder builder = new StringBuilder("");
            if ("1".equals(flag)) {
                // 用户名是否唯一
                queryWrapper.eq("username", value);
                builder.append("用户名");
            } else if ("2".equals(flag)) {
                // 手机号是否唯一
                queryWrapper.eq("phone", value);
                builder.append("手机号");
            } else if ("3".equals(flag)) {
                // 电子邮箱是否唯一
                queryWrapper.eq("email", value);
                builder.append("电子邮箱");
            } else {
                // 错误请求
                return BaizhanResult.error("服务器忙，请稍后重试");
            }
            TbUser user = userMapper.selectOne(queryWrapper);
            BaizhanResult baizhanResult = null;
            // 判断查询结果是否存在。
            if (user == null) {
                // 数据不存在。用户名、手机号或电子邮箱可用
                baizhanResult = BaizhanResult.ok();
            } else {
                // 数据存在。用户名、手机号或电子邮箱不可用
                builder.append("'");
                builder.append(value);
                builder.append("'");
                builder.append("已重复，请检查后重新申请!");
                baizhanResult = BaizhanResult.error(builder.toString());
            }
            return baizhanResult;
        }catch (Exception e){
            throw new DaoException("校验用户注册信息失败：" + e.getMessage());
        }
    }
}
