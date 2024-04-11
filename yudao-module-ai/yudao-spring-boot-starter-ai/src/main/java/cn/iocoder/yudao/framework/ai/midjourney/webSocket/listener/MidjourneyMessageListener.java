package cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyConstants;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyGennerateStatusEnum;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyMessageTypeEnum;
import cn.iocoder.yudao.framework.ai.midjourney.util.MidjourneyUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.List;

@Slf4j
public class MidjourneyMessageListener {

    private MidjourneyConfig midjourneyConfig;

    public MidjourneyMessageListener(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
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

        // 转换几个重要的信息
        MidjourneyMessage mjMessage = new MidjourneyMessage();
        mjMessage.setId(data.getString(MidjourneyConstants.MSG_ID));
        mjMessage.setType(data.getInt(MidjourneyConstants.MSG_TYPE));
        mjMessage.setRawData(StrUtil.str(raw.toJson(), "UTF-8"));
		mjMessage.setContent(MidjourneyUtil.parseContent(data.getString(MidjourneyConstants.MSG_CONTENT)));
        // 转换 components
        if (!data.getArray(MidjourneyConstants.MSG_COMPONENTS).isEmpty()) {
            String componentsJson = StrUtil.str(data.getArray(MidjourneyConstants.MSG_COMPONENTS).toJson(), "UTF-8");
            List<MidjourneyMessage.ComponentType> components = JSON.parseArray(componentsJson, MidjourneyMessage.ComponentType.class);
            mjMessage.setComponents(components);
        }
        // 转换附件
        if (!data.getArray(MidjourneyConstants.MSG_ATTACHMENTS).isEmpty()) {
            String attachmentsJson = StrUtil.str(data.getArray(MidjourneyConstants.MSG_ATTACHMENTS).toJson(), "UTF-8");
            List<MidjourneyMessage.Attachment> attachments = JSON.parseArray(attachmentsJson, MidjourneyMessage.Attachment.class);
            mjMessage.setAttachments(attachments);
        }
        // 转换状态
        convertGenerateStatus(mjMessage);
        //
        log.info("message 信息 {}", JSONUtil.toJsonPrettyStr(mjMessage));
        System.err.println(JSONUtil.toJsonPrettyStr(mjMessage));
    }

    private void convertGenerateStatus(MidjourneyMessage mjMessage) {
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
