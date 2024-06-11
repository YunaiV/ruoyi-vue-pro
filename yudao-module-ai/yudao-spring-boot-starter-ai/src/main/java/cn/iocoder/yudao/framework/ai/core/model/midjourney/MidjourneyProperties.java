package cn.iocoder.yudao.framework.ai.core.model.midjourney;

import lombok.Data;

/**
 * Midjourney 属性
 *
 * @author fansili
 * @time 2024/6/5 15:02
 * @since 1.0
 */
@Data
public class MidjourneyProperties {

    private String key;
    private String url;
    private String notifyUrl;
}
