package org.springframework.ai.models.midjourney.api.req;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * author: fansili
 * time: 2024/4/6 21:33
 */
@Data
@Accessors(chain = true)
public class ReRollReq {

    /**
     * socket 消息里面收到的 messageId
     */
    private String messageId;
    /**
     * socket 消息里面的，操作按钮id（MJ::JOB::upsample::3::2aeefbef-43e2-4057-bcf1-43b5f39ab6f7）
     */
    private String customId;
}
