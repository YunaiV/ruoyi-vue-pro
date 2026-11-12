package cn.iocoder.yudao.module.ai.service.ppt;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptRedrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptSlideImageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ppt.AiPptDO;
import cn.iocoder.yudao.module.ai.dal.mysql.ppt.AiPptMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptExportFormatEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptModeEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptSourceEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptStatusEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.module.ai.util.AiDocumentUtils;
import cn.iocoder.yudao.module.ai.util.AiImageUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI PPT Service 实现类
 *
 * PPT 的生成流程（每页是一张整页图，标题、要点由绘图模型画在图里）：
 * 1. 提交：插入生成中（IN_PROGRESS）的记录，异步执行 {@link #executeGeneratePpt(Long, Integer)}
 * 2. 分镜：上传材料的先读取文本；默认的对话模型一次调用，返回标题与每页的主题、标题、要点、生图提示词
 * 3. 配图：按页码逐页串行文生图，跳过已有图的页；任一页失败，则任务失败，已生成的图保留
 * 4. 完成：每页都有图，更新为已完成（SUCCESS）
 *
 * 失败后可以继续生成（{@link #resumePptMy(Long, Long)}），从中断处重入第 2、3 步；
 * 已完成、已失败的可以重绘某一页（{@link #redrawSlideMy(AiPptRedrawReqVO, Long)}），期间任务也是生成中；
 * 超过 {@link #TIMEOUT_MINUTES} 分钟没有进展的，由 {@link #timeoutPpt()} 结束。
 *
 * 并发：每次进入生成中（新建、继续生成、单页重绘），以及切换版本，{@link AiPptDO#getAttempt()} 加 1。
 * 所有修改都经过 {@link #updatePpt(AiPptDO, Integer, Integer)}，只在状态、轮次与读取时一致时写入：
 * 1. 超时、删除、开始新一轮后，上一轮迟到的分镜、图片、失败结果都会被丢弃
 * 2. 用户的修改基于旧数据时失败（提示刷新后重试），不会覆盖其它请求已成功的修改
 * 3. 超时额外要求期间没有进展（update_time 未变化），不会覆盖生成线程刚保存的进度
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class AiPptServiceImpl implements AiPptService {

    /**
     * 任务超时时间：生成中、且超过该时间没有进展（update_time 未变化）
     */
    @VisibleForTesting
    static final int TIMEOUT_MINUTES = 30;
    /**
     * 上传材料读取的最大长度：超出部分截断
     */
    private static final int CONTENT_MAX_LENGTH = 25000;
    /**
     * 标题占位的最大长度
     */
    private static final int TITLE_PLACEHOLDER_MAX_LENGTH = 50;
    /**
     * 默认标题：大纲为空白时使用
     */
    private static final String TITLE_DEFAULT = "未命名演示文稿";

    /**
     * 支持的绘图平台：Spring AI ImageModel 可直接按宽高调用的平台。
     * Stable Diffusion 需要额外的采样参数，Midjourney 是另一套异步接口，都不支持
     */
    private static final Set<String> IMAGE_PLATFORMS = asSet(AiPlatformEnum.OPENAI.getPlatform(),
            AiPlatformEnum.SILICON_FLOW.getPlatform(), AiPlatformEnum.TONG_YI.getPlatform());

    /**
     * 分镜的系统提示词：图文并茂（imagePrompt 写明画面上应出现的文字），输出带标题的 JSON 对象
     */
    private static final String SLIDES_SYSTEM_PROMPT = "# Role\n\n"
            + "你是一位顶级的专业演示文稿（PPT）策划专家和 AI 图像提示词（Prompt）工程师。"
            + "任务是根据用户提供的「内容大纲」或「设计要求」，生成一套逻辑清晰、视觉风格高度统一的幻灯片分镜数据。"
            + "目标是生成**图文并茂**的幻灯片：每页既有文字又有插图，图片上直接呈现与本页内容一致的文字，而不是留白让用户后加文字。\n\n"
            + "# Rules\n\n"
            + "1. 全局风格锚定：根据大纲推断或遵循用户要求的全局视觉风格。所有配图必须严格遵循此风格。\n"
            + "2. 结构化拆解：合理拆分为多张幻灯片，单页最多 3-4 个简短要点。\n"
            + "3. 视觉转译（图文并茂）：\n"
            + "   - 为每页构思的 imagePrompt 既要描述**插图画面**，也要明确**画面上应出现的文字**（如本页标题、要点或短句），"
            + "与当页 theme、title、points 内容一致。不要描述留白或“用于排版文字的空间”。\n"
            + "   - 图片中出现的所有文字必须使用与 theme、title、points **相同的语言**（即本次请求指定的输出语言）。\n"
            + "   - 图片上文字的量由「生成模式」决定（见用户输入中的模式说明）：\n"
            + "     - **详细演示文稿**：图片上的说明文字可适当多一些，如本页要点、一两句说明。\n"
            + "     - **演示用幻灯片**：图片上的文字尽量精简，如仅主标题或少量关键词，便于演讲时配合口述。\n"
            + "   - 每页画面为 16:9 横版幻灯片。\n"
            + "   - imagePrompt 须包含前缀「[全局风格描述]」，并清晰描述画面中的插图与文字内容（含具体要出现的文字及其语言），"
            + "不要包含“不要在图片中生成任何文字”的约束。\n"
            + "4. 严格输出合法的纯 JSON 对象，title 为整套演示文稿的标题：\n"
            + "{\"title\": \"...\", \"slides\": [{\"index\": 1, \"theme\": \"...\", \"title\": \"...\", \"points\": [\"...\"], \"imagePrompt\": \"...\"}]}\n"
            + "禁止输出任何 Markdown 标记或多余文本。\n\n"
            + "# 页数约束\n\n"
            + "请将内容拆分为恰好 {} 页的幻灯片分镜，保证逻辑完整、故事线连贯，不要多也不要少。slides 数组长度必须为 {}。";

    @Resource
    private AiPptMapper pptMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 创作：提交 ==========

    @Override
    public Long generatePpt(Long userId, AiPptGenerateReqVO reqVO) {
        // 1. 校验模型：整页图使用所选的绘图模型；分镜使用默认的对话模型
        AiModelDO imageModel = validateImageModel(reqVO.getImageModelId());
        AiModelDO chatModel = modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());

        // 2. 插入生成中的记录：上传材料在生成任务里按地址读取文本；标题先取文件名，粘贴大纲先取首行
        boolean upload = ObjUtil.equal(reqVO.getSource(), AiPptSourceEnum.UPLOAD.getSource());
        String title = upload ? FileUtil.mainName(HttpUtils.decodeUrlPath(
                HttpUtils.removeUrlPathQueryAndFragment(reqVO.getFileUrl()))) : buildTitle(reqVO.getContent());
        AiPptDO ppt = BeanUtils.toBean(reqVO, AiPptDO.class).setUserId(userId).setTitle(title)
                .setContent(upload ? null : reqVO.getContent()).setFileUrl(upload ? reqVO.getFileUrl() : null)
                .setChatModelId(chatModel.getId()).setSlides(new ArrayList<>())
                .setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setAttempt(1);
        pptMapper.insert(ppt);

        // 3. 异步生成，前端通过轮询获取进度
        getSelf().executeGeneratePpt(ppt.getId(), ppt.getAttempt());
        return ppt.getId();
    }

    /**
     * 校验绘图模型：类型为绘图、平台在 {@link #IMAGE_PLATFORMS} 中，且与 API 密钥的平台一致
     *
     * @param modelId 模型编号
     * @return 模型
     */
    private AiModelDO validateImageModel(Long modelId) {
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.IMAGE.getType())
                || !IMAGE_PLATFORMS.contains(model.getPlatform())) {
            throw exception(PPT_IMAGE_MODEL_NOT_SUPPORTED);
        }
        // 实际调用的客户端按 API 密钥的平台创建（见 AiModelServiceImpl#getImageModel），而生图参数按模型的平台构建，两者需一致
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        if (ObjUtil.notEqual(apiKey.getPlatform(), model.getPlatform())) {
            throw exception(PPT_IMAGE_MODEL_PLATFORM_MISMATCH, model.getPlatform(), apiKey.getPlatform());
        }
        return model;
    }

    /**
     * 标题占位：大纲第一行非空内容，去掉 Markdown 的标题、列表、引用标记
     */
    @VisibleForTesting
    static String buildTitle(String content) {
        for (String line : StrUtil.split(content, '\n')) {
            String title = StrUtil.trim(ReUtil.delFirst("^[#>*\\-\\s]+", line));
            if (StrUtil.isNotBlank(title)) {
                return StrUtil.sub(title, 0, TITLE_PLACEHOLDER_MAX_LENGTH);
            }
        }
        return TITLE_DEFAULT;
    }

    @Override
    public void resumePptMy(Long id, Long userId) {
        // 1. 校验存在、已失败
        AiPptDO ppt = validateMyPptExists(id, userId);
        if (ObjUtil.notEqual(ppt.getStatus(), AiPptStatusEnum.FAIL.getStatus())) {
            throw exception(PPT_STATUS_NOT_FAIL);
        }

        // 2. 置回生成中、开始新一轮：没有图的页回到待生成，有图的页不动
        Integer attempt = ppt.getAttempt();
        ppt.setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setAttempt(attempt + 1).setErrorMessage(null);
        ppt.getSlides().stream().filter(slide -> StrUtil.isBlank(slide.getImageUrl()))
                .forEach(slide -> slide.setStatus(null).setErrorMessage(null));
        if (!updatePpt(ppt, AiPptStatusEnum.FAIL.getStatus(), attempt)) {
            throw exception(PPT_UPDATE_CONFLICT);
        }

        // 3. 异步重入生成：没有分镜时重新生成分镜，否则只补没有图的页
        getSelf().executeGeneratePpt(id, ppt.getAttempt());
    }

    @Override
    public void redrawSlideMy(AiPptRedrawReqVO reqVO, Long userId) {
        // 1.1 校验存在、不在生成中
        AiPptDO ppt = validateMyPptExists(reqVO.getId(), userId);
        validatePptNotInProgress(ppt);
        // 1.2 校验本页已有图
        AiPptDO.Slide slide = validateSlideExists(ppt, reqVO.getSlideIndex());
        if (StrUtil.isBlank(slide.getImageUrl())) {
            throw exception(PPT_SLIDE_IMAGE_NOT_EXISTS, reqVO.getSlideIndex());
        }

        // 2. 任务、本页置为生成中、开始新一轮：当前图在重绘成功前保持不变
        Integer status = ppt.getStatus();
        Integer attempt = ppt.getAttempt();
        slide.setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setErrorMessage(null);
        ppt.setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setAttempt(attempt + 1);
        if (!updatePpt(ppt, status, attempt)) {
            throw exception(PPT_UPDATE_CONFLICT);
        }

        // 3. 异步重绘
        getSelf().executeRedrawSlide(ppt.getId(), ppt.getAttempt(), slide.getIndex(), reqVO.getPrompt());
    }

    @Override
    public void updateSlideImageMy(AiPptSlideImageReqVO reqVO, Long userId) {
        // 1.1 校验存在、不在生成中
        AiPptDO ppt = validateMyPptExists(reqVO.getId(), userId);
        validatePptNotInProgress(ppt);
        // 1.2 校验版本：只接受本页历史版本里的地址
        AiPptDO.Slide slide = validateSlideExists(ppt, reqVO.getSlideIndex());
        if (!anyMatch(slide.getImages(), image -> ObjUtil.equal(image.getUrl(), reqVO.getImageUrl()))) {
            throw exception(PPT_SLIDE_VERSION_NOT_EXISTS, reqVO.getSlideIndex());
        }

        // 2. 切换当前图，不新增版本；封面随之重算。轮次也加 1，使基于旧数据的并发修改失败
        Integer attempt = ppt.getAttempt();
        slide.setImageUrl(reqVO.getImageUrl());
        ppt.setAttempt(attempt + 1);
        if (!updatePpt(ppt, ppt.getStatus(), attempt)) {
            throw exception(PPT_UPDATE_CONFLICT);
        }
    }

    // ========== 创作：异步生成 ==========

    /**
     * 异步生成：没有分镜时先生成分镜，再逐页生图。新建、继续生成都从这里进入
     *
     * @param id 编号
     * @param attempt 生成轮次：只在任务仍为这一轮时写入
     */
    @Async
    public void executeGeneratePpt(Long id, Integer attempt) {
        AiPptDO ppt = pptMapper.selectById(id);
        if (!isRunning(ppt, attempt)) {
            return;
        }

        // 1. 分镜：还没有分镜时（新建，或分镜阶段失败后继续生成）
        if (CollUtil.isEmpty(ppt.getSlides())) {
            try {
                generateSlides(ppt);
            } catch (Exception ex) {
                log.error("[executeGeneratePpt][ppt({}) attempt({}) 分镜生成失败]", id, attempt, ex);
                finishPpt(ppt, "分镜生成失败：" + ex.getMessage());
            }
            // 写入失败（已被超时、新一轮取代），或分镜失败时，结束
            if (!updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt) || !isRunning(ppt, attempt)) {
                return;
            }
        }

        // 2. 逐页生图
        generateSlideImages(ppt, attempt);
    }

    /**
     * 生成分镜：上传材料的先读取文本，再调用对话模型
     *
     * @param ppt PPT：读取的文本、生成的标题与分镜，都设置到其中
     */
    private void generateSlides(AiPptDO ppt) {
        // 1. 上传材料：读取文件的文本，过长的截断
        if (StrUtil.isBlank(ppt.getContent())) {
            String content = AiDocumentUtils.readUrl(fileApi.presignGetUrlQuietly(ppt.getFileUrl()));
            ppt.setContent(StrUtil.sub(content, 0, CONTENT_MAX_LENGTH));
        }

        // 2.1 调用对话模型：沿用创建时记录的模型
        AiModelDO model = modelService.validateModel(ppt.getChatModelId());
        ChatModel chatModel = modelService.getChatModel(model.getId());
        ChatOptions options = AiChatUtils.buildChatOptions(AiPlatformEnum.validatePlatform(model.getPlatform()),
                model.getModel(), model.getTemperature(), model.getMaxTokens());
        List<Message> messages = Arrays.asList(
                new SystemMessage(StrUtil.format(SLIDES_SYSTEM_PROMPT, ppt.getPageCount(), ppt.getPageCount())),
                new UserMessage(buildSlidesUserPrompt(ppt)));
        String text = AiChatUtils.getChatResponseContent(chatModel.call(new Prompt(messages, options)));
        // 2.2 解析分镜：模型多给的页丢弃
        AiPptDO plan = parseSlides(text);
        ppt.setTitle(StrUtil.blankToDefault(plan.getTitle(), ppt.getTitle()))
                .setSlides(CollUtil.sub(plan.getSlides(), 0, ppt.getPageCount()));
    }

    /**
     * 分镜的用户提示词：素材 + 额外的设计要求 + 语言 + 格式
     */
    @VisibleForTesting
    static String buildSlidesUserPrompt(AiPptDO ppt) {
        StringBuilder prompt = new StringBuilder("下面是用户提供的演示文稿大纲内容：\n\n").append(ppt.getContent());
        if (StrUtil.isNotBlank(ppt.getPrompt())) {
            prompt.append("\n\n额外的设计要求：").append(ppt.getPrompt());
        }
        String language = "en".equals(ppt.getLanguage()) ? "英文" : "中文";
        prompt.append("\n\n请用").append(language)
                .append("输出所有分镜内容（theme、title、points、imagePrompt 等均使用该语言；图片中出现的文字也必须是")
                .append(language).append("）。");
        String mode = ObjUtil.equal(ppt.getMode(), AiPptModeEnum.DETAILED.getMode())
                ? AiPptModeEnum.DETAILED.getName() : AiPptModeEnum.SLIDES.getName();
        prompt.append("\n\n本次生成模式为：").append(mode).append("。请按上述规则控制每页插图中文字的量。");
        return prompt.toString();
    }

    /**
     * 解析分镜：截取第一个 { 到最后一个 } 之间的 JSON 对象，即去掉 Markdown 代码块围栏、前后的说明文字
     *
     * @param text 模型返回的内容
     * @return 分镜：只有 title、slides；页码按顺序重排为 1..N
     */
    @VisibleForTesting
    static AiPptDO parseSlides(String text) {
        // 1. 解析 JSON
        AiPptDO plan = StrUtil.contains(text, '{') ? JsonUtils.parseObjectQuietly(
                StrUtil.sub(text, text.indexOf('{'), text.lastIndexOf('}') + 1), AiPptDO.class) : null;
        if (plan == null) {
            throw new IllegalStateException("模型返回的分镜不是合法的 JSON");
        }
        if (CollUtil.isEmpty(plan.getSlides())) {
            throw new IllegalStateException("模型返回的分镜为空");
        }

        // 2. 逐页校验：imagePrompt 必填；只保留分镜字段
        List<AiPptDO.Slide> slides = new ArrayList<>(plan.getSlides().size());
        for (AiPptDO.Slide slide : plan.getSlides()) {
            int index = slides.size() + 1;
            if (StrUtil.isBlank(slide.getImagePrompt())) {
                throw new IllegalStateException(StrUtil.format("第 {} 页缺少生图提示词", index));
            }
            slides.add(new AiPptDO.Slide().setIndex(index).setTheme(slide.getTheme()).setTitle(slide.getTitle())
                    .setPoints(filterList(slide.getPoints(), StrUtil::isNotBlank))
                    .setImagePrompt(slide.getImagePrompt()).setImages(new ArrayList<>()));
        }
        return new AiPptDO().setTitle(plan.getTitle()).setSlides(slides);
    }

    /**
     * 逐页生图：跳过已有图的页；任一页失败，则本页、任务都置为失败，已生成的图保留
     *
     * @param ppt PPT
     * @param attempt 生成轮次
     */
    private void generateSlideImages(AiPptDO ppt, Integer attempt) {
        for (AiPptDO.Slide slide : ppt.getSlides()) {
            if (StrUtil.isNotBlank(slide.getImageUrl())) {
                continue;
            }
            // 1. 本页置为生成中
            slide.setStatus(AiPptStatusEnum.IN_PROGRESS.getStatus()).setErrorMessage(null);
            if (!updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt)) {
                return;
            }

            // 2. 生图：成功追加版本，设为当前；失败则结束
            try {
                addSlideImage(slide, drawSlideImage(ppt.getImageModelId(), slide.getImagePrompt()), slide.getImagePrompt());
            } catch (Exception ex) {
                log.error("[generateSlideImages][ppt({}) slide({}) 生图失败]", ppt.getId(), slide.getIndex(), ex);
                slide.setStatus(AiPptStatusEnum.FAIL.getStatus()).setErrorMessage(ex.getMessage());
                finishPpt(ppt, StrUtil.format("第 {} 页：{}", slide.getIndex(), ex.getMessage()));
            }
            // 写入失败（已被超时、新一轮取代），或本页失败时，结束
            if (!updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt) || !isRunning(ppt, attempt)) {
                return;
            }
        }

        // 3. 每页都有图：完成
        finishPpt(ppt, null);
        updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt);
    }

    /**
     * 异步重绘一页：成功追加版本并设为当前；失败只记录本页原因，当前图不变。之后按每页是否有图结束任务
     *
     * @param id 编号
     * @param attempt 生成轮次
     * @param index 页码
     * @param prompt 完整的生图提示词
     */
    @Async
    public void executeRedrawSlide(Long id, Integer attempt, Integer index, String prompt) {
        AiPptDO ppt = pptMapper.selectById(id);
        if (!isRunning(ppt, attempt)) {
            return;
        }

        // 1. 生图
        AiPptDO.Slide slide = findFirst(ppt.getSlides(), item -> ObjUtil.equal(item.getIndex(), index));
        try {
            addSlideImage(slide, drawSlideImage(ppt.getImageModelId(), prompt), prompt);
        } catch (Exception ex) {
            log.error("[executeRedrawSlide][ppt({}) attempt({}) slide({}) 重绘失败]", id, attempt, index, ex);
            slide.setStatus(AiPptStatusEnum.FAIL.getStatus()).setErrorMessage(ex.getMessage());
        }

        // 2. 结束：仍为这一轮时才写入；保留其它页原有的失败原因
        finishPpt(ppt, ppt.getErrorMessage());
        updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt);
    }

    /**
     * 整页文生图，保存到文件服务
     *
     * @param imageModelId 绘图模型编号
     * @param prompt 生图提示词
     * @return 文件服务的地址
     */
    private String drawSlideImage(Long imageModelId, String prompt) {
        // 1. 调用绘图模型
        AiModelDO model = validateImageModel(imageModelId);
        ImageModel imageModel = modelService.getImageModel(model.getId());
        ImageResponse response = imageModel.call(new ImagePrompt(prompt, buildImageOptions(model)));
        if (response.getResult() == null) {
            throw new IllegalStateException("生成结果为空");
        }

        // 2. 保存到文件服务
        String b64Json = response.getResult().getOutput().getB64Json();
        byte[] content = StrUtil.isNotEmpty(b64Json) ? Base64.decode(b64Json)
                : HttpUtil.downloadBytes(response.getResult().getOutput().getUrl());
        return fileApi.createFile(content);
    }

    /**
     * 构建生图参数：按平台取接近 16:9 的宽高
     */
    @VisibleForTesting
    static ImageOptions buildImageOptions(AiModelDO model) {
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        if (platform == AiPlatformEnum.OPENAI) {
            // gpt-image 系列：横版最大 1536x1024；dall-e-3：横版 1792x1024
            return StrUtil.startWithIgnoreCase(model.getModel(), "gpt-image")
                    ? AiImageUtils.buildImageOptions(platform, model.getModel(), 1536, 1024)
                    : AiImageUtils.buildImageOptions(platform, model.getModel(), 1792, 1024);
        } else if (platform == AiPlatformEnum.SILICON_FLOW) {
            // Qwen-Image 的 16:9 尺寸
            return AiImageUtils.buildImageOptions(platform, model.getModel(), 1664, 928);
        } else if (platform == AiPlatformEnum.TONG_YI) {
            return AiImageUtils.buildImageOptions(platform, model.getModel(), 1280, 720);
        }
        throw new IllegalArgumentException("不支持的 AI 平台：" + model.getPlatform());
    }

    // ========== 创作：修改 ==========

    /**
     * 更新 PPT：slides、状态等；封面、页数从 slides 重算
     *
     * 只在状态、轮次与读取时一致时更新：期间被超时、删除，或开始了新一轮的，不更新
     *
     * @param ppt 修改后的 PPT
     * @param whereStatus 读取时的状态
     * @param whereAttempt 读取时的轮次
     * @return 是否更新成功
     */
    private boolean updatePpt(AiPptDO ppt, Integer whereStatus, Integer whereAttempt) {
        return updatePpt(ppt, whereStatus, whereAttempt, null);
    }

    /**
     * 更新 PPT：在 {@link #updatePpt(AiPptDO, Integer, Integer)} 的基础上，要求更新时间早于指定时间
     *
     * @param ppt 修改后的 PPT
     * @param whereStatus 读取时的状态
     * @param whereAttempt 读取时的轮次
     * @param whereUpdateTimeLt 更新时间早于该时间才更新，允许空：用于超时，期间有进展则不更新
     * @return 是否更新成功
     */
    private boolean updatePpt(AiPptDO ppt, Integer whereStatus, Integer whereAttempt, LocalDateTime whereUpdateTimeLt) {
        List<AiPptDO.Slide> slides = ppt.getSlides();
        AiPptDO updateObj = new AiPptDO().setId(ppt.getId()).setTitle(ppt.getTitle()).setContent(ppt.getContent())
                .setSlides(slides).setSlideCount(slides.size())
                .setCoverUrl(findFirst(slides, slide -> StrUtil.isNotBlank(slide.getImageUrl()), AiPptDO.Slide::getImageUrl))
                .setFinishCount((int) count(slides, slide -> StrUtil.isNotBlank(slide.getImageUrl())))
                .setStatus(ppt.getStatus()).setAttempt(ppt.getAttempt())
                .setErrorMessage(ppt.getErrorMessage()).setFinishTime(ppt.getFinishTime());
        return pptMapper.updateByIdAndStatusAndAttempt(updateObj, whereStatus, whereAttempt, whereUpdateTimeLt) > 0;
    }

    /**
     * 是否仍为这一轮的生成中：状态为生成中、且轮次未变
     */
    private static boolean isRunning(AiPptDO ppt, Integer attempt) {
        return ppt != null && ObjUtil.equal(ppt.getStatus(), AiPptStatusEnum.IN_PROGRESS.getStatus())
                && ObjUtil.equal(ppt.getAttempt(), attempt);
    }

    /**
     * 结束生成：每页都有图为已完成，否则为已失败
     *
     * @param ppt PPT
     * @param errorMessage 已失败时的原因
     */
    private static void finishPpt(AiPptDO ppt, String errorMessage) {
        // 情况一：每页都有图，已完成。例如逐页生图完成；重绘结束（无论成败）、重绘超时
        // 情况二：有页没有图，已失败。例如分镜、某页生图失败，生成超时
        boolean success = CollUtil.isNotEmpty(ppt.getSlides())
                && ppt.getSlides().stream().allMatch(slide -> StrUtil.isNotBlank(slide.getImageUrl()));
        ppt.setStatus(success ? AiPptStatusEnum.SUCCESS.getStatus() : AiPptStatusEnum.FAIL.getStatus())
                .setErrorMessage(success ? null : errorMessage).setFinishTime(LocalDateTime.now());
    }

    /**
     * 追加图片版本，并设为当前
     */
    private static void addSlideImage(AiPptDO.Slide slide, String imageUrl, String prompt) {
        slide.getImages().add(new AiPptDO.SlideImage().setUrl(imageUrl).setPrompt(prompt).setCreateTime(LocalDateTime.now()));
        slide.setImageUrl(imageUrl).setStatus(AiPptStatusEnum.SUCCESS.getStatus()).setErrorMessage(null);
    }

    private static AiPptDO.Slide validateSlideExists(AiPptDO ppt, Integer index) {
        AiPptDO.Slide slide = findFirst(ppt.getSlides(), item -> ObjUtil.equal(item.getIndex(), index));
        if (slide == null) {
            throw exception(PPT_SLIDE_NOT_EXISTS, index);
        }
        return slide;
    }

    private static void validatePptNotInProgress(AiPptDO ppt) {
        if (ObjUtil.equal(ppt.getStatus(), AiPptStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(PPT_STATUS_IN_PROGRESS);
        }
    }

    // ========== 我的 PPT ==========

    @Override
    public PageResult<AiPptDO> getPptMyPage(Long userId, PageParam pageReqVO) {
        return pptMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public AiPptDO getPptMy(Long id, Long userId) {
        return validateMyPptExists(id, userId);
    }

    @Override
    public void deletePptMy(Long id, Long userId) {
        // 1. 校验存在
        validateMyPptExists(id, userId);

        // 2. 删除：生成中的不允许，条件删除避免与重绘、继续生成并发
        deletePptNotInProgress(id);
    }

    @Override
    public byte[] exportPpt(AiPptDO ppt, String format) {
        // 1. 校验格式、已完成
        if (!ArrayUtil.contains(AiPptExportFormatEnum.ARRAYS, format)) {
            throw exception(PPT_EXPORT_FORMAT_INVALID);
        }
        if (ObjUtil.notEqual(ppt.getStatus(), AiPptStatusEnum.SUCCESS.getStatus())) {
            throw exception(PPT_STATUS_NOT_SUCCESS);
        }

        // 2. 按页码下载每页的当前图：某页失败则整体失败，不导出缺页的文件
        List<byte[]> images = new ArrayList<>(ppt.getSlides().size());
        for (AiPptDO.Slide slide : ppt.getSlides()) {
            try {
                images.add(HttpUtil.downloadBytes(fileApi.presignGetUrlQuietly(slide.getImageUrl())));
            } catch (Exception ex) {
                log.error("[exportPpt][ppt({}) slide({}) 图片下载失败]", ppt.getId(), slide.getIndex(), ex);
                throw exception(PPT_EXPORT_FAIL, StrUtil.format("第 {} 页图片下载失败", slide.getIndex()));
            }
        }

        // 3. 拼接文件：每页一张图，16:9，等比居中
        try {
            return AiPptExportFormatEnum.PPTX.getFormat().equals(format) ? AiDocumentUtils.buildPptx(images)
                    : AiDocumentUtils.buildPdf(images);
        } catch (Exception ex) {
            log.error("[exportPpt][ppt({}) format({}) 导出失败]", ppt.getId(), format, ex);
            throw exception(PPT_EXPORT_FAIL, ex.getMessage());
        }
    }

    private AiPptDO validateMyPptExists(Long id, Long userId) {
        AiPptDO ppt = validatePptExists(id);
        if (ObjUtil.notEqual(ppt.getUserId(), userId)) {
            throw exception(PPT_NOT_EXISTS);
        }
        return ppt;
    }

    private void deletePptNotInProgress(Long id) {
        if (pptMapper.deleteByIdAndStatusNe(id, AiPptStatusEnum.IN_PROGRESS.getStatus()) == 0) {
            throw exception(PPT_STATUS_IN_PROGRESS);
        }
    }

    // ========== PPT 管理 ==========

    @Override
    public PageResult<AiPptDO> getPptPage(AiPptPageReqVO pageReqVO) {
        return pptMapper.selectPage(pageReqVO);
    }

    @Override
    public AiPptDO getPpt(Long id) {
        return pptMapper.selectById(id);
    }

    @Override
    public AiPptDO validatePptExists(Long id) {
        AiPptDO ppt = pptMapper.selectById(id);
        if (ppt == null) {
            throw exception(PPT_NOT_EXISTS);
        }
        return ppt;
    }

    @Override
    public void deletePpt(Long id) {
        // 1. 校验存在
        validatePptExists(id);

        // 2. 删除：生成中的不允许
        deletePptNotInProgress(id);
    }

    // ========== 超时 ==========

    @Override
    public int timeoutPpt() {
        // 1. 查询超时的：生成中、且超过 TIMEOUT_MINUTES 分钟没有进展
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<AiPptDO> list = pptMapper.selectListByStatusAndUpdateTimeLt(AiPptStatusEnum.IN_PROGRESS.getStatus(), deadline);

        // 2. 逐个结束
        int count = 0;
        for (AiPptDO ppt : list) {
            if (timeoutPpt(ppt, deadline)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 超时结束一个 PPT：生成中的页置为失败，再按每页是否有图结束。例如重绘超时、其它页都有图的，回到已完成
     *
     * @param ppt PPT
     * @param deadline 截止时间：期间有进展（更新时间不早于它）的，不结束
     * @return 是否结束；期间有进展、被生成线程结束、开始了新一轮的，返回 false
     */
    private boolean timeoutPpt(AiPptDO ppt, LocalDateTime deadline) {
        Integer attempt = ppt.getAttempt();
        ppt.getSlides().stream()
                .filter(slide -> ObjUtil.equal(slide.getStatus(), AiPptStatusEnum.IN_PROGRESS.getStatus()))
                .forEach(slide -> slide.setStatus(AiPptStatusEnum.FAIL.getStatus()).setErrorMessage("生成超时"));
        finishPpt(ppt, "生成超时");
        return updatePpt(ppt, AiPptStatusEnum.IN_PROGRESS.getStatus(), attempt, deadline);
    }

    // ========== 工具方法 ==========

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private AiPptServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
