package cn.iocoder.yudao.module.ai.tool.function;

import cn.iocoder.yudao.module.ai.util.AiUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.security.core.LoginUser;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * 工具：用户信息查询
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
    @JsonClassDescription("用户信息查询")
    public static class Request {

        /**
         * 用户编号
         */
        @JsonProperty(value = "id")
        @JsonPropertyDescription("用户编号，例如说：1。如果查询自己，则 id 为空")
        private Long id;

    }

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
        Long tenantId = (Long) toolContext.getContext().get(AiUtils.TOOL_CONTEXT_TENANT_ID);
        if (tenantId == null) {
            return new Response();
        }
        if (request.getId() == null) {
            LoginUser loginUser = (LoginUser) toolContext.getContext().get(AiUtils.TOOL_CONTEXT_LOGIN_USER);
            if (loginUser == null) {
                return new Response();
            }
            request.setId(loginUser.getId());
        }
        return TenantUtils.execute(tenantId, () -> {
            AdminUserRespDTO user = adminUserApi.getUser(request.getId());
            return BeanUtils.toBean(user, Response.class);
        });
    }

}
