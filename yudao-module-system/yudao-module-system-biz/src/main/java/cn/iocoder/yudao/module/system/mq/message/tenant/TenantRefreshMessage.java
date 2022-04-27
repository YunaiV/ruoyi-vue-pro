package cn.iocoder.yudao.module.system.mq.message.tenant;

import cn.iocoder.yudao.framework.mq.core.pubsub.AbstractChannelMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户数据刷新 Message
 *
 * @author 芋道源码
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantRefreshMessage extends AbstractChannelMessage {

    @Override
    public String getChannel() {
        return "system.tenant.refresh";
    }

}
