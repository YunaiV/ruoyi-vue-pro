package cn.iocoder.yudao.framework.ai.midjourney.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * author: fansili
 * time: 2024/4/6 21:33
 */
@Data
@Accessors(chain = true)
public class ReRoll {

    private String messageId;
    private String customId;
}
