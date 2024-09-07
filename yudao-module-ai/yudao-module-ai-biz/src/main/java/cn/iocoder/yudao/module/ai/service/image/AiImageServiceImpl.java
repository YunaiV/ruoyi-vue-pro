package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.ai.core.enums.AiPlatformEnum;
import cn.iocoder.yudao.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.*;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.ai.zhipuai.ZhiPuAiImageOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

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
    private AiApiKeyService apiKeyService;

    @Override
    public PageResult<AiImageDO> getImagePageMy(Long userId, AiImagePageReqVO pageReqVO) {
        return imageMapper.selectPageMy(userId, pageReqVO);
    }

    @Override
    public PageResult<AiImageDO> getImagePagePublic(AiImagePublicPageReqVO pageReqVO) {
        return imageMapper.selectPage(pageReqVO);
    }

    @Override
    public AiImageDO getImage(Long id) {
        return imageMapper.selectById(id);
    }

    @Override
    public List<AiImageDO> getImageList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return imageMapper.selectBatchIds(ids);
    }

    @Override
    public Long drawImage(Long userId, AiImageDrawReqVO drawReqVO) {
        // 1. 保存数据库
        AiImageDO image = BeanUtils.toBean(drawReqVO, AiImageDO.class).setUserId(userId).setPublicStatus(false)
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus());
        imageMapper.insert(image);
        // 2. 异步绘制，后续前端通过返回的 id 进行轮询结果
        getSelf().executeDrawImage(image, drawReqVO);
        return image.getId();
    }

    @Async
    public void executeDrawImage(AiImageDO image, AiImageDrawReqVO req) {
        try {
            // 1.1 构建请求
            ImageOptions request = buildImageOptions(req);
            // 1.2 执行请求
            ImageModel imageModel = apiKeyService.getImageModel(AiPlatformEnum.validatePlatform(req.getPlatform()));
            ImageResponse response = imageModel.call(new ImagePrompt(req.getPrompt(), request));

            // 2. 上传到文件服务
            String b64Json = response.getResult().getOutput().getB64Json();
            byte[] fileContent = StrUtil.isNotEmpty(b64Json) ? Base64.decode(b64Json)
                    : HttpUtil.downloadBytes(response.getResult().getOutput().getUrl());
            String filePath = fileApi.createFile(fileContent);

            // 3. 更新数据库
            imageMapper.updateById(new AiImageDO().setId(image.getId()).setStatus(AiImageStatusEnum.SUCCESS.getStatus())
                    .setPicUrl(filePath).setFinishTime(LocalDateTime.now()));
        } catch (Exception ex) {
            log.error("[doDall][image({}) 生成异常]", image, ex);
            imageMapper.updateById(new AiImageDO().setId(image.getId())
                    .setStatus(AiImageStatusEnum.FAIL.getStatus())
                    .setErrorMessage(ex.getMessage()).setFinishTime(LocalDateTime.now()));
        }
    }

    private static ImageOptions buildImageOptions(AiImageDrawReqVO draw) {
        if (ObjUtil.equal(draw.getPlatform(), AiPlatformEnum.OPENAI.getPlatform())) {
            // https://platform.openai.com/docs/api-reference/images/create
            return OpenAiImageOptions.builder().withModel(draw.getModel())
                    .withHeight(draw.getHeight()).withWidth(draw.getWidth())
                    .withStyle(MapUtil.getStr(draw.getOptions(), "style")) // 风格
                    .withResponseFormat("b64_json")
                    .build();
        } else if (ObjUtil.equal(draw.getPlatform(), AiPlatformEnum.STABLE_DIFFUSION.getPlatform())) {
            // https://platform.stability.ai/docs/api-reference#tag/SDXL-and-SD1.6/operation/textToImage
            // https://platform.stability.ai/docs/api-reference#tag/Text-to-Image/operation/textToImage
            return StabilityAiImageOptions.builder().withModel(draw.getModel())
                    .withHeight(draw.getHeight()).withWidth(draw.getWidth())
                    .withSeed(Long.valueOf(draw.getOptions().get("seed")))
                    .withCfgScale(Float.valueOf(draw.getOptions().get("scale")))
                    .withSteps(Integer.valueOf(draw.getOptions().get("steps")))
                    .withSampler(String.valueOf(draw.getOptions().get("sampler")))
                    .withStylePreset(String.valueOf(draw.getOptions().get("stylePreset")))
                    .withClipGuidancePreset(String.valueOf(draw.getOptions().get("clipGuidancePreset")))
                    .build();
        } else if (ObjUtil.equal(draw.getPlatform(), AiPlatformEnum.TONG_YI.getPlatform())) {
            return TongYiImagesOptions.builder()
                    .withModel(draw.getModel()).withN(1)
                    .withHeight(draw.getHeight()).withWidth(draw.getWidth())
                    .build();
        } else if (ObjUtil.equal(draw.getPlatform(), AiPlatformEnum.YI_YAN.getPlatform())) {
            return QianFanImageOptions.builder()
                    .withModel(draw.getModel()).withN(1)
                    .withHeight(draw.getHeight()).withWidth(draw.getWidth())
                    .build();
        } else if (ObjUtil.equal(draw.getPlatform(), AiPlatformEnum.ZHI_PU.getPlatform())) {
            return ZhiPuAiImageOptions.builder()
                    .withModel(draw.getModel())
                    .build();
        }
        throw new IllegalArgumentException("不支持的 AI 平台：" + draw.getPlatform());
    }

    @Override
    public void deleteImageMy(Long id, Long userId) {
        // 1. 校验是否存在
        AiImageDO image = validateImageExists(id);
        if (ObjUtil.notEqual(image.getUserId(), userId)) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        // 2. 删除记录
        imageMapper.deleteById(id);
    }

    @Override
    public PageResult<AiImageDO> getImagePage(AiImagePageReqVO pageReqVO) {
        return imageMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateImage(AiImageUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validateImageExists(updateReqVO.getId());
        // 2. 更新发布状态
        imageMapper.updateById(BeanUtils.toBean(updateReqVO, AiImageDO.class));
    }

    @Override
    public void deleteImage(Long id) {
        // 1. 校验存在
        validateImageExists(id);
        // 2. 删除
        imageMapper.deleteById(id);
    }

    private AiImageDO validateImageExists(Long id) {
        AiImageDO image = imageMapper.selectById(id);
        if (image == null) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        return image;
    }

    // ================ midjourney 专属 ================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long midjourneyImagine(Long userId, AiMidjourneyImagineReqVO reqVO) {
        MidjourneyApi midjourneyApi = apiKeyService.getMidjourneyApi();
        // 1. 保存数据库
        AiImageDO image = BeanUtils.toBean(reqVO, AiImageDO.class).setUserId(userId).setPublicStatus(false)
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus())
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform());
        imageMapper.insert(image);

        // 2. 调用 Midjourney Proxy 提交任务
        List<String> base64Array = StrUtil.isBlank(reqVO.getReferImageUrl()) ? null :
                Collections.singletonList("data:image/jpeg;base64,".concat(Base64.encode(HttpUtil.downloadBytes(reqVO.getReferImageUrl()))));
        MidjourneyApi.ImagineRequest imagineRequest = new MidjourneyApi.ImagineRequest(
                base64Array, reqVO.getPrompt(),null,
                MidjourneyApi.ImagineRequest.buildState(reqVO.getWidth(),
                        reqVO.getHeight(), reqVO.getVersion(), reqVO.getModel()));
        MidjourneyApi.SubmitResponse imagineResponse = midjourneyApi.imagine(imagineRequest);

        // 3. 情况一【失败】：抛出业务异常
        if (!MidjourneyApi.SubmitCodeEnum.SUCCESS_CODES.contains(imagineResponse.code())) {
            String description = imagineResponse.description().contains("quota_not_enough") ?
                    "账户余额不足" : imagineResponse.description();
            throw exception(IMAGE_MIDJOURNEY_SUBMIT_FAIL, description);
        }

        // 4. 情况二【成功】：更新 taskId 和参数
        imageMapper.updateById(new AiImageDO().setId(image.getId())
                .setTaskId(imagineResponse.result()).setOptions(BeanUtil.beanToMap(reqVO)));
        return image.getId();
    }

    @Override
    public Integer midjourneySync() {
        MidjourneyApi midjourneyApi = apiKeyService.getMidjourneyApi();
        // 1.1 获取 Midjourney 平台，状态在 “进行中” 的 image
        List<AiImageDO> imageList = imageMapper.selectListByStatusAndPlatform(
                AiImageStatusEnum.IN_PROGRESS.getStatus(), AiPlatformEnum.MIDJOURNEY.getPlatform());
        if (CollUtil.isEmpty(imageList)) {
            return 0;
        }
        // 1.2 调用 Midjourney Proxy 获取任务进展
        List<MidjourneyApi.Notify> taskList = midjourneyApi.getTaskList(convertSet(imageList, AiImageDO::getTaskId));
        Map<String, MidjourneyApi.Notify> taskMap = convertMap(taskList, MidjourneyApi.Notify::id);

        // 2. 逐个处理，更新进展
        int count = 0;
        for (AiImageDO image : imageList) {
            MidjourneyApi.Notify notify = taskMap.get(image.getTaskId());
            if (notify == null) {
                log.error("[midjourneySync][image({}) 查询不到进展]", image);
                continue;
            }
            count++;
            updateMidjourneyStatus(image, notify);
        }
        return count;
    }

    @Override
    public void midjourneyNotify(MidjourneyApi.Notify notify) {
        // 1. 校验 image 存在
        AiImageDO image = imageMapper.selectByTaskId(notify.id());
        if (image == null) {
            log.warn("[midjourneyNotify][回调任务({}) 不存在]", notify.id());
            return;
        }
        // 2. 更新状态
        updateMidjourneyStatus(image, notify);
    }

    private void updateMidjourneyStatus(AiImageDO image, MidjourneyApi.Notify notify) {
        // 1. 转换状态
        Integer status = null;
        LocalDateTime finishTime = null;
        if (StrUtil.isNotBlank(notify.status())) {
            MidjourneyApi.TaskStatusEnum taskStatusEnum = MidjourneyApi.TaskStatusEnum.valueOf(notify.status());
            if (MidjourneyApi.TaskStatusEnum.SUCCESS == taskStatusEnum) {
                status = AiImageStatusEnum.SUCCESS.getStatus();
                finishTime = LocalDateTime.now();
            } else if (MidjourneyApi.TaskStatusEnum.FAILURE == taskStatusEnum) {
                status = AiImageStatusEnum.FAIL.getStatus();
                finishTime = LocalDateTime.now();
            }
        }

        // 2. 上传图片
        String picUrl = null;
        if (StrUtil.isNotBlank(notify.imageUrl())) {
            try {
                picUrl = fileApi.createFile(HttpUtil.downloadBytes(notify.imageUrl()));
            } catch (Exception e) {
                picUrl = notify.imageUrl();
                log.warn("[updateMidjourneyStatus][图片({}) 地址({}) 上传失败]", image.getId(), notify.imageUrl(), e);
            }
        }

        // 3. 更新 image 状态
        imageMapper.updateById(new AiImageDO().setId(image.getId()).setStatus(status)
                .setPicUrl(picUrl).setButtons(notify.buttons()).setErrorMessage(notify.failReason())
                .setFinishTime(finishTime));
    }

    @Override
    public Long midjourneyAction(Long userId, AiMidjourneyActionReqVO reqVO) {
        MidjourneyApi midjourneyApi = apiKeyService.getMidjourneyApi();
        // 1.1 检查 image
        AiImageDO image = validateImageExists(reqVO.getId());
        if (ObjUtil.notEqual(userId, image.getUserId())) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        // 1.2 检查 customId
        MidjourneyApi.Button button = CollUtil.findOne(image.getButtons(),
                buttonX -> buttonX.customId().equals(reqVO.getCustomId()));
        if (button == null) {
            throw exception(IMAGE_CUSTOM_ID_NOT_EXISTS);
        }

        // 2. 调用 Midjourney Proxy 提交任务
        MidjourneyApi.SubmitResponse actionResponse = midjourneyApi.action(
                new MidjourneyApi.ActionRequest(button.customId(), image.getTaskId(), null));
        if (!MidjourneyApi.SubmitCodeEnum.SUCCESS_CODES.contains(actionResponse.code())) {
            String description = actionResponse.description().contains("quota_not_enough") ?
                    "账户余额不足" : actionResponse.description();
            throw exception(IMAGE_MIDJOURNEY_SUBMIT_FAIL, description);
        }

        // 3. 新增 image 记录
        AiImageDO newImage = new AiImageDO().setUserId(image.getUserId()).setPublicStatus(false).setPrompt(image.getPrompt())
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus())
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform())
                .setModel(image.getModel()).setWidth(image.getWidth()).setHeight(image.getHeight())
                .setOptions(image.getOptions()).setTaskId(actionResponse.result());
        imageMapper.insert(newImage);
        return newImage.getId();
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
