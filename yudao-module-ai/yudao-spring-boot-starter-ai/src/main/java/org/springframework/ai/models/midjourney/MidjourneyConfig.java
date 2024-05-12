package org.springframework.ai.models.midjourney;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

/**
 * Midjourney 配置
 *
 * author: fansili
 * time: 2024/4/3 17:10
 */
@Data
@Accessors(chain = true)
public class MidjourneyConfig {

    /**
     * token信息
     *
     * tip: 登录discard F12找 messages 接口
     */
    private String token;
    /**
     * 服务器id
     */
    private String guildId;
    /**
     * 频道id
     */
    private String channelId;

    //
    // api 接口

    /**
     * 服务地址
     */
    private String serverUrl = "https://discord.com/";
    /**
     * 发送命令
     */
    private String apiInteractions = "api/v9/interactions";
    /**
     * 附件
     */
    private String apiAttachments = "/api/v9/channels/%s/attachments";
    /**
     * 文件上传
     */
    private String apiAttachmentsUpload = "https://discord-attachments-uploads-prd.storage.googleapis.com/";

    //
    // 浏览器配置

    private String userAage = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";

    //
    // 请求 json 文件


    private Map<String, String> requestTemplates;

    //
    //

    private String sessionId;

    public MidjourneyConfig(String token, String guildId, String channelId, Map<String, String> requestTemplates) {
        this.token = token;
        this.guildId = guildId;
        this.channelId = channelId;
        this.requestTemplates = requestTemplates;

        // 生成 session id
        sessionId = UUID.randomUUID().toString().replaceAll("-", "");
    }

}
