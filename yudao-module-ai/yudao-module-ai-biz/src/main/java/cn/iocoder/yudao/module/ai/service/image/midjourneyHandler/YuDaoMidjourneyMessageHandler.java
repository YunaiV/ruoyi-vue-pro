package cn.iocoder.yudao.module.ai.service.image.midjourneyHandler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import org.springframework.ai.models.midjourney.MidjourneyMessage;
import org.springframework.ai.models.midjourney.constants.MidjourneyGennerateStatusEnum;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyMessageHandler;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyOperationsVO;
import cn.iocoder.yudao.module.ai.convert.AiImageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiImageStatusEnum;
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
                        .setErrorMessage(errorMessage)
                        .setStatus(AiImageStatusEnum.FAIL.getStatus())
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
        // TODO @芋艿 这个地方有问题，不能根据 nonce来更新，不返回这个信息(别人获取了 image-xxx-xx 后面一段hash，由于没有mj账号测试，暂不清楚。)
        // 获取生成 url
        String imageUrl = null;
        if (CollUtil.isNotEmpty(midjourneyMessage.getAttachments())) {
            imageUrl = midjourneyMessage.getAttachments().get(0).getUrl();
        }
        // 转换状态
        AiImageStatusEnum drawingStatusEnum = null;
        String generateStatus = midjourneyMessage.getGenerateStatus();
        if (MidjourneyGennerateStatusEnum.COMPLETED.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiImageStatusEnum.COMPLETE;
        } else if (MidjourneyGennerateStatusEnum.IN_PROGRESS.getStatus().equals(generateStatus)) {
            drawingStatusEnum = AiImageStatusEnum.IN_PROGRESS;
        }
//        else if (MidjourneyGennerateStatusEnum.WAITING.getStatus().equals(generateStatus)) {
//            drawingStatusEnum = AiImageStatusEnum.WAITING;
//        }
        // 获取 midjourneyOperations
        List<AiImageMidjourneyOperationsVO> midjourneyOperations = getMidjourneyOperationsList(midjourneyMessage);
        // 更新数据库
        aiImageMapper.updateByMjNonce(nonceId,
                new AiImageDO()
                        .setOriginalPicUrl(imageUrl)
                        .setStatus(drawingStatusEnum == null ? null : drawingStatusEnum.getStatus())
//                        .setMjNonceId(midjourneyMessage.getId())
//                        .setMjOperations(JsonUtils.toJsonString(midjourneyOperations))
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
