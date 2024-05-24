package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.ai.core.exception.AiException;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageStyleEnum;
import org.springframework.ai.models.midjourney.api.MidjourneyInteractionsApi;
import org.springframework.ai.models.midjourney.api.req.ReRollReq;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyWebSocketStarter;
import org.springframework.ai.models.midjourney.webSocket.WssNotify;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.convert.AiImageConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiImageDrawingStatusEnum;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

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
    public PageResult<AiImageListRespVO> list(AiImageListReqVO req) {
        // 获取登录用户
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 查询当前用户下所有的绘画记录
        PageResult<AiImageDO> pageResult = aiImageMapper.selectPage(req,
                new LambdaQueryWrapperX<AiImageDO>()
                        .eq(AiImageDO::getUserId, loginUserId)
                        .orderByDesc(AiImageDO::getId)
        );
        // 转换 PageResult<AiImageListRespVO> 返回
        PageResult<AiImageListRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(AiImageConvert.INSTANCE.convertAiImageListRespVO(pageResult.getList()));
        return result;
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
            openAiImageOptions.setModel(openAiImageModelEnum.getModel());
            openAiImageOptions.setStyle(openAiImageStyleEnum.getStyle());
            openAiImageOptions.setSize(req.getSize());
            ImageResponse imageResponse = openAiImageClient.call(new ImagePrompt(req.getPrompt(), openAiImageOptions));
            // 发送
            ImageGeneration imageGeneration = imageResponse.getResult();
            // 保存数据库
            doSave(req.getPrompt(), req.getSize(), req.getModal(),
                    imageGeneration.getOutput().getUrl(), AiImageDrawingStatusEnum.COMPLETE, null,
                    null, null, null);
            // 返回 flex
            respVO.setUrl(imageGeneration.getOutput().getUrl());
            respVO.setBase64(imageGeneration.getOutput().getB64Json());
            return respVO;
        } catch (AiException aiException) {
            // 保存数据库
            doSave(req.getPrompt(), req.getSize(), req.getModal(),
                    null, AiImageDrawingStatusEnum.FAIL, aiException.getMessage(),
                    null, null, null);
            // 发送错误信息
            respVO.setErrorMessage(aiException.getMessage());
            return respVO;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void midjourney(AiImageMidjourneyReqVO req) {
        // 保存数据库
        String messageId = String.valueOf(IdUtil.getSnowflakeNextId());
        AiImageDO aiImageDO = doSave(req.getPrompt(), null, "midjoureny",
                null, AiImageDrawingStatusEnum.SUBMIT, null,
                messageId, null, null);
        // 提交 midjourney 任务
        Boolean imagine = midjourneyInteractionsApi.imagine(messageId, req.getPrompt());
        if (!imagine) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void midjourneyOperate(AiImageMidjourneyOperateReqVO req) {
        // 校验是否存在
        AiImageDO aiImageDO = validateExists(req.getId());
        // 获取 midjourneyOperations
        List<AiImageMidjourneyOperationsVO> midjourneyOperations = getMidjourneyOperations(aiImageDO);
        // 校验 OperateId 是否存在
        AiImageMidjourneyOperationsVO midjourneyOperationsVO = validateMidjourneyOperationsExists(midjourneyOperations, req.getOperateId());
        // 校验 messageId
        validateMessageId(aiImageDO.getMjNonceId(), req.getMessageId());
        // 获取 mjOperationName
        String mjOperationName = midjourneyOperationsVO.getLabel();
        // 保存一个 image 任务记录
        doSave(aiImageDO.getPrompt(), aiImageDO.getSize(), aiImageDO.getModal(),
                null, AiImageDrawingStatusEnum.SUBMIT, null,
                req.getMessageId(), req.getOperateId(), mjOperationName);
        // 提交操作
        midjourneyInteractionsApi.reRoll(
                new ReRollReq()
                        .setCustomId(req.getOperateId())
                        .setMessageId(req.getMessageId())
        );
    }

    @Override
    public void delete(Long id) {
        // 校验记录是否存在
        AiImageDO aiImageDO = validateExists(id);
        // 删除记录
        aiImageMapper.deleteById(id);
    }

    private void validateMessageId(String mjMessageId, String messageId) {
        if (!mjMessageId.equals(messageId)) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_MESSAGE_ID_INCORRECT);
        }
    }

    private AiImageMidjourneyOperationsVO validateMidjourneyOperationsExists(List<AiImageMidjourneyOperationsVO> midjourneyOperations, String operateId) {
        for (AiImageMidjourneyOperationsVO midjourneyOperation : midjourneyOperations) {
            if (midjourneyOperation.getCustom_id().equals(operateId)) {
                return midjourneyOperation;
            }
        }
        throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_OPERATION_NOT_EXISTS);
    }


    private List<AiImageMidjourneyOperationsVO> getMidjourneyOperations(AiImageDO aiImageDO) {
        if (StrUtil.isBlank(aiImageDO.getMjOperations())) {
            return Collections.emptyList();
        }
        return JsonUtils.parseArray(aiImageDO.getMjOperations(), AiImageMidjourneyOperationsVO.class);
    }

    private AiImageDO validateExists(Long id) {
        AiImageDO aiImageDO = aiImageMapper.selectById(id);
        if (aiImageDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
        return aiImageDO;
    }

    private AiImageDO doSave(String prompt,
                             String size,
                             String model,
                             String drawingImageUrl,
                             AiImageDrawingStatusEnum drawingStatusEnum,
                             String drawingErrorMessage,
                             String mjMessageId,
                             String mjOperationId,
                             String mjOperationName) {
        // 保存数据库
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        AiImageDO aiImageDO = new AiImageDO();
        aiImageDO.setId(null);
        aiImageDO.setPrompt(prompt);
        aiImageDO.setSize(size);
        aiImageDO.setModal(model);
        aiImageDO.setUserId(loginUserId);
        // TODO @芋艿 如何上传到自己服务器
        aiImageDO.setImageUrl(null);
        aiImageDO.setDrawingStatus(drawingStatusEnum.getStatus());
        aiImageDO.setDrawingImageUrl(drawingImageUrl);
        aiImageDO.setDrawingErrorMessage(drawingErrorMessage);
        //
        aiImageDO.setMjNonceId(mjMessageId);
        aiImageDO.setMjOperationId(mjOperationId);
        aiImageDO.setMjOperationName(mjOperationName);
        aiImageMapper.insert(aiImageDO);
        return aiImageDO;
    }
}
