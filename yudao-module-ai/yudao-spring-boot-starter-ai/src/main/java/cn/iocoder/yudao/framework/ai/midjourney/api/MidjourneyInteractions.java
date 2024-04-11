package cn.iocoder.yudao.framework.ai.midjourney.api;

import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyConstants;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;

// TODO @fansili：按照 spring ai 的封装习惯，这个类是不是 MidjourneyApi

/**
 * 图片生成
 *
 * author: fansili
 * time: 2024/4/3 17:36
 */
@Slf4j
public abstract class MidjourneyInteractions {

    // TODO done @fansili：静态变量，放在最前面哈；
    /**
     * header - referer 头信息
     */
    private static final String HEADER_REFERER = "https://discord.com/channels/%s/%s";
    /**
     * mj配置文件
     */
    protected final MidjourneyConfig midjourneyConfig;

    protected MidjourneyInteractions(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
    }

    /**
     * 获取headers - application json
     *
     * @return
     */
    protected HttpHeaders getHeadersOfAppJson() {
        // 设置header值
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", midjourneyConfig.getToken());
        httpHeaders.set("User-Agent", midjourneyConfig.getUserAage());
        httpHeaders.set("Cookie", MidjourneyConstants.HTTP_COOKIE);
        httpHeaders.set("Referer", String.format(HEADER_REFERER, midjourneyConfig.getGuildId(), midjourneyConfig.getChannelId()));
        return httpHeaders;
    }

    /**
     * 获取headers - http form data
     *
     * @return
     */
    protected HttpHeaders getHeadersOfFormData() {
        // 设置header值
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        httpHeaders.set("Authorization", midjourneyConfig.getToken());
        httpHeaders.set("User-Agent", midjourneyConfig.getUserAage());
        httpHeaders.set("Cookie", MidjourneyConstants.HTTP_COOKIE);
        httpHeaders.set("Referer", String.format(HEADER_REFERER, midjourneyConfig.getGuildId(), midjourneyConfig.getChannelId()));
        return httpHeaders;
    }

    /**
     * 获取 - 默认参数
     * @return
     */
    protected HashMap<String, String> getDefaultParams() {
        HashMap<String, String> requestParams = Maps.newHashMap();
        // TODO @fansili：感觉参数的组装，可以搞成一个公用的方法；就是 config + 入参的感觉；
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId())); // TODO @fansili：建议用 uuid 之类的；nextId 跨进程未必合适哈；
        return requestParams;
    }
}
