package com.somle.esb.handler;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.enums.TenantId;
import com.somle.esb.service.EsbMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class DingtalkUserHandler {

    @Autowired
    DingTalkToErpConverter dingTalkToErpConverter;

    @Autowired
    private AdminUserApi adminUserApi;

    @Autowired
    private EsbMappingService mappingService;

    public static final Long ADMIN_USER_ID = 50001L;

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
            AdminUserReqDTO erpUser = dingTalkToErpConverter.toErp(dingTalkUser);
            log.info("user to add " + erpUser);
            //获取钉钉中的昵称
            String nickname = erpUser.getNickname();
            //根据昵称自动生成用户名
            erpUser.setUsername(dingTalkToErpConverter.generateUserName(nickname));
            if (erpUser.getId() != null) {
                adminUserApi.updateUser(erpUser);
            } else {
                Long userId = adminUserApi.createUser(erpUser);
                var mapping = mappingService.toMapping(dingTalkUser);
                mapping
                    .setInternalId(userId);
                mappingService.save(mapping);
            }
        } finally {
            TenantContextHolder.clear();
        }


    }
}
