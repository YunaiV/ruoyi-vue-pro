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
public class MidjourneyConfig {

    /**
     * keys
     */
    private String key;
    /**
     * 请求地址
     */
    private String url;
    /**
     * 通知回调地址
     */
    private String notifyUrl;
}
