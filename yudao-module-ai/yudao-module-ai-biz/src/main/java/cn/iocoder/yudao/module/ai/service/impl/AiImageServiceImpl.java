package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.ai.exception.AiException;
import cn.iocoder.yudao.framework.ai.image.ImageGeneration;
import cn.iocoder.yudao.framework.ai.image.ImagePrompt;
import cn.iocoder.yudao.framework.ai.image.ImageResponse;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageClient;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageOptions;
import cn.iocoder.yudao.framework.ai.imageopenai.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.imageopenai.enums.OpenAiImageStyleEnum;
import cn.iocoder.yudao.framework.ai.midjourney.api.MidjourneyInteractionsApi;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.MidjourneyWebSocketStarter;
import cn.iocoder.yudao.framework.ai.midjourney.webSocket.WssNotify;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyReqVO;
import cn.iocoder.yudao.module.ai.convert.AiImageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiChatDrawingStatusEnum;
import cn.iocoder.yudao.module.ai.service.AiImageService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ai 作图
 *
 * @author fansili
 * @time 2024/4/25 15:51
 * @since 1.0
 */
@AllArgsConstructor
@Service
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    private final AiImageMapper aiImageMapper;
    private final OpenAiImageClient openAiImageClient;
    private final MidjourneyWebSocketStarter midjourneyWebSocketStarter;
    private final MidjourneyInteractionsApi midjourneyInteractionsApi;

    @PostConstruct
    public void startMidjourney() {
        log.info("midjourney web socket starter...");
        midjourneyWebSocketStarter.start(new WssNotify() {
            @Override
            public void notify(int code, String message) {
                log.info("code: {}, message: {}", code, message);
                if (message.contains("Authentication failed")) {
                    // TODO 芋艿，这里看怎么处理，token无效的时候会认证失败！
                    // 认证失败
                    log.error("midjourney socket 认证失败，检查token是否失效!");
                }
            }
        });
    }

    @Override
    public AiImageDallDrawingRespVO dallDrawing(AiImageDallDrawingReqVO req) {
        // 获取 model
        OpenAiImageModelEnum openAiImageModelEnum = OpenAiImageModelEnum.valueOfModel(req.getModal());
        OpenAiImageStyleEnum openAiImageStyleEnum = OpenAiImageStyleEnum.valueOfStyle(req.getStyle());
        // 转换 AiImageDallDrawingRespVO
        AiImageDallDrawingRespVO respVO = AiImageConvert.INSTANCE.convertAiImageDallDrawingRespVO(req);
        try {
            // 转换openai 参数
            OpenAiImageOptions openAiImageOptions = new OpenAiImageOptions();
            openAiImageOptions.setModel(openAiImageModelEnum);
            openAiImageOptions.setStyle(openAiImageStyleEnum);
            openAiImageOptions.setSize(req.getSize());
            ImageResponse imageResponse = openAiImageClient.call(new ImagePrompt(req.getPrompt(), openAiImageOptions));
            // 发送
            ImageGeneration imageGeneration = imageResponse.getResult();
            // 保存数据库
            doSave(req.getPrompt(), req.getSize(), req.getModal(),
                    imageGeneration.getOutput().getUrl(), AiChatDrawingStatusEnum.COMPLETE, null);
            // 返回 flex
            respVO.setUrl(imageGeneration.getOutput().getUrl());
            respVO.setBase64(imageGeneration.getOutput().getB64Json());
            return respVO;
        } catch (AiException aiException) {
            // 保存数据库
            doSave(req.getPrompt(), req.getSize(), req.getModal(),
                    null, AiChatDrawingStatusEnum.FAIL, aiException.getMessage());
            // 发送错误信息
            respVO.setErrorMessage(aiException.getMessage());
            return respVO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void midjourney(AiImageMidjourneyReqVO req) {
        // 保存数据库
        AiImageDO aiImageDO = doSave(req.getPrompt(), null, "midjoureny",
                null, AiChatDrawingStatusEnum.SUBMIT, null);
        // 提交 midjourney 任务
        Boolean imagine = midjourneyInteractionsApi.imagine(aiImageDO.getId(), req.getPrompt());
        if (!imagine) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
    }

//    private static void sendSseEmitter(Utf8SseEmitter sseEmitter, Object object) {
//        try {
//            sseEmitter.send(object, MediaType.APPLICATION_JSON);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            // 发送 complete
//            sseEmitter.complete();
//        }
//    }

    private AiImageDO doSave(String prompt,
                        String size,
                        String model,
                        String imageUrl,
                        AiChatDrawingStatusEnum drawingStatusEnum,
                        String drawingError) {
        // 保存数据库
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AiImageDO aiImageDO = new AiImageDO();
        aiImageDO.setId(null);
        aiImageDO.setPrompt(prompt);
        aiImageDO.setSize(size);
        aiImageDO.setModal(model);
        aiImageDO.setUserId(loginUserId);
        aiImageDO.setDrawingImageUrl(imageUrl);
        aiImageDO.setDrawingStatus(drawingStatusEnum.getStatus());
        aiImageDO.setDrawingError(drawingError);
        aiImageMapper.insert(aiImageDO);
        return aiImageDO;
    }
}
