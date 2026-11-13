package cn.iocoder.yudao.module.ai.service.image;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageOptimizePromptReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImagePageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImagePublicPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageUpdateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyActionReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyBlendReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyImagineReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneyModalReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.midjourney.AiMidjourneySwapFaceReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.image.AiImageMapper;
import cn.iocoder.yudao.module.ai.enums.image.AiImageInputRoleEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageModeEnum;
import cn.iocoder.yudao.module.ai.enums.image.AiImageStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.midjourney.api.MidjourneyApi;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.service.ArkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

// TODO DONE @AI：service 里的代码，还是 jdk8 兼容哈；保持风格的一致性；（switch 表达式改为传统 switch）
/**
 * AI 绘画 Service 实现类
 *
 * @author fansili
 */
@Service
@Slf4j
public class AiImageServiceImpl implements AiImageService {

    /**
     * 超时时间：超过该时间仍在进行中的绘画，置为失败
     */
    static final Duration TIMEOUT = Duration.ofMinutes(10);

    /**
     * 生成专业绘画指令的系统提示词
     */
    static final String OPTIMIZE_PROMPT_SYSTEM = "你是专业的 AI 绘画提示词工程师。请把用户的描述改写为一段专业的绘画指令："
            + "补充主体细节、场景、构图、光线、色彩、风格与画质等关键信息；保持用户原意，不添加与原意冲突的内容；"
            + "使用与用户描述相同的语言；只输出改写后的提示词本身，不要解释、不要分点、不要加引号；不超过 500 字。";

    /**
     * Midjourney 局部重绘的中转流程
     */
    static final MidjourneyApi.ModalProtocolEnum MIDJOURNEY_MODAL_PROTOCOL = MidjourneyApi.ModalProtocolEnum.ACTION_THEN_MODAL;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private AiImageMapper imageMapper;

    @Resource
    private FileApi fileApi;

    @Override
    public PageResult<AiImageDO> getImagePageMy(Long userId, PageParam pageReqVO) {
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
    public AiImageDO getImagePublic(Long id) {
        AiImageDO image = imageMapper.selectById(id);
        if (image == null || !Boolean.TRUE.equals(image.getPublicStatus())
                || ObjUtil.notEqual(image.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())) {
            throw exception(IMAGE_PUBLIC_NOT_EXISTS);
        }
        return image;
    }

    @Override
    public List<AiImageDO> getImageList(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return imageMapper.selectByIds(ids);
    }

    @Override
    public List<AiImageDO> getImageListByParentId(Long parentId) {
        return imageMapper.selectListByParentId(parentId);
    }

    // ================ 通用模型、Seedream ================

    @Override
    public Long drawImage(Long userId, AiImageDrawReqVO drawReqVO) {
        // 1. 校验模型；获得客户端时，会校验模型与 API 密钥的平台一致
        AiModelDO model = modelService.validateModel(drawReqVO.getModelId());
        if (ObjUtil.equal(model.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())) {
            throw exception(IMAGE_MODEL_NOT_SUPPORTED, model.getName(), "Midjourney 模型请使用 Midjourney 生成");
        }
        boolean seedream = ObjUtil.equal(model.getPlatform(), AiPlatformEnum.BYTEDANCE.getPlatform());
        // 宽高：通用模型必填（按宽高请求）；Seedream 按尺寸档位请求，不需要
        if (!seedream && (drawReqVO.getWidth() == null || drawReqVO.getHeight() == null)) {
            throw exception(IMAGE_OPTION_INVALID, "width/height", "图片宽高不能为空");
        }
        ArkService arkService = seedream ? modelService.getArkService(model.getId()) : null;
        ImageModel imageModel = seedream ? null : modelService.getImageModel(model.getId());

        // 2. 保存数据库：请求参数逐个写入 Options，不整体复制
        AiImageDO image = new AiImageDO().setUserId(userId).setMode(AiImageModeEnum.DRAW.getMode())
                .setPlatform(model.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setApiKeyId(model.getKeyId())
                .setPrompt(drawReqVO.getPrompt()).setNegativePrompt(StrUtil.emptyToNull(drawReqVO.getNegativePrompt()))
                .setInputImages(buildInputImages(AiImageInputRoleEnum.REFERENCE, drawReqVO.getReferenceImageUrls()))
                .setPublicStatus(false).setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus()).setProgress(0);
        if (seedream) {
            // 宽高：生成前未知，记为 0（字段不允许为空）；成功后按实际的结果尺寸回写
            image.setWidth(0).setHeight(0)
                    .setSeedreamOptions(new AiImageDO.SeedreamOptions().setSize(drawReqVO.getSizeLevel())
                            .setOptimizePromptMode(AiImageUtils.SEEDREAM_OPTIMIZE_PROMPT_MODE)
                            .setWatermark(AiImageUtils.SEEDREAM_WATERMARK));
        } else {
            image.setWidth(drawReqVO.getWidth()).setHeight(drawReqVO.getHeight())
                    .setOptions(new AiImageDO.Options().setQuality(StrUtil.emptyToNull(drawReqVO.getQuality()))
                            .setAspectRatio(StrUtil.emptyToNull(drawReqVO.getAspectRatio()))
                            .setSeed(drawReqVO.getSeed()).setSteps(drawReqVO.getSteps()).setCfgScale(drawReqVO.getCfgScale())
                            .setSampler(StrUtil.emptyToNull(drawReqVO.getSampler()))
                            .setStylePreset(StrUtil.emptyToNull(drawReqVO.getStylePreset()))
                            .setClipGuidancePreset(StrUtil.emptyToNull(drawReqVO.getClipGuidancePreset())));
        }
        imageMapper.insert(image);

        // 3. 异步绘制，后续前端通过返回的 id 进行轮询结果
        if (seedream) {
            getSelf().executeSeedream(image, arkService);
        } else {
            getSelf().executeDrawImage(image, imageModel);
        }
        return image.getId();
    }

    @Async
    public void executeDrawImage(AiImageDO image, ImageModel imageModel) {
        try {
            // 1.1 构建请求
            ImageOptions request = AiImageUtils.buildImageOptions(image);
            // 1.2 执行请求
            ImageResponse response = imageModel.call(new ImagePrompt(image.getPrompt(), request));
            if (response.getResult() == null) {
                throw new IllegalArgumentException("生成结果为空");
            }

            // 2. 上传到文件服务
            String b64Json = response.getResult().getOutput().getB64Json();
            byte[] fileContent = StrUtil.isNotEmpty(b64Json) ? Base64.decode(b64Json)
                    : HttpUtil.downloadBytes(response.getResult().getOutput().getUrl());
            String filePath = fileApi.createFile(fileContent);

            // 3. 更新数据库：只更新进行中的记录（超时后不再覆盖）
            imageMapper.updateByIdAndStatus(image.getId(), AiImageStatusEnum.IN_PROGRESS.getStatus(), new AiImageDO()
                    .setStatus(AiImageStatusEnum.SUCCESS.getStatus()).setProgress(100)
                    .setPicUrl(filePath).setFinishTime(LocalDateTime.now()));
        } catch (Exception ex) {
            log.error("[executeDrawImage][image({}) 生成异常]", image, ex);
            updateImageFail(image.getId(), ex.getMessage(), null);
        }
    }

    @Async
    public void executeSeedream(AiImageDO image, ArkService arkService) {
        AiImageDO.SeedreamOptions options = ObjUtil.defaultIfNull(image.getSeedreamOptions(), new AiImageDO.SeedreamOptions());
        try {
            // 1. 调用火山方舟生成；用量写入元数据
            ImagesResponse response = arkService.generateImages(AiImageUtils.buildSeedreamRequest(image));
            if (response.getUsage() != null) {
                options.setUsage(JsonUtils.parseMap(JsonUtils.toJsonString(response.getUsage())));
            }

            // 2. 校验结果：返回错误、结果为空，都置为失败
            if (response.getError() != null) {
                options.setErrorCode(response.getError().getCode());
                updateImageFail(image.getId(), response.getError().getMessage(), options);
                return;
            }
            ImagesResponse.Image item = CollUtil.getFirst(response.getData());
            if (item == null || StrUtil.isEmpty(item.getUrl())) {
                updateImageFail(image.getId(), "生成结果为空", options);
                return;
            }

            // 3. 转存到文件服务：转存失败时置为失败，不回退到会过期的临时地址
            String picUrl;
            try {
                picUrl = fileApi.createFile(HttpUtil.downloadBytes(item.getUrl()));
            } catch (Exception ex) {
                log.error("[executeSeedream][image({}) 结果({}) 转存失败]", image.getId(), item.getUrl(), ex);
                updateImageFail(image.getId(), "结果转存失败：" + ex.getMessage(), options);
                return;
            }

            // 4. 更新为成功：宽高取实际的结果尺寸
            options.setResultSize(item.getSize());
            AiImageDO updateObj = new AiImageDO().setStatus(AiImageStatusEnum.SUCCESS.getStatus()).setProgress(100)
                    .setPicUrl(picUrl).setSeedreamOptions(options).setFinishTime(LocalDateTime.now());
            List<String> sizes = StrUtil.split(item.getSize(), 'x');
            if (sizes.size() == 2 && NumberUtil.isInteger(sizes.get(0)) && NumberUtil.isInteger(sizes.get(1))) {
                updateObj.setWidth(Integer.valueOf(sizes.get(0))).setHeight(Integer.valueOf(sizes.get(1)));
            }
            imageMapper.updateByIdAndStatus(image.getId(), AiImageStatusEnum.IN_PROGRESS.getStatus(), updateObj);
        } catch (Exception ex) {
            log.error("[executeSeedream][image({}) 生成异常]", image, ex);
            updateImageFail(image.getId(), ex.getMessage(), options);
        }
    }

    private void updateImageFail(Long id, String errorMessage, AiImageDO.SeedreamOptions seedreamOptions) {
        imageMapper.updateByIdAndStatus(id, AiImageStatusEnum.IN_PROGRESS.getStatus(), new AiImageDO()
                .setStatus(AiImageStatusEnum.FAIL.getStatus()).setErrorMessage(errorMessage)
                .setSeedreamOptions(seedreamOptions).setFinishTime(LocalDateTime.now()));
    }

    private static List<AiImageDO.InputImage> buildInputImages(AiImageInputRoleEnum role, List<String> urls) {
        return convertList(urls, url -> new AiImageDO.InputImage(role.getRole(), url));
    }

    // ================ 我的、公开、管理 ================

    @Override
    public void deleteImageMy(Long id, Long userId) {
        // 1. 校验是否存在
        validateImageMy(id, userId);
        // 2. 删除记录
        imageMapper.deleteById(id);
    }

    @Override
    public void updateImagePublicMy(Long userId, AiImageUpdateReqVO updateReqVO) {
        // 1. 校验存在
        AiImageDO image = validateImageMy(updateReqVO.getId(), userId);
        // 2. 更新公开状态
        updateImagePublicStatus(image, updateReqVO.getPublicStatus());
    }

    @Override
    public String optimizePrompt(Long userId, AiImageOptimizePromptReqVO reqVO) {
        // 1. 获得默认的对话模型
        AiModelDO model = modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        ChatModel chatModel = modelService.getChatModel(model.getId());
        ChatOptions options = AiChatUtils.buildChatOptions(AiPlatformEnum.validatePlatform(model.getPlatform()),
                model.getModel(), model.getTemperature(), model.getMaxTokens());
        Prompt prompt = new Prompt(Arrays.asList(new SystemMessage(OPTIMIZE_PROMPT_SYSTEM),
                new UserMessage(reqVO.getPrompt())), options);

        // 2. 同步生成
        String content;
        try {
            // TODO DONE @AI：分开，不要连续噢；先调用，产生一个变量；再继续；
            ChatResponse response = chatModel.call(prompt);
            content = AiChatUtils.getChatResponseContent(response);
        } catch (Exception ex) {
            log.error("[optimizePrompt][userId({}) prompt({}) 生成失败]", userId, reqVO.getPrompt(), ex);
            throw exception(IMAGE_OPTIMIZE_PROMPT_FAIL);
        }
        if (StrUtil.isBlank(content)) {
            throw exception(IMAGE_OPTIMIZE_PROMPT_FAIL);
        }
        return content.trim();
    }

    @Override
    public PageResult<AiImageDO> getImagePage(AiImagePageReqVO pageReqVO) {
        return imageMapper.selectPage(pageReqVO);
    }

    @Override
    public void updateImage(AiImageUpdateReqVO updateReqVO) {
        // 1. 校验存在
        AiImageDO image = validateImageExists(updateReqVO.getId());
        // 2. 更新公开状态
        updateImagePublicStatus(image, updateReqVO.getPublicStatus());
    }

    private void updateImagePublicStatus(AiImageDO image, Boolean publicStatus) {
        // 只有已完成的绘画，才能公开
        if (Boolean.TRUE.equals(publicStatus)
                && ObjUtil.notEqual(image.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())) {
            throw exception(IMAGE_PUBLIC_STATUS_FAIL);
        }
        imageMapper.updateById(new AiImageDO().setId(image.getId()).setPublicStatus(publicStatus));
    }

    @Override
    public void deleteImage(Long id) {
        // 1. 校验存在
        validateImageExists(id);
        // 2. 删除
        imageMapper.deleteById(id);
    }

    @Override
    public int timeoutImage() {
        // 1. 查询超时的绘画
        List<AiImageDO> images = imageMapper.selectListByStatusAndCreateTimeLt(
                AiImageStatusEnum.IN_PROGRESS.getStatus(), LocalDateTime.now().minus(TIMEOUT));
        // 2. 逐个置为失败：只更新进行中的记录；之后迟到的回调、同步，不再改变状态
        int count = 0;
        for (AiImageDO image : images) {
            count += imageMapper.updateByIdAndStatus(image.getId(), AiImageStatusEnum.IN_PROGRESS.getStatus(),
                    new AiImageDO().setStatus(AiImageStatusEnum.FAIL.getStatus())
                            .setErrorMessage(StrUtil.format("生成超时：超过 {} 分钟未完成", TIMEOUT.toMinutes()))
                            .setFinishTime(LocalDateTime.now()));
        }
        return count;
    }

    private AiImageDO validateImageExists(Long id) {
        AiImageDO image = imageMapper.selectById(id);
        if (image == null) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        return image;
    }

    private AiImageDO validateImageMy(Long id, Long userId) {
        AiImageDO image = validateImageExists(id);
        if (ObjUtil.notEqual(image.getUserId(), userId)) {
            throw exception(IMAGE_NOT_EXISTS);
        }
        return image;
    }

    // ================ midjourney 专属 ================

    @Override
    public Long midjourneyImagine(Long userId, AiMidjourneyImagineReqVO reqVO) {
        // 1.1 校验生成方式、输入图片：图生图 1-5 张参考图；一致性至少 1 张角色或风格参考；文生图没有参考图
        AiImageModeEnum mode = validateMidjourneyImagineMode(reqVO.getMode());
        int referenceCount = CollUtil.size(reqVO.getReferenceImageUrls());
        boolean consistencyImage = StrUtil.isNotEmpty(reqVO.getCrefUrl()) || StrUtil.isNotEmpty(reqVO.getSrefUrl());
        if (mode == AiImageModeEnum.MIDJOURNEY_IMAGE ? referenceCount < 1 : referenceCount > 0) {
            throw exception(IMAGE_REFERENCE_COUNT_EXCEED, mode == AiImageModeEnum.MIDJOURNEY_IMAGE ? "1-5" : "0", referenceCount);
        }
        if (mode == AiImageModeEnum.MIDJOURNEY_CONSISTENCY ? !consistencyImage : consistencyImage) {
            throw exception(IMAGE_OPTION_INVALID, "crefUrl/srefUrl", "只有一致性需要、且至少需要 1 张角色或风格参考");
        }
        // 1.2 校验模型，获得 MidjourneyApi
        AiModelDO model = validateMidjourneyModel(reqVO.getModelId());
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(model.getId());

        // 2. 保存数据库：请求参数逐个写入 midjourneyOptions；finalPrompt 由服务端拼接
        List<AiImageDO.InputImage> inputImages = new ArrayList<>(
                buildInputImages(AiImageInputRoleEnum.REFERENCE, reqVO.getReferenceImageUrls()));
        if (StrUtil.isNotEmpty(reqVO.getCrefUrl())) {
            inputImages.add(new AiImageDO.InputImage(AiImageInputRoleEnum.CREF.getRole(), reqVO.getCrefUrl()));
        }
        if (StrUtil.isNotEmpty(reqVO.getSrefUrl())) {
            inputImages.add(new AiImageDO.InputImage(AiImageInputRoleEnum.SREF.getRole(), reqVO.getSrefUrl()));
        }
        String finalPrompt = AiImageUtils.buildMidjourneyPrompt(reqVO);
        AiImageDO image = buildMidjourneyImage(userId, model, mode.getMode(), inputImages)
                .setPrompt(reqVO.getPrompt()).setNegativePrompt(StrUtil.emptyToNull(reqVO.getNegativePrompt()))
                .setMidjourneyOptions(new AiImageDO.MidjourneyOptions().setVersion(reqVO.getVersion())
                        .setAspectRatio(reqVO.getAspectRatio()).setQuality(reqVO.getQuality())
                        .setStylize(reqVO.getStylize()).setChaos(reqVO.getChaos())
                        .setSeed(reqVO.getSeed() != null && reqVO.getSeed() >= 0 ? reqVO.getSeed() : null)
                        .setTile(reqVO.getTile()).setRaw(reqVO.getRaw()).setHd(reqVO.getHd())
                        .setImageWeight(reqVO.getImageWeight()).setCharacterWeight(reqVO.getCharacterWeight())
                        .setStyleWeight(reqVO.getStyleWeight()).setStyleVersion(reqVO.getStyleVersion())
                        .setFinalPrompt(finalPrompt));
        setRatioSize(image, reqVO.getAspectRatio());
        imageMapper.insert(image);

        // 3. 提交到 Midjourney Proxy：图生图的参考图，全部提交
        submitMidjourney(image, () -> midjourneyApi.imagine(new MidjourneyApi.ImagineRequest(
                convertList(reqVO.getReferenceImageUrls(), AiImageUtils::toBase64DataUrl), finalPrompt, null, null)));
        return image.getId();
    }

    private static AiImageModeEnum validateMidjourneyImagineMode(Integer mode) {
        if (ObjUtil.equal(mode, AiImageModeEnum.MIDJOURNEY_IMAGINE.getMode())) {
            return AiImageModeEnum.MIDJOURNEY_IMAGINE;
        }
        if (ObjUtil.equal(mode, AiImageModeEnum.MIDJOURNEY_IMAGE.getMode())) {
            return AiImageModeEnum.MIDJOURNEY_IMAGE;
        }
        if (ObjUtil.equal(mode, AiImageModeEnum.MIDJOURNEY_CONSISTENCY.getMode())) {
            return AiImageModeEnum.MIDJOURNEY_CONSISTENCY;
        }
        throw exception(IMAGE_OPTION_INVALID, "mode", "只支持文生图、图生图、一致性");
    }

    // TODO DONE @AI：一些类似这种的方法，弄到 AiImageUtils 如何？其它类似也是的，这样 service 更聚焦一些逻辑；
    // （提示词拼接 buildMidjourneyPrompt、Seedream 请求 buildSeedreamRequest、融图比例 getDimensionsRatio、进度解析 parseProgress，已移到 AiImageUtils）

    @Override
    public Long midjourneyBlend(Long userId, AiMidjourneyBlendReqVO reqVO) {
        // 1. 校验模型，获得 MidjourneyApi
        AiModelDO model = validateMidjourneyModel(reqVO.getModelId());
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(model.getId());

        // 2. 保存数据库
        String aspectRatio = AiImageUtils.getDimensionsRatio(reqVO.getDimensions());
        AiImageDO image = buildMidjourneyImage(userId, model, AiImageModeEnum.MIDJOURNEY_BLEND.getMode(),
                buildInputImages(AiImageInputRoleEnum.BLEND, reqVO.getImageUrls()))
                .setPrompt("").setMidjourneyOptions(new AiImageDO.MidjourneyOptions()
                        .setDimensions(reqVO.getDimensions()).setAspectRatio(aspectRatio));
        setRatioSize(image, aspectRatio);
        imageMapper.insert(image);

        // 3. 提交到 Midjourney Proxy
        submitMidjourney(image, () -> midjourneyApi.blend(new MidjourneyApi.BlendRequest(
                convertList(reqVO.getImageUrls(), AiImageUtils::toBase64DataUrl), reqVO.getDimensions(), null, null)));
        return image.getId();
    }

    @Override
    public Long midjourneySwapFace(Long userId, AiMidjourneySwapFaceReqVO reqVO) {
        // 1. 校验模型，获得 MidjourneyApi
        AiModelDO model = validateMidjourneyModel(reqVO.getModelId());
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(model.getId());

        // 2. 保存数据库：输入图片的顺序为来源人脸 → 目标图
        List<AiImageDO.InputImage> inputImages = Arrays.asList(
                new AiImageDO.InputImage(AiImageInputRoleEnum.SWAP_SOURCE.getRole(), reqVO.getSourceUrl()),
                new AiImageDO.InputImage(AiImageInputRoleEnum.SWAP_TARGET.getRole(), reqVO.getTargetUrl()));
        AiImageDO image = buildMidjourneyImage(userId, model, AiImageModeEnum.MIDJOURNEY_SWAP_FACE.getMode(), inputImages)
                .setPrompt("").setMidjourneyOptions(new AiImageDO.MidjourneyOptions());
        imageMapper.insert(image);

        // 3. 提交到 Midjourney Proxy
        submitMidjourney(image, () -> midjourneyApi.swapFace(new MidjourneyApi.SwapFaceRequest(
                AiImageUtils.toBase64DataUrl(reqVO.getSourceUrl()), AiImageUtils.toBase64DataUrl(reqVO.getTargetUrl()), null, null)));
        return image.getId();
    }

    @Override
    public Long midjourneyAction(Long userId, AiMidjourneyActionReqVO reqVO) {
        // 1.1 校验来源记录
        AiImageDO source = validateMidjourneySource(reqVO.getId(), userId);
        // 1.2 校验按钮：必须是来源记录返回的按钮；CustomZoom 需要额外输入、局部重绘需要蒙版，不在这里执行
        MidjourneyApi.Button button = findButton(source, item -> StrUtil.equals(item.customId(), reqVO.getCustomId()));
        if (button == null) {
            throw exception(IMAGE_CUSTOM_ID_NOT_EXISTS, reqVO.getCustomId());
        }
        if (StrUtil.containsAny(button.customId(), "CustomZoom", "Inpaint")) {
            throw exception(IMAGE_MIDJOURNEY_ACTION_NOT_SUPPORTED, buildActionLabel(button));
        }
        // 1.3 沿用来源记录的 API 密钥
        Long apiKeyId = getMidjourneyApiKeyId(source);
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(apiKeyService.validateApiKey(apiKeyId));

        // 2. 保存子任务
        AiImageDO image = buildMidjourneyChildImage(source, apiKeyId, AiImageModeEnum.MIDJOURNEY_ACTION.getMode())
                .setPrompt(source.getPrompt()).setNegativePrompt(source.getNegativePrompt());
        image.getMidjourneyOptions().setCustomId(button.customId()).setActionLabel(buildActionLabel(button));
        imageMapper.insert(image);

        // 3. 提交到 Midjourney Proxy
        submitMidjourney(image, () -> midjourneyApi.action(
                new MidjourneyApi.ActionRequest(source.getTaskId(), button.customId(), null)));
        return image.getId();
    }

    @Override
    public Long midjourneyModal(Long userId, AiMidjourneyModalReqVO reqVO) {
        // 1.1 校验来源记录
        AiImageDO source = validateMidjourneySource(reqVO.getId(), userId);
        // 1.2 先执行按钮的流程：需要来源记录有局部重绘（Inpaint）按钮
        MidjourneyApi.ModalProtocolEnum protocol = MIDJOURNEY_MODAL_PROTOCOL;
        MidjourneyApi.Button button = null;
        if (protocol == MidjourneyApi.ModalProtocolEnum.ACTION_THEN_MODAL) {
            button = findButton(source, item -> StrUtil.contains(item.customId(), "Inpaint"));
            if (button == null) {
                throw exception(IMAGE_CUSTOM_ID_NOT_EXISTS, "Inpaint");
            }
        }
        // 1.3 沿用来源记录的 API 密钥
        Long apiKeyId = getMidjourneyApiKeyId(source);
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(apiKeyService.validateApiKey(apiKeyId));

        // 2. 保存子任务：modalProtocol 是提交时的流程快照
        String inpaintCustomId = button != null ? button.customId() : null;
        AiImageDO image = buildMidjourneyChildImage(source, apiKeyId, AiImageModeEnum.MIDJOURNEY_MODAL.getMode())
                .setPrompt(reqVO.getPrompt()).setInputImages(Collections.singletonList(
                        new AiImageDO.InputImage(AiImageInputRoleEnum.MASK.getRole(), reqVO.getMaskUrl())));
        image.getMidjourneyOptions().setCustomId(inpaintCustomId)
                .setActionLabel("局部重绘").setModalProtocol(protocol.name());
        imageMapper.insert(image);

        // 3. 提交到 Midjourney Proxy
        submitMidjourney(image, () -> {
            String maskBase64 = AiImageUtils.toBase64DataUrl(reqVO.getMaskUrl());
            String taskId = source.getTaskId();
            // 情况一：先执行局部重绘按钮（进入 MODAL 等待），再用新的任务编号提交蒙版
            if (protocol == MidjourneyApi.ModalProtocolEnum.ACTION_THEN_MODAL) {
                MidjourneyApi.SubmitResponse actionResponse = midjourneyApi.action(
                        new MidjourneyApi.ActionRequest(source.getTaskId(), inpaintCustomId, null));
                if (!MidjourneyApi.SubmitCodeEnum.SUCCESS_CODES.contains(actionResponse.code())) {
                    return actionResponse;
                }
                taskId = actionResponse.result();
            }
            // 情况二：直接用来源任务编号提交蒙版
            return midjourneyApi.modal(new MidjourneyApi.ModalRequest(taskId, reqVO.getPrompt(), maskBase64));
        });
        return image.getId();
    }

    private AiModelDO validateMidjourneyModel(Long modelId) {
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())) {
            throw exception(IMAGE_MODEL_NOT_SUPPORTED, model.getName(), "不是 Midjourney 模型");
        }
        return model;
    }

    private AiImageDO validateMidjourneySource(Long id, Long userId) {
        AiImageDO source = validateImageMy(id, userId);
        if (ObjUtil.notEqual(source.getPlatform(), AiPlatformEnum.MIDJOURNEY.getPlatform())
                || ObjUtil.notEqual(source.getStatus(), AiImageStatusEnum.SUCCESS.getStatus())
                || StrUtil.isEmpty(source.getTaskId())) {
            throw exception(IMAGE_MIDJOURNEY_SOURCE_INVALID);
        }
        return source;
    }

    private static MidjourneyApi.Button findButton(AiImageDO image, Predicate<MidjourneyApi.Button> predicate) {
        List<MidjourneyApi.Button> buttons = image.getMidjourneyOptions() != null
                ? image.getMidjourneyOptions().getButtons() : null;
        return CollUtil.findOne(buttons, predicate::test);
    }

    private static String buildActionLabel(MidjourneyApi.Button button) {
        return StrUtil.blankToDefault(StrUtil.blankToDefault(button.label(), button.emoji()), button.customId());
    }

    /**
     * 获得记录绑定的 API 密钥编号；历史记录没有绑定时，沿用模型当前的密钥
     */
    private Long getMidjourneyApiKeyId(AiImageDO image) {
        if (image.getApiKeyId() != null) {
            return image.getApiKeyId();
        }
        return modelService.validateModel(image.getModelId()).getKeyId();
    }

    private static AiImageDO buildMidjourneyImage(Long userId, AiModelDO model, Integer mode,
                                                  List<AiImageDO.InputImage> inputImages) {
        // 宽高：默认未知，记为 0（字段不允许为空）；有比例时由 setRatioSize 覆盖，换脸没有比例
        return new AiImageDO().setUserId(userId).setMode(mode)
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setModelId(model.getId()).setModel(model.getModel())
                .setApiKeyId(model.getKeyId()).setInputImages(inputImages).setWidth(0).setHeight(0).setPublicStatus(false)
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus()).setProgress(0);
    }

    /**
     * 构建按钮动作、局部重绘的子任务：记录来源编号，沿用来源的模型、密钥与请求参数
     */
    private static AiImageDO buildMidjourneyChildImage(AiImageDO source, Long apiKeyId, Integer mode) {
        AiImageDO.MidjourneyOptions sourceOptions = ObjUtil.defaultIfNull(source.getMidjourneyOptions(),
                new AiImageDO.MidjourneyOptions());
        AiImageDO.MidjourneyOptions options = new AiImageDO.MidjourneyOptions().setVersion(sourceOptions.getVersion())
                .setAspectRatio(sourceOptions.getAspectRatio()).setQuality(sourceOptions.getQuality())
                .setStylize(sourceOptions.getStylize()).setChaos(sourceOptions.getChaos()).setSeed(sourceOptions.getSeed())
                .setTile(sourceOptions.getTile()).setRaw(sourceOptions.getRaw()).setHd(sourceOptions.getHd())
                .setImageWeight(sourceOptions.getImageWeight()).setCharacterWeight(sourceOptions.getCharacterWeight())
                .setStyleWeight(sourceOptions.getStyleWeight()).setStyleVersion(sourceOptions.getStyleVersion())
                .setDimensions(sourceOptions.getDimensions());
        return new AiImageDO().setUserId(source.getUserId()).setMode(mode).setParentId(source.getId())
                .setPlatform(AiPlatformEnum.MIDJOURNEY.getPlatform()).setModelId(source.getModelId())
                .setModel(source.getModel()).setApiKeyId(apiKeyId)
                .setWidth(source.getWidth()).setHeight(source.getHeight())
                .setMidjourneyOptions(options).setPublicStatus(false)
                .setStatus(AiImageStatusEnum.IN_PROGRESS.getStatus()).setProgress(0);
    }

    private static void setRatioSize(AiImageDO image, String aspectRatio) {
        List<String> parts = StrUtil.split(aspectRatio, ':');
        if (parts.size() == 2 && NumberUtil.isInteger(parts.get(0)) && NumberUtil.isInteger(parts.get(1))) {
            image.setWidth(Integer.valueOf(parts.get(0))).setHeight(Integer.valueOf(parts.get(1)));
        }
    }

    /**
     * 提交到 Midjourney Proxy：受理（1 / 21 / 22）时记录任务编号；拒绝或异常时，记录置为失败
     *
     * 注意：不使用事务包住远程调用，提交失败也保留记录，便于用户看到失败原因
     */
    private void submitMidjourney(AiImageDO image, Supplier<MidjourneyApi.SubmitResponse> submit) {
        try {
            MidjourneyApi.SubmitResponse response = submit.get();
            if (!MidjourneyApi.SubmitCodeEnum.SUCCESS_CODES.contains(response.code())) {
                String description = StrUtil.contains(response.description(), "quota_not_enough")
                        ? "账户余额不足" : response.description();
                updateImageFail(image.getId(), StrUtil.format("Midjourney 提交失败：{}", description), null);
                return;
            }
            imageMapper.updateById(new AiImageDO().setId(image.getId()).setTaskId(response.result()));
        } catch (Exception ex) {
            log.error("[submitMidjourney][image({}) 提交异常]", image.getId(), ex);
            updateImageFail(image.getId(), StrUtil.format("Midjourney 提交失败：{}", ex.getMessage()), null);
        }
    }

    @Override
    public Integer midjourneySync() {
        // 1. 获取 Midjourney 平台，状态在 “进行中”，并且已经提交到中转的 image
        List<AiImageDO> images = filterList(imageMapper.selectListByStatusAndPlatform(
                        AiImageStatusEnum.IN_PROGRESS.getStatus(), AiPlatformEnum.MIDJOURNEY.getPlatform()),
                image -> StrUtil.isNotEmpty(image.getTaskId()));
        if (CollUtil.isEmpty(images)) {
            return 0;
        }

        // 2. 按照 API 密钥分组：历史记录没有绑定密钥时，沿用模型当前的密钥；模型也不存在时跳过，由超时任务置为失败
        Map<Long, List<AiImageDO>> apiKeyImagesMap = new LinkedHashMap<>();
        Map<Long, Optional<AiModelDO>> modelMap = new HashMap<>();
        for (AiImageDO image : images) {
            Long apiKeyId = image.getApiKeyId();
            if (apiKeyId == null && image.getModelId() != null) {
                apiKeyId = modelMap.computeIfAbsent(image.getModelId(),
                                modelId -> Optional.ofNullable(modelService.getModel(modelId)))
                        .map(AiModelDO::getKeyId).orElse(null);
            }
            if (apiKeyId == null) {
                log.warn("[midjourneySync][image({}) 无法确定 API 密钥，跳过同步]", image.getId());
                continue;
            }
            apiKeyImagesMap.computeIfAbsent(apiKeyId, key -> new ArrayList<>()).add(image);
        }

        // 3. 逐个密钥同步：单个密钥同步失败（例如说：Proxy 不可用），不影响其它密钥
        int count = 0;
        for (Map.Entry<Long, List<AiImageDO>> entry : apiKeyImagesMap.entrySet()) {
            try {
                count += midjourneySync(entry.getKey(), entry.getValue());
            } catch (Exception ex) {
                log.error("[midjourneySync][apiKey({}) 同步异常]", entry.getKey(), ex);
            }
        }
        return count;
    }

    private int midjourneySync(Long apiKeyId, List<AiImageDO> images) {
        // 1. 调用 Midjourney Proxy 获取任务进展
        MidjourneyApi midjourneyApi = modelService.getMidjourneyApi(apiKeyService.validateApiKey(apiKeyId));
        List<MidjourneyApi.Notify> taskList = midjourneyApi.getTaskList(convertSet(images, AiImageDO::getTaskId));
        Map<String, MidjourneyApi.Notify> taskMap = convertMap(taskList, MidjourneyApi.Notify::id);

        // 2. 逐个处理，更新进展
        int count = 0;
        for (AiImageDO image : images) {
            MidjourneyApi.Notify notify = taskMap.get(image.getTaskId());
            if (notify == null) {
                log.error("[midjourneySync][image({}) 查询不到进展]", image.getId());
                continue;
            }
            count++;
            updateMidjourneyStatus(image, notify);
        }
        return count;
    }

    @Override
    public void midjourneyNotify(MidjourneyApi.Notify notify) {
        // 1. 获得任务编号对应的全部 image（含已结束的）：同一 API 密钥复用任务编号（中转返回 21）时，是同一个上游任务
        List<AiImageDO> images = imageMapper.selectListByTaskId(notify.id());
        if (CollUtil.isEmpty(images)) {
            log.warn("[midjourneyNotify][回调任务({}) 不存在]", notify.id());
            return;
        }
        // 2. 属于多个 API 密钥（例如说：不同租户的中转返回了相同的任务编号）时，无法确定归属，交给按密钥同步的 Job 处理
        // 注意：已结束的记录也要参与判断，否则其它密钥的迟到回调，会被误写到仍在进行中的记录
        if (images.stream().map(AiImageDO::getApiKeyId).distinct().count() > 1) {
            log.warn("[midjourneyNotify][回调任务({}) 属于多个 API 密钥，忽略回调，由同步 Job 更新]", notify.id());
            return;
        }

        // 3. 更新状态：只更新进行中的记录（updateMidjourneyStatus 中判断）
        images.forEach(image -> updateMidjourneyStatus(image, notify));
    }

    private void updateMidjourneyStatus(AiImageDO image, MidjourneyApi.Notify notify) {
        // 1. 只更新进行中的记录：超时、已完成后迟到的回调，只记录日志
        if (ObjUtil.notEqual(image.getStatus(), AiImageStatusEnum.IN_PROGRESS.getStatus())) {
            log.info("[updateMidjourneyStatus][image({}) 状态({}) 不是进行中，忽略任务状态({})]",
                    image.getId(), image.getStatus(), notify.status());
            return;
        }

        // 2. 转换状态与进度
        AiImageDO updateObj = new AiImageDO();
        MidjourneyApi.TaskStatusEnum taskStatus = StrUtil.isBlank(notify.status()) ? null
                : EnumUtil.fromString(MidjourneyApi.TaskStatusEnum.class, notify.status(), null);
        if (taskStatus == null) {
            // 未知状态：不抛出异常（避免中断其它任务的同步），也不更新状态
            log.warn("[updateMidjourneyStatus][image({}) 未知的任务状态({})，不更新状态]", image.getId(), notify.status());
        } else {
            // noinspection EnhancedSwitchMigration
            switch (taskStatus) {
                case NOT_START:
                case SUBMITTED:
                    updateObj.setProgress(0);
                    break;
                case IN_PROGRESS:
                    updateObj.setProgress(AiImageUtils.parseProgress(notify.progress()));
                    break;
                case SUCCESS:
                    updateObj.setStatus(AiImageStatusEnum.SUCCESS.getStatus()).setProgress(100)
                            .setPicUrl(transferMidjourneyImage(image, notify.imageUrl()))
                            .setFinishTime(LocalDateTime.now());
                    break;
                case FAILURE:
                case CANCEL:
                    updateObj.setStatus(AiImageStatusEnum.FAIL.getStatus())
                            .setErrorMessage(StrUtil.blankToDefault(notify.failReason(),
                                    taskStatus == MidjourneyApi.TaskStatusEnum.CANCEL ? "任务已取消" : "任务失败"))
                            .setFinishTime(LocalDateTime.now());
                    break;
                default: // MODAL：保持进行中，进度不变
                    break;
            }
        }

        // 3. 读改写 midjourneyOptions 的元数据（中转状态、按钮），请求参数不变；带状态条件写回
        AiImageDO.MidjourneyOptions options = ObjUtil.defaultIfNull(image.getMidjourneyOptions(),
                new AiImageDO.MidjourneyOptions());
        options.setProviderStatus(notify.status());
        if (notify.buttons() != null) {
            options.setButtons(notify.buttons());
        }
        updateObj.setMidjourneyOptions(options);
        int count = imageMapper.updateByIdAndStatus(image.getId(), AiImageStatusEnum.IN_PROGRESS.getStatus(), updateObj);
        if (count == 0) {
            log.info("[updateMidjourneyStatus][image({}) 已不是进行中，忽略任务状态({})]", image.getId(), notify.status());
        }
    }

    /**
     * 转存 Midjourney 结果图片；转存失败时，沿用中转的源地址
     */
    private String transferMidjourneyImage(AiImageDO image, String imageUrl) {
        if (StrUtil.isBlank(imageUrl)) {
            return null;
        }
        try {
            return fileApi.createFile(HttpUtil.downloadBytes(imageUrl));
        } catch (Exception ex) {
            log.warn("[transferMidjourneyImage][图片({}) 地址({}) 上传失败]", image.getId(), imageUrl, ex);
            return imageUrl;
        }
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
