package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageStyleEnum;
import cn.iocoder.yudao.framework.ai.core.exception.AiException;
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
import cn.iocoder.yudao.module.ai.enums.AiImageStatusEnum;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.models.midjourney.api.MidjourneyInteractionsApi;
import org.springframework.ai.models.midjourney.api.req.ReRollReq;
import org.springframework.ai.models.midjourney.webSocket.MidjourneyWebSocketStarter;
import org.springframework.ai.models.midjourney.webSocket.WssNotify;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


// TODO @fan：注释优化下哈
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

    // TODO @fan：使用 @Resource 注入

    // TODO @fan：imageMapper
    private final AiImageMapper aiImageMapper;

    private final FileApi fileApi;

    private final OpenAiImageClient openAiImageClient;

    private final MidjourneyWebSocketStarter midjourneyWebSocketStarter;

    private final MidjourneyInteractionsApi midjourneyInteractionsApi;

    private static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            3, 5, 1, TimeUnit.HOURS, new LinkedBlockingQueue<>(32));

    // TODO @fan：接 mj proxy
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

    // TODO @fan：1）分页，然后 loginUser 通过参数传入，这样 Service 无状态；2）另外，返回 DO；VO 的翻译，交给 Controller；3：还有，使用 BeanUtils 替代哈
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

    // TODO @fan：1）返回 DO；VO 的翻译，交给 Controller；2）还有，使用 BeanUtils 替代哈
    @Override
    public AiImageListRespVO getMy(Long id) {
        AiImageDO aiImageDO = aiImageMapper.selectById(id);
        return AiImageConvert.INSTANCE.convertAiImageListRespVO(aiImageDO);
    }

    // TODO @fan：1）loginUserId 通过 controller 传入；
    @Override
    public AiImageDallRespVO dall(AiImageDallReqVO req) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        // 保存数据库
        // TODO @fan：1）使用 BeanUtils；2）使用链式调用哈；
        AiImageDO aiImageDO = AiImageConvert.INSTANCE.convertAiImageDO(req);
        aiImageDO.setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus());
        aiImageDO.setUserId(loginUserId);
        aiImageMapper.insert(aiImageDO);
        // 异步执行
        // TODO @fan：使用 @Async 去调用哈；
        EXECUTOR.execute(() -> {
            try {
                // 获取 model
                OpenAiImageModelEnum openAiImageModelEnum = OpenAiImageModelEnum.valueOfModel(req.getModel());
                OpenAiImageStyleEnum openAiImageStyleEnum = OpenAiImageStyleEnum.valueOfStyle(req.getStyle());

                // 转换openai 参数
                // TODO @fan：需要考虑，不同平台，参数不同；
                OpenAiImageOptions openAiImageOptions = new OpenAiImageOptions();
                openAiImageOptions.setModel(openAiImageModelEnum.getModel());
                openAiImageOptions.setStyle(openAiImageStyleEnum.getStyle());
                openAiImageOptions.setSize(req.getSize());
                ImageResponse imageResponse = openAiImageClient.call(new ImagePrompt(req.getPrompt(), openAiImageOptions));
                // 发送
                ImageGeneration imageGeneration = imageResponse.getResult();
                // 图片保存到服务器
                String filePath = fileApi.createFile(HttpUtil.downloadBytes(imageGeneration.getOutput().getUrl()));
                // 更新数据库
                aiImageMapper.updateById(new AiImageDO().setId(aiImageDO.getId()).setStatus(AiImageStatusEnum.COMPLETE.getStatus())
                        .setPicUrl(filePath).setOriginalPicUrl(imageGeneration.getOutput().getUrl()));
            } catch (AiException aiException) {
                // TODO @fan：错误日志，也打印下哈；因为 aiException.getMessage() 比较精简；
                aiImageMapper.updateById(new AiImageDO().setId(aiImageDO.getId()).setStatus(AiImageStatusEnum.FAIL.getStatus())
                        .setErrorMessage(aiException.getMessage()));
            }
        });
        // TODO @fan：返回 id 就可以啦
        // 转换 AiImageDallDrawingRespVO
        return AiImageConvert.INSTANCE.convertAiImageDallDrawingRespVO(aiImageDO);
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

    // TODO @fan：1）需要校验存在；2）需要校验属于我；
    @Override
    public void deleteMy(Long id, Long loginUserId) {
        // 校验记录是否存在
        AiImageDO aiImageDO = validateExists(id);
        if (!aiImageDO.getUserId().equals(loginUserId)) {
            throw exception(ErrorCodeConstants.AI_IMAGE_NOT_CREATE_USER);
        }
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
//        if (StrUtil.isBlank(aiImageDO.getMjOperations())) {
//            return Collections.emptyList();
//        }
//        return JsonUtils.parseArray(aiImageDO.getMjOperations(), AiImageMidjourneyOperationsVO.class);
        return null;
    }

    private AiImageDO validateExists(Long id) {
        AiImageDO aiImageDO = aiImageMapper.selectById(id);
        if (aiImageDO == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.AI_MIDJOURNEY_IMAGINE_FAIL);
        }
        return aiImageDO;
    }
}
