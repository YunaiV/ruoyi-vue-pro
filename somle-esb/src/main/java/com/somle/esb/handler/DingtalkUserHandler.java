package com.somle.esb.handler;

import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserReqDTO;
import com.somle.dingtalk.service.DingTalkService;
import com.somle.esb.converter.DingTalkToErpConverter;
import com.somle.esb.service.EsbMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    DingTalkService dingTalkService;

    @Autowired
    DingTalkToErpConverter dingTalkToErpConverter;

    @Autowired
    private AdminUserApi adminUserApi;

    @Autowired
    private EsbMappingService mappingService;

    public void syncUsers() {
        dingTalkService.getUserDetailStream().forEach(dingTalkUser -> {
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
        });
    }
}
