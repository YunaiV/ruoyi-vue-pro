package cn.iocoder.yudao.framework.ai.midjourney.interactions;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyInteractionsEnum;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 *
 *
 * author: fansili
 * time: 2024/4/3 17:36
 */
@Slf4j
public class MjImagineInteractions implements MjInteractions {

    private MidjourneyConfig midjourneyConfig;

    public MjImagineInteractions(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
    }

    @Override
    public List<MidjourneyInteractionsEnum> supperInteractions() {
        return null;
    }

    @Override
    public Boolean execute(String prompt) {
        String url = midjourneyConfig.getServerUrl().concat(midjourneyConfig.getApiInteractions());
        // 获取请求模板
        String requestTemplate = midjourneyConfig.getRequestTemplates().get("imagine");
        // 设置参数
        HashMap<String, String> requestParams = Maps.newHashMap();
        requestParams.put("guild_id", midjourneyConfig.getGuildId());
        requestParams.put("channel_id", midjourneyConfig.getChannelId());
        requestParams.put("session_id", UUID.randomUUID().toString().replaceAll("-", ""));
        requestParams.put("nonce", String.valueOf(IdUtil.getSnowflakeNextId()));
        requestParams.put("prompt", prompt);
        // 设置参数
        String requestBody = MjClient.setParams(requestTemplate, requestParams);
        // 发送请求
        String res = MjClient.post(url, midjourneyConfig.getToken(), requestBody);
        //
        System.err.println(res);
        log.info(res);
        // 这个 res 只要不返回值，就是成功!
        return StrUtil.isBlank(res);
    }
}
