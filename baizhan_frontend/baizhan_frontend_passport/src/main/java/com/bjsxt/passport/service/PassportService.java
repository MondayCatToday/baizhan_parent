package com.bjsxt.passport.service;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.pojo.TbUser;

public interface PassportService {

    BaizhanResult checkUserInfo(String value, String flag);

    BaizhanResult register(TbUser user);

    BaizhanResult login(String username, String password);
}
