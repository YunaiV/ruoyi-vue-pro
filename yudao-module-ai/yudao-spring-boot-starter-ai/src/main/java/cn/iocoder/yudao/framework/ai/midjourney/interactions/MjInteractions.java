package cn.iocoder.yudao.framework.ai.midjourney.interactions;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.vo.ReRoll;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;

/**
 * 图片生成
 *
 * author: fansili
 * time: 2024/4/3 17:36
 */
@Slf4j
public class MjInteractions {

    private MidjourneyConfig midjourneyConfig;

    private String url;

    public MjInteractions(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
        this.url = midjourneyConfig.getServerUrl().concat(midjourneyConfig.getApiInteractions());
    }

    public Boolean imagine(String prompt) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("imagine");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("prompt", prompt);
        // 设置参数
        String requestBody = MjClient.setParams(requestTemplate, requestParams);
        // 发送请求
        String res = MjClient.post(url, midjourneyConfig, requestBody);
        // 这个 res 只要不返回值，就是成功!
        boolean isSuccess = StrUtil.isBlank(res);
        if (isSuccess) {
            return true;
        }
        log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
        return isSuccess;
    }

    public Boolean reRoll(ReRoll reRoll) {
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("reroll");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", midjourneyConfig.getSessionId());
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("custom_id", reRoll.getCustomId());
        requestParams.put("message_id", reRoll.getMessageId());
        // 设置参数
        String requestBody = MjClient.setParams(requestTemplate, requestParams);
        // 发送请求
        String res = MjClient.post(url, midjourneyConfig, requestBody);
        // 这个 res 只要不返回值，就是成功!
        boolean isSuccess = StrUtil.isBlank(res);
        if (isSuccess) {
            return true;
        }
        log.error("请求失败! 请求参数：{} 返回结果! {}", requestBody, res);
        return isSuccess;
    }
}
