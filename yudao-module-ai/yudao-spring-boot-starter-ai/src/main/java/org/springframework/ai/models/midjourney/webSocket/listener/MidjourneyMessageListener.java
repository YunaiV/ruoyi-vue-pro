package org.springframework.ai.models.midjourney.webSocket.listener;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.ai.models.midjourney.MidjourneyConfig;
import org.springframework.ai.models.midjourney.MidjourneyMessage;
import org.springframework.ai.models.midjourney.constants.MidjourneyConstants;
import org.springframework.ai.models.midjourney.constants.MidjourneyGennerateStatusEnum;
import org.springframework.ai.models.midjourney.constants.MidjourneyMessageTypeEnum;
import org.springframework.ai.models.midjourney.util.MidjourneyUtil;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyMessageHandler;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.List;

@Slf4j
public class MidjourneyMessageListener {

    private MidjourneyConfig midjourneyConfig;
    private MidjourneyMessageHandler midjourneyMessageHandler = null;

    public MidjourneyMessageListener(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
    }

    public MidjourneyMessageListener(MidjourneyConfig midjourneyConfig,
                                     MidjourneyMessageHandler midjourneyMessageHandler) {
        this.midjourneyConfig = midjourneyConfig;
        this.midjourneyMessageHandler = midjourneyMessageHandler;
    }

    public void onMessage(DataObject raw) {
        MidjourneyMessageTypeEnum messageType = MidjourneyMessageTypeEnum.of(raw.getString("t"));
        if (messageType == null || MidjourneyMessageTypeEnum.DELETE == messageType) {
            return;
        }
        DataObject data = raw.getObject("d");
        if (ignoreAndLogMessage(data, messageType)) {
            return;
        }
        log.info("socket message: {}", raw);
        // 转换几个重要的信息
        MidjourneyMessage mjMessage = new MidjourneyMessage();
        mjMessage.setId(getString(data, MidjourneyConstants.MSG_ID, ""));
        mjMessage.setNonce(getString(data, MidjourneyConstants.MSG_NONCE, ""));
        mjMessage.setType(data.getInt(MidjourneyConstants.MSG_TYPE));
        mjMessage.setRawData(StrUtil.str(raw.toJson(), "UTF-8"));
        mjMessage.setContent(MidjourneyUtil.parseContent(data.getString(MidjourneyConstants.MSG_CONTENT)));
        // 转换 components
        if (!data.getArray(MidjourneyConstants.MSG_COMPONENTS).isEmpty()) {
            String componentsJson = StrUtil.str(data.getArray(MidjourneyConstants.MSG_COMPONENTS).toJson(), "UTF-8");
            List<MidjourneyMessage.ComponentType> components = JsonUtils.parseArray(componentsJson, MidjourneyMessage.ComponentType.class);
            mjMessage.setComponents(components);
        }
        // 转换附件
        if (!data.getArray(MidjourneyConstants.MSG_ATTACHMENTS).isEmpty()) {
            String attachmentsJson = StrUtil.str(data.getArray(MidjourneyConstants.MSG_ATTACHMENTS).toJson(), "UTF-8");
            List<MidjourneyMessage.Attachment> attachments = JsonUtils.parseArray(attachmentsJson, MidjourneyMessage.Attachment.class);
            mjMessage.setAttachments(attachments);
        }
        // 转换 embeds 提示信息
        if (!data.getArray(MidjourneyConstants.MSG_EMBEDS).isEmpty()) {
            String embedJson = StrUtil.str(data.getArray(MidjourneyConstants.MSG_EMBEDS).toJson(), "UTF-8");
            List<MidjourneyMessage.Embed> embeds = JsonUtils.parseArray(embedJson, MidjourneyMessage.Embed.class);
            mjMessage.setEmbeds(embeds);
        }
        // 转换状态
        convertGenerateStatus(mjMessage);
        // message handler 调用
        if (midjourneyMessageHandler != null) {
            midjourneyMessageHandler.messageHandler(mjMessage);
        }
    }

    private String getString(DataObject data, String key, String defaultValue) {
        if (!data.hasKey(key)) {
            return defaultValue;
        }
        return data.getString(key);
    }

    private void convertGenerateStatus(MidjourneyMessage mjMessage) {
        //
        // tip：提示、警告、异常 content是没有内容的
        // tip: 一般错误信息在 Embeds 只要 Embeds有值，content就没信息。
        if (CollUtil.isNotEmpty(mjMessage.getEmbeds())) {
            return;
        }
        if (mjMessage.getType() == 20 && mjMessage.getContent().getStatus().contains("Waiting")) {
            mjMessage.setGenerateStatus(MidjourneyGennerateStatusEnum.WAITING.getStatus());
        } else if (mjMessage.getType() == 20 && !StrUtil.isBlank(mjMessage.getContent().getProgress())) {
            mjMessage.setGenerateStatus(MidjourneyGennerateStatusEnum.IN_PROGRESS.getStatus());
        } else if (mjMessage.getType() == 0 && !CollUtil.isEmpty(mjMessage.getComponents())) {
            mjMessage.setGenerateStatus(MidjourneyGennerateStatusEnum.COMPLETED.getStatus());
        }
    }

    private boolean ignoreAndLogMessage(DataObject data, MidjourneyMessageTypeEnum messageType) {
        String channelId = data.getString(MidjourneyConstants.MSG_CHANNEL_ID);
        if (!CharSequenceUtil.equals(channelId, midjourneyConfig.getChannelId())) {
            return true;
        }
        String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
        log.debug("{} - {} - {}: {}", midjourneyConfig.getChannelId(), messageType.name(), authorName, data.opt("content").orElse(""));
        return false;
    }
}
