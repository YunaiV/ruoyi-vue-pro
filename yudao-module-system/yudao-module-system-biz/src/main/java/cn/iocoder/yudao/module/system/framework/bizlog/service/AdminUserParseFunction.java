package cn.iocoder.yudao.module.system.framework.bizlog.service;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.service.IParseFunction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义函数-通过用户编号获取用户信息
 *
 * @author HUIHUI
 */
@Slf4j
@RequiredArgsConstructor
public class AdminUserParseFunction implements IParseFunction {

    private final AdminUserApi adminUserApi;

    @Override
    public boolean executeBefore() {
        return true;
    }

    @Override
    public String functionName() {
        return "getAdminUserById";
    }

    @Override
    public String apply(Object value) {
        if (value == null) {
            log.warn("(getAdminUserById) 解析异常参数为 null");
            return "";
        }
        if (StrUtil.isEmpty(value.toString())) {
            log.warn("(getAdminUserById) 解析异常参数为空");
            return "";
        }

        // 获取用户信息
        AdminUserRespDTO user = adminUserApi.getUser(Long.parseLong(value.toString()));
        if (user == null) {
            log.warn("(getAdminUserById) 获取用户信息失败，参数为：{}", value);
            return "";
        }
        // 返回格式 芋道源码(13888888888)
        String nickname = user.getNickname();
        if (ObjUtil.isNotEmpty(user.getMobile())) {
            return nickname.concat("(").concat(user.getMobile()).concat(")");
        }
        return nickname;
    }
}
