package cn.iocoder.yudao.module.ai.service.model.tool;

import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * 工具：当前用户信息查询
 *
 * 同时，也是展示 ToolContext 上下文的使用
 *
 * @author Ren
 */
@Component("user_profile_query")
public class UserProfileQueryToolFunction
        implements BiFunction<UserProfileQueryToolFunction.Request, ToolContext, UserProfileQueryToolFunction.Response> {

    @Resource
    private AdminUserApi adminUserApi;

    @Data
    @JsonClassDescription("当前用户信息查询")
    public static class Request { }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {

        /**
         * 用户ID
         */
        private Long id;
        /**
         * 用户昵称
         */
        private String nickname;

        /**
         * 手机号码
         */
        private String mobile;
        /**
         * 用户头像
         */
        private String avatar;

    }

    @Override
    public Response apply(Request request, ToolContext toolContext) {
        LoginUser loginUser = (LoginUser) toolContext.getContext().get(AiUtils.TOOL_CONTEXT_LOGIN_USER);
        Long tenantId = (Long) toolContext.getContext().get(AiUtils.TOOL_CONTEXT_TENANT_ID);
        if (loginUser == null | tenantId == null) {
            return null;
        }
        return TenantUtils.execute(tenantId, () -> {
            AdminUserRespDTO user = adminUserApi.getUser(loginUser.getId());
            return BeanUtils.toBean(user, Response.class);
        });
    }

}
