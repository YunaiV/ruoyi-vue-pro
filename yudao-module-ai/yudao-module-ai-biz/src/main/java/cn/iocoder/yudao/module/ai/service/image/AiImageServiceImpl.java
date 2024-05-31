package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageModelEnum;
import cn.iocoder.yudao.framework.ai.core.enums.OpenAiImageStyleEnum;
import cn.iocoder.yudao.framework.ai.core.exception.AiException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.AiCommonConstants;
import cn.iocoder.yudao.module.ai.client.MidjourneyProxyClient;
import cn.iocoder.yudao.module.ai.client.enums.MidjourneyModelEnum;
import cn.iocoder.yudao.module.ai.client.enums.MidjourneySubmitCodeEnum;
import cn.iocoder.yudao.module.ai.client.enums.MidjourneyTaskStatusEnum;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneyNotifyReqVO;
import cn.iocoder.yudao.module.ai.client.vo.MidjourneySubmitRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.AiImagePublicStatusEnum;
import cn.iocoder.yudao.module.ai.enums.AiImageStatusEnum;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageClient;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;


import static cn.iocoder.yudao.module.ai.ErrorCodeConstants.AI_IMAGE_NOT_EXISTS;

/**
 * AI 绘画 Service 实现类
 *
 * @author fansili
 */
@Service
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    @Resource
    private AiImageMapper imageMapper;

    @Resource
    private FileApi fileApi;

    @Resource
    private OpenAiImageClient openAiImageClient;

    @Autowired
    private MidjourneyProxyClient midjourneyProxyClient;

    @Value("${ai.midjourney-proxy.notifyUrl:http://127.0.0.1:48080/admin-api/ai/image/midjourney-notify}")
    private String midjourneyNotifyUrl;

    @Override
    public PageResult<AiImageDO> getImagePageMy(Long userId, PageParam pageReqVO) {
        return imageMapper.selectPage(userId, pageReqVO);
    }

    @Override
    public AiImageDO getImage(Long id) {
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
        getSelf().doDall(aiImageDO, req);
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
    public Long midjourneyImagine(Long loginUserId, AiImageMidjourneyImagineReqVO req) {

        // 1、构建 AiImageDO
        AiImageDO aiImageDO = new AiImageDO();
        aiImageDO.setId(null);
        aiImageDO.setUserId(loginUserId);
        aiImageDO.setPrompt(req.getPrompt());
        aiImageDO.setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform());
        // todo @范 平台需要转换(mj 模型一般分版本)
        aiImageDO.setModel(null);
        aiImageDO.setWidth(null);
        aiImageDO.setHeight(null);
        aiImageDO.setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus());
        aiImageDO.setPublicStatus(AiImagePublicStatusEnum.PRIVATE.getStatus());
        aiImageDO.setPicUrl(null);
        aiImageDO.setOriginalPicUrl(null);
        aiImageDO.setDrawRequest(null);
        aiImageDO.setDrawResponse(null);
        aiImageDO.setErrorMessage(null);

        // 2、保存 image
        imageMapper.insert(aiImageDO);

        // 3、调用 MidjourneyProxy 提交任务
        MidjourneyImagineReqVO imagineReqVO = BeanUtils.toBean(req, MidjourneyImagineReqVO.class);
        imagineReqVO.setNotifyHook(midjourneyNotifyUrl);
        // 设置 midjourney 扩展参数
        //  --ar 来设置尺寸
        String midjourneySizeParam = String.format(" --ar %s:%s ", req.getWidth(), req.getHeight());
        // --v 版本
        String midjourneyVersionParam = String.format(" --v %s ", req.getVersion());
        // --niji 模型
        MidjourneyModelEnum midjourneyModelEnum = MidjourneyModelEnum.valueOfModel(req.getModel());
        String midjourneyNijiParam = MidjourneyModelEnum.NIJI == midjourneyModelEnum ? " --niji " : "";
        // 设置参数
        imagineReqVO.setState(midjourneySizeParam.concat(midjourneyVersionParam).concat(midjourneyNijiParam));
        MidjourneySubmitRespVO submitRespVO = midjourneyProxyClient.imagine(imagineReqVO);

        // 4、保存任务 id (状态码: 1(提交成功), 21(已存在), 22(排队中), other(错误))
        String updateStatus = null;
        String errorMessage = null;

        if (!MidjourneySubmitCodeEnum.SUCCESS_CODES.contains(submitRespVO.getCode())) {
            updateStatus = AiImageStatusEnum.FAIL.getStatus();
            errorMessage = submitRespVO.getDescription();
        }
        imageMapper.updateById(new AiImageDO()
                .setId(aiImageDO.getId())
                .setStatus(updateStatus)
                .setErrorMessage(errorMessage)
                .setJobId(submitRespVO.getResult())
        );
        return aiImageDO.getId();
    }

    @Override
    public void deleteImageMy(Long id, Long userId) {
        // 1. 校验是否存在
        AiImageDO image = validateImageExists(id);
        if (ObjUtil.notEqual(image.getUserId(), userId)) {
            throw exception(AI_IMAGE_NOT_EXISTS);
        }
        // 删除记录
        imageMapper.deleteById(id);
    }

    @Override
    public Boolean midjourneyNotify(Long loginUserId, MidjourneyNotifyReqVO notifyReqVO) {
        // 1、根据 job id 查询关联的 image
        AiImageDO image = imageMapper.selectByJobId(notifyReqVO.getId());
        if (image == null) {
            log.warn("midjourneyNotify 回调的 jobId 不存在! jobId: {}", notifyReqVO.getId());
            return false;
        }
        //
        String imageStatus = null;
        if (MidjourneyTaskStatusEnum.SUCCESS == notifyReqVO.getStatus()) {
            imageStatus = AiImageStatusEnum.COMPLETE.getStatus();
        } else if (MidjourneyTaskStatusEnum.FAILURE == notifyReqVO.getStatus()) {
            imageStatus = AiImageStatusEnum.FAIL.getStatus();
        }
        // 2、上传图片
        String filePath = null;
        if (!StrUtil.isBlank(notifyReqVO.getImageUrl())) {
            filePath = fileApi.createFile(HttpUtil.downloadBytes(notifyReqVO.getImageUrl()));
        }
        // 2、更新 image 状态
        imageMapper.updateById(
                new AiImageDO()
                        .setId(image.getId())
                        .setStatus(imageStatus)
                        .setPicUrl(filePath)
                        .setOriginalPicUrl(notifyReqVO.getImageUrl())
                        .setDrawResponse(BeanUtil.beanToMap(notifyReqVO))
        );
        return true;
    }

    private AiImageDO validateImageExists(Long id) {
        AiImageDO image = imageMapper.selectById(id);
        if (image == null) {
            throw exception(AI_IMAGE_NOT_EXISTS);
        }
        return image;
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private AiImageServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
