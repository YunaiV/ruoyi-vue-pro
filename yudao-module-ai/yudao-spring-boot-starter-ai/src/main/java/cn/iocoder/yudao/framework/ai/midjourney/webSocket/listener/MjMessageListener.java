package cn.iocoder.yudao.framework.ai.midjourney.webSocket.listener;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyConfig;
import cn.iocoder.yudao.framework.ai.midjourney.MjMessage;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjConstants;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjGennerateStatusEnum;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MjMessageTypeEnum;
import cn.iocoder.yudao.framework.ai.midjourney.util.MjUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.util.List;

@Slf4j
public class MjMessageListener {

    private MidjourneyConfig midjourneyConfig;

    public MjMessageListener(MidjourneyConfig midjourneyConfig) {
        this.midjourneyConfig = midjourneyConfig;
    }

    public void onMessage(DataObject raw) {
        MjMessageTypeEnum messageType = MjMessageTypeEnum.of(raw.getString("t"));
        if (messageType == null || MjMessageTypeEnum.DELETE == messageType) {
            return;
        }
        DataObject data = raw.getObject("d");
        if (ignoreAndLogMessage(data, messageType)) {
            return;
        }

        // 转换几个重要的信息
        MjMessage mjMessage = new MjMessage();
        mjMessage.setId(data.getString(MjConstants.MSG_ID));
        mjMessage.setType(data.getInt(MjConstants.MSG_TYPE));
        mjMessage.setRawData(StrUtil.str(raw.toJson(), "UTF-8"));
		mjMessage.setContent(MjUtil.parseContent(data.getString(MjConstants.MSG_CONTENT)));
        // 转换 components
        if (!data.getArray(MjConstants.MSG_COMPONENTS).isEmpty()) {
            String componentsJson = StrUtil.str(data.getArray(MjConstants.MSG_COMPONENTS).toJson(), "UTF-8");
            List<MjMessage.ComponentType> components = JSON.parseArray(componentsJson, MjMessage.ComponentType.class);
            mjMessage.setComponents(components);
        }
        // 转换附件
        if (!data.getArray(MjConstants.MSG_ATTACHMENTS).isEmpty()) {
            String attachmentsJson = StrUtil.str(data.getArray(MjConstants.MSG_ATTACHMENTS).toJson(), "UTF-8");
            List<MjMessage.Attachment> attachments = JSON.parseArray(attachmentsJson, MjMessage.Attachment.class);
            mjMessage.setAttachments(attachments);
        }
        // 转换状态
        convertGenerateStatus(mjMessage);
        //
        log.info("message 信息 {}", JSONUtil.toJsonPrettyStr(mjMessage));
        System.err.println(JSONUtil.toJsonPrettyStr(mjMessage));
    }

    private void convertGenerateStatus(MjMessage mjMessage) {
        if (mjMessage.getType() == 20 && mjMessage.getContent().getStatus().contains("Waiting")) {
            mjMessage.setGenerateStatus(MjGennerateStatusEnum.WAITING.getValue());
        } else if (mjMessage.getType() == 20 && !StrUtil.isBlank(mjMessage.getContent().getProgress())) {
            mjMessage.setGenerateStatus(MjGennerateStatusEnum.IN_PROGRESS.getValue());
        } else if (mjMessage.getType() == 0 && !CollUtil.isEmpty(mjMessage.getComponents())) {
            mjMessage.setGenerateStatus(MjGennerateStatusEnum.COMPLETED.getValue());
        }
    }

    private boolean ignoreAndLogMessage(DataObject data, MjMessageTypeEnum messageType) {
        String channelId = data.getString(MjConstants.MSG_CHANNEL_ID);
        if (!CharSequenceUtil.equals(channelId, midjourneyConfig.getChannelId())) {
            return true;
        }
        String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
        log.debug("{} - {} - {}: {}", midjourneyConfig.getChannelId(), messageType.name(), authorName, data.opt("content").orElse(""));
        return false;
    }
}
