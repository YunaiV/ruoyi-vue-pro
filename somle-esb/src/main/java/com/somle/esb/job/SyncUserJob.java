package com.somle.esb.job;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import com.somle.esb.service.EsbService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static com.somle.esb.job.SyncDepartmentsJob.TENANT_ID_DEFAULT;

@Slf4j
@Component
public class SyncUserJob extends DataJob {
    public static final Long ADMIN_USER_ID = 50001L;
    @Autowired
    EsbService service;

    @Override
    public String execute(String param) throws Exception {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(ADMIN_USER_ID);
        loginUser.setUserType(UserTypeEnum.ADMIN.getValue());
        // 设置用户信息
        SecurityFrameworkUtils.setLoginUser(loginUser);
        try {
            TenantContextHolder.setTenantId(TENANT_ID_DEFAULT);
            service.syncUsers();
            return "sync users success";
        } finally {
            TenantContextHolder.clear();
        }
    }
}