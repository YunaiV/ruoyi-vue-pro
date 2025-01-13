package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.esb.enums.TenantId;
import com.somle.esb.handler.DingtalkUserHandler;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SyncUserJob extends DataJob {
    public static final Long ADMIN_USER_ID = 50001L;
    @Autowired
    DingtalkUserHandler dingtalkUserHandler;

    @Override
    public String execute(String param) throws Exception {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(ADMIN_USER_ID);
        loginUser.setUserType(UserTypeEnum.ADMIN.getValue());
        // 设置用户信息
        SecurityFrameworkUtils.setLoginUser(loginUser);
        try {
            TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
            dingtalkUserHandler.syncUsers();
            return "sync users success";
        } finally {
            TenantContextHolder.clear();
        }
    }
}