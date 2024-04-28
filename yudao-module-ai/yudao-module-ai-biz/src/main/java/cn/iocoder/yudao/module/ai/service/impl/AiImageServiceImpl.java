package cn.iocoder.yudao.module.ai.service.impl;

import cn.iocoder.yudao.framework.ai.image.Image;
import cn.iocoder.yudao.framework.ai.image.ImageGeneration;
import cn.iocoder.yudao.framework.ai.image.ImagePrompt;
import cn.iocoder.yudao.framework.ai.image.ImageResponse;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageClient;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.imageopenai.OpenAiImageOptions;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.controller.Utf8SseEmitter;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiImageDO;
import cn.iocoder.yudao.module.ai.mapper.AiImageMapper;
import cn.iocoder.yudao.module.ai.service.AiImageService;
import cn.iocoder.yudao.module.ai.vo.AiImageDallDrawingReq;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;

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

    @Override
    public void dallDrawing(AiImageDallDrawingReq req, Utf8SseEmitter sseEmitter) {
        // 获取 model
        OpenAiImageModelEnum openAiImageModelEnum = OpenAiImageModelEnum.valueOfModel(req.getModal());
        //
        OpenAiImageOptions openAiImageOptions = new OpenAiImageOptions();
        openAiImageOptions.setModel(openAiImageModelEnum);
        ImageResponse imageResponse = openAiImageClient.call(new ImagePrompt(req.getPrompt(), openAiImageOptions));
        // 发送
        ImageGeneration imageGeneration = imageResponse.getResult();
        try {
            sseEmitter.send(imageGeneration, MediaType.APPLICATION_JSON);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 发送 complete
            sseEmitter.complete();
        }
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        //
        // 保存数据库
        Image output = imageGeneration.getOutput();
        String imageUrl = output.getUrl();
        AiImageDO aiImageDO = new AiImageDO();
        aiImageDO.setId(null);
        aiImageDO.setPrompt(req.getPrompt());
        aiImageDO.setSize(req.getSize());
        aiImageDO.setModal(req.getModal());
        aiImageDO.setUserId(loginUserId);
        aiImageDO.setDrawingImageUrl(imageUrl);
        aiImageMapper.insert(aiImageDO);
    }
}
