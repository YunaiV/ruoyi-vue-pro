package cn.iocoder.yudao.module.visualization.config;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.api.oauth2.OAuth2TokenApi;
import cn.iocoder.yudao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.jeecg.modules.jmreport.api.JmReportTokenServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JimuReportTokenService implements JmReportTokenServiceI {
    @Autowired
    private OAuth2TokenApi oauth2TokenApi;

    @Autowired
    private AdminUserService adminUserService;

    @Override
    public String getUsername(String token) {
        if (StrUtil.isNotEmpty(token)) {
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
            if (accessToken != null) {
                Long userId = accessToken.getUserId();
                System.out.println(userId);
                AdminUserDO user = adminUserService.getUser(userId);
                if (user != null) {
                    return user.getUsername();
                }
            }
        }
        return null;
    }

    @Override
    public Boolean verifyToken(String token) {
        if (StrUtil.isNotEmpty(token)) {
            OAuth2AccessTokenCheckRespDTO accessToken = oauth2TokenApi.checkAccessToken(token);
            return accessToken != null;
        }
        return false;
    }
}