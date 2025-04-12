package cn.iocoder.yudao.module.ai.service.model.tool;

import cn.iocoder.yudao.framework.ai.core.pojo.AiToolContext;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

/**
 * 工具：用户ID查询（上下文参数Demo）
 *
 * @author Ren
 */
@Component("userid_query")
public class UserIdQueryToolFunction
        implements BiFunction<UserIdQueryToolFunction.Request, ToolContext, UserIdQueryToolFunction.Response> {

    @Data
    @JsonClassDescription("用户ID查询")
    public static class Request { }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        /**
         * 用户ID
         */
        private Long UserId;

    }
    @Override
    public UserIdQueryToolFunction.Response apply(UserIdQueryToolFunction.Request request, ToolContext toolContext) {
        // 获取当前登录用户
        AiToolContext context = (AiToolContext) toolContext.getContext().get(AiToolContext.CONTEXT_KEY);

        return new Response(context.getUserId());
    }
}
