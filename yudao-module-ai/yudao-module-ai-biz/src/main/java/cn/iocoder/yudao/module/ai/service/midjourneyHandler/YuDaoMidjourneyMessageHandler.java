package cn.iocoder.yudao.module.ai.service.midjourneyHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyGennerateStatusEnum;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MidjourneyMessageHandler;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyOperationsVO;
import cn.iocoder.yudao.module.ai.convert.AiImageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiImageDrawingStatusEnum;
import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        // 根据 Embeds 来判断是否异常
        if (CollUtil.isEmpty(midjourneyMessage.getEmbeds())) {
            successHandler(midjourneyMessage);
        } else {
            errorHandler(midjourneyMessage);
        }
    }

    private void errorHandler(MidjourneyMessage midjourneyMessage) {
        // image 编号
        Long nonceId = Long.valueOf(midjourneyMessage.getNonce());
        // 获取 error message
        String errorMessage = getErrorMessage(midjourneyMessage);
        aiImageMapper.updateByMjNonce(nonceId,
                new AiImageDO()
                        .setDrawingErrorMessage(errorMessage)
                        .setDrawingStatus(AiImageDrawingStatusEnum.FAIL.getStatus())
        );
    }

    private String getErrorMessage(MidjourneyMessage midjourneyMessage) {
        StringBuilder errorMessage = new StringBuilder();
        for (MidjourneyMessage.Embed embed : midjourneyMessage.getEmbeds()) {
            errorMessage.append(embed.getDescription());
        }
        return errorMessage.toString();
    }

    private void successHandler(MidjourneyMessage midjourneyMessage) {
        // 获取id
        Long nonceId = Long.valueOf(midjourneyMessage.getNonce());
        // 获取生成 url
        String imageUrl = null;
        if (CollUtil.isNotEmpty(midjourneyMessage.getAttachments())) {
            imageUrl = midjourneyMessage.getAttachments().get(0).getUrl();
        }
        // 转换状态
        AiImageDrawingStatusEnum drawingStatusEnum = null;
        String generateStatus = midjourneyMessage.getGenerateStatus();
        if (MidjourneyGennerateStatusEnum.COMPLETED.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiImageDrawingStatusEnum.COMPLETE;
        } else if (MidjourneyGennerateStatusEnum.IN_PROGRESS.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiImageDrawingStatusEnum.IN_PROGRESS;
        } else if (MidjourneyGennerateStatusEnum.WAITING.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiImageDrawingStatusEnum.WAITING;
        }
        // 获取 midjourneyOperations
        List<AiImageMidjourneyOperationsVO> midjourneyOperations = getMidjourneyOperationsList(midjourneyMessage);
        // 更新数据库
        aiImageMapper.updateByMjNonce(nonceId,
                new AiImageDO()
                        .setDrawingImageUrl(imageUrl)
                        .setDrawingStatus(drawingStatusEnum == null ? null : drawingStatusEnum.getStatus())
                        .setMjNonceId(midjourneyMessage.getId())
                        .setMjOperations(JsonUtils.toJsonString(midjourneyOperations))
        );
    }

    private List<AiImageMidjourneyOperationsVO> getMidjourneyOperationsList(MidjourneyMessage midjourneyMessage) {
        // 为空直接返回
        if (CollUtil.isEmpty(midjourneyMessage.getComponents())) {
            return Collections.emptyList();
        }
        // 将 component 转成 AiImageMidjourneyOperationsVO
        return midjourneyMessage.getComponents().stream()
                .map(componentType -> componentType.getComponents().stream()
                        .map(AiImageConvert.INSTANCE::convertAiImageMidjourneyOperationsVO)
                        .collect(Collectors.toList()))
                .toList().stream().flatMap(List::stream).toList();
    }
}
