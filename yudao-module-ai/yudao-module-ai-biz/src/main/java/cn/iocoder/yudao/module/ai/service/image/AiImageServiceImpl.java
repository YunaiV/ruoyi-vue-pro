package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageStyleEnum;
import cn.iocoder.yudao.framework.ai.core.exception.AiException;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.AiCommonConstants;
import cn.iocoder.yudao.module.ai.ErrorCodeConstants;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiImagePublicStatusEnum;
import cn.iocoder.yudao.module.ai.enums.AiImageStatusEnum;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.models.midjourney.api.MidjourneyInteractionsApi;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyWebSocketStarter;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


// TODO @fan：注释优化下哈

/**
 * AI 绘画(接入 dall2/dall3、midjourney)
 *
 * @author fansili
 * @time 2024/4/25 15:51
 * @since 1.0
 */
@Service
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    // TODO @fan：使用 @Resource 注入

    // TODO @fan：imageMapper
    @Resource
    private AiImageMapper imageMapper;
    @Resource
    private FileApi fileApi;
    @Resource
    private OpenAiImageClient openAiImageClient;
    @Resource
    private MidjourneyWebSocketStarter midjourneyWebSocketStarter;
    @Resource
    private MidjourneyInteractionsApi midjourneyInteractionsApi;

    // TODO @fan：接 mj proxy
    @PostConstruct
    public void startMidjourney() {
        // todo @fan 暂时注释掉
//        log.info("midjourney web socket starter...");
//        midjourneyWebSocketStarter.start(new WssNotify() {
//            @Override
//            public void notify(int code, String message) {
//                log.info("code: {}, message: {}", code, message);
//                if (message.contains("Authentication failed")) {
//                    // TODO 芋艿，这里看怎么处理，token无效的时候会认证失败！
//                    // 认证失败
//                    log.error("midjourney socket 认证失败，检查token是否失效!");
//                }
//            }
//        });
    }

    @Override
    public PageResult<AiImageDO> getImagePageMy(Long loginUserId, AiImageListReqVO req) {
        // 查询当前用户下所有的绘画记录
        return imageMapper.selectPage(req,
                new LambdaQueryWrapperX<AiImageDO>()
                        .eq(AiImageDO::getUserId, loginUserId)
                        .orderByDesc(AiImageDO::getId));
    }

    @Override
    public AiImageDO getMy(Long id) {
        return imageMapper.selectById(id);
    }

    @Override
    public Long dall(Long loginUserId, AiImageDallReqVO req) {
        // 保存数据库
        AiImageDO aiImageDO = BeanUtils.toBean(req, AiImageDO.class)
                .setUserId(loginUserId)
                .setWidth(req.getWidth())
                .setHeight(req.getHeight())
                .setDrawRequest(ImmutableMap.of(AiCommonConstants.DRAW_REQ_KEY_STYLE, req.getStyle()))
                .setPublicStatus(AiImagePublicStatusEnum.PRIVATE.getStatus())
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus());
        imageMapper.insert(aiImageDO);
        // 异步执行
        doDall(aiImageDO, req);
        // 转换 AiImageDallDrawingRespVO
        return aiImageDO.getId();
    }

    @Async
    public void doDall(AiImageDO aiImageDO, AiImageDallReqVO req) {
        try {
            // 获取 model
            OpenAiImageModelEnum openAiImageModelEnum = OpenAiImageModelEnum.valueOfModel(req.getModel());
            OpenAiImageStyleEnum openAiImageStyleEnum = OpenAiImageStyleEnum.valueOfStyle(req.getStyle());

            // 转换openai 参数
            // TODO @fan：需要考虑，不同平台，参数不同；
            OpenAiImageOptions openAiImageOptions = new OpenAiImageOptions();
            openAiImageOptions.setModel(openAiImageModelEnum.getModel());
            openAiImageOptions.setStyle(openAiImageStyleEnum.getStyle());
            openAiImageOptions.setSize(String.format(AiCommonConstants.DALL_SIZE_TEMPLATE, req.getWidth(), req.getHeight()));
            ImageResponse imageResponse = openAiImageClient.call(new ImagePrompt(req.getPrompt(), openAiImageOptions));
            // 发送
            ImageGeneration imageGeneration = imageResponse.getResult();
            // 图片保存到服务器
            String filePath = fileApi.createFile(HttpUtil.downloadBytes(imageGeneration.getOutput().getUrl()));
            // 更新数据库
            imageMapper.updateById(new AiImageDO().setId(aiImageDO.getId()).setStatus(AiImageStatusEnum.COMPLETE.getStatus())
                    .setPicUrl(filePath).setOriginalPicUrl(imageGeneration.getOutput().getUrl()));
        } catch (AiException aiException) {
            // TODO @fan：错误日志，也打印下哈；因为 aiException.getMessage() 比较精简；
            imageMapper.updateById(new AiImageDO().setId(aiImageDO.getId()).setStatus(AiImageStatusEnum.FAIL.getStatus())
                    .setErrorMessage(aiException.getMessage()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void midjourney(AiImageMidjourneyReqVO req) {
        // 保存数据库
        String messageId = String.valueOf(IdUtil.getSnowflakeNextId());
        // todo
//        AiImageDO aiImageDO = doSave(req.getPrompt(), null, "midjoureny",
//                null, null, AiImageStatusEnum.SUBMIT, null,
//                messageId, null, null);
        // 提交 midjourney 任务
        Boolean imagine = midjourneyInteractionsApi.imagine(messageId, req.getPrompt());
        if (!imagine) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void midjourneyOperate(AiImageMidjourneyOperateReqVO req) {
//        // 校验是否存在
//        AiImageDO aiImageDO = validateExists(req.getId());
//        // 获取 midjourneyOperations
//        List<AiImageMidjourneyOperationsVO> midjourneyOperations = getMidjourneyOperations(aiImageDO);
//        // 校验 OperateId 是否存在
//        AiImageMidjourneyOperationsVO midjourneyOperationsVO = validateMidjourneyOperationsExists(midjourneyOperations, req.getOperateId());
//        // 校验 messageId
//        validateMessageId(aiImageDO.getMjNonceId(), req.getMessageId());
//        // 获取 mjOperationName
//        String mjOperationName = midjourneyOperationsVO.getLabel();
//        // 保存一个 image 任务记录
//        // todo
////        doSave(aiImageDO.getPrompt(), aiImageDO.getSize(), aiImageDO.getModel(),
////                null, null, AiImageStatusEnum.SUBMIT, null,
////                req.getMessageId(), req.getOperateId(), mjOperationName);
//        // 提交操作
//        midjourneyInteractionsApi.reRoll(
//                new ReRollReq()
//                        .setCustomId(req.getOperateId())
//                        .setMessageId(req.getMessageId())
//        );
    }

    @Override
    public Boolean myDelete(Long id, Long userId) {
        // 校验是否存在，并获取 image
        AiImageDO image = validateExists(id);
        // 是否属于当前用户
        if (!image.getUserId().equals(userId)) {
            throw exception(ErrorCodeConstants.AI_IMAGE_NOT_EXISTS);
        }
        // 删除记录
        return imageMapper.deleteById(id) > 0;
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
//        if (StrUtil.isBlank(aiImageDO.getMjOperations())) {
//            return Collections.emptyList();
//        }
//        return JsonUtils.parseArray(aiImageDO.getMjOperations(), AiImageMidjourneyOperationsVO.class);
        return null;
    }

    private AiImageDO validateExists(Long id) {
        AiImageDO aiImageDO = imageMapper.selectById(id);
        if (aiImageDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
        return aiImageDO;
    }
}
