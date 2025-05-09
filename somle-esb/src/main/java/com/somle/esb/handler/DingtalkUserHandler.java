package com.somle.esb.handler;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserSaveReqDTO;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.enums.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * @Description: $
 * @Author: c-tao
 * @Date: 2025/1/13$
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class DingtalkUserHandler {

    public static final Long ADMIN_USER_ID = 50001L;
    private final DingTalkToErpConverter dingTalkToErpConverter;
    private final AdminUserApi adminUserApi;

    @ServiceActivator(inputChannel = "dingtalkUserOutputChannel")
    public void syncUser(@Payload OapiV2UserGetResponse.UserGetResponse dingTalkUser) {

        LoginUser loginUser = new LoginUser();
        loginUser.setId(ADMIN_USER_ID);
        loginUser.setUserType(UserTypeEnum.ADMIN.getValue());
        // 设置用户信息
        SecurityFrameworkUtils.setLoginUser(loginUser);
        try {
            TenantContextHolder.setTenantId(TenantId.DEFAULT.getId());
            log.info("begin syncing: " + dingTalkUser.toString());
            AdminUserSaveReqDTO erpUser = dingTalkToErpConverter.toSaveReq(dingTalkUser);
            log.info("user to add " + erpUser);
            if (erpUser.getId() != null) {
                adminUserApi.updateUser(erpUser);
            } else {
                adminUserApi.createUser(erpUser);
            }
        } finally {
            TenantContextHolder.clear();
        }


    }
}
