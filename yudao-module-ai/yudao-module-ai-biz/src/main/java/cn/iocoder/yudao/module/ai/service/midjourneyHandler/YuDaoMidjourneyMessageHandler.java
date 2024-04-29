package cn.iocoder.yudao.module.ai.service.midjourneyHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyGennerateStatusEnum;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MidjourneyMessageHandler;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiImageDO;
import cn.iocoder.yudao.module.ai.enums.AiChatDrawingStatusEnum;
import cn.iocoder.yudao.module.ai.mapper.AiImageMapper;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * yudao message handler
 *
 * @author fansili
 * @time 2024/4/29 14:34
 * @since 1.0
 */
@Component
@Slf4j
@AllArgsConstructor
public class YuDaoMidjourneyMessageHandler implements MidjourneyMessageHandler {

    private final AiImageMapper aiImageMapper;

    @Override
    public void messageHandler(MidjourneyMessage midjourneyMessage) {
        log.info("yudao-midjourney-midjourney-message-handler {}", JSON.toJSONString(midjourneyMessage));
        if (midjourneyMessage.getContent() != null) {
            log.info("进度id {} 状态 {} 进度 {}",
                    midjourneyMessage.getNonce(),
                    midjourneyMessage.getGenerateStatus(),
                    midjourneyMessage.getContent().getProgress());
        }
        //
        updateImage(midjourneyMessage);
    }

    private void updateImage(MidjourneyMessage midjourneyMessage) {
        // Nonce 不存在不更新
        if (StrUtil.isBlank(midjourneyMessage.getNonce())) {
            return;
        }
        // 获取id
        Long aiImageId = Long.valueOf(midjourneyMessage.getNonce());
        // 获取生成 url
        String imageUrl = null;
        if (CollUtil.isNotEmpty(midjourneyMessage.getAttachments())) {
            imageUrl = midjourneyMessage.getAttachments().get(0).getUrl();
        }
        // 转换状态
        AiChatDrawingStatusEnum drawingStatusEnum = null;
        String generateStatus = midjourneyMessage.getGenerateStatus();
        if (MidjourneyGennerateStatusEnum.COMPLETED.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiChatDrawingStatusEnum.COMPLETE;
        } else if (MidjourneyGennerateStatusEnum.IN_PROGRESS.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiChatDrawingStatusEnum.IN_PROGRESS;
        }  else if (MidjourneyGennerateStatusEnum.WAITING.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiChatDrawingStatusEnum.WAITING;
        }
        aiImageMapper.updateById(
                new AiImageDO()
                        .setId(aiImageId)
                        .setDrawingImageUrl(imageUrl)
                        .setDrawingStatus(drawingStatusEnum == null ? null : drawingStatusEnum.getStatus())
        );
    }
}
