package cn.iocoder.yudao.framework.tenant.core.mq.rocketmq;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.util.List;

import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.HEADER_TENANT_ID;

/**
 * RocketMQ 消息队列的多租户 {@link ConsumeMessageHook} 实现类
 *
 * Consumer 消费消息时，将消息的 Header 的租户编号，添加到 {@link TenantContextHolder} 中，通过 {@link InvocableHandlerMethod} 实现
 *
 * @author 芋道源码
 */
public class TenantRocketMQConsumeMessageHook implements ConsumeMessageHook {

    @Override
    public String hookName() {
        return getClass().getSimpleName();
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        // 校验，消息必须是单条，不然设置租户可能不正确
        List<MessageExt> messages = context.getMsgList();
        Assert.isTrue(messages.size() == 1, "消息条数({})不正确", messages.size());
        // 设置租户编号
        String tenantId = messages.get(0).getUserProperty(HEADER_TENANT_ID);
        if (StrUtil.isNotEmpty(tenantId)) {
            TenantContextHolder.setTenantId(Long.parseLong(tenantId));
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        TenantContextHolder.clear();
    }

}
