package cn.iocoder.yudao.module.ai.service.music;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.music.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.music.AiMusicDO;
import cn.iocoder.yudao.module.ai.dal.mysql.music.AiMusicMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicGenerateModeEnum;
import cn.iocoder.yudao.module.ai.enums.music.AiMusicStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.suno.api.SunoApi;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.AiChatUtils;
import cn.iocoder.yudao.module.ai.util.FileTypeUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.net.InetAddresses;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

// TODO @AI：最好有 1. 2. 3 这样，更好理解；
/**
 * AI 音乐 Service 实现类
 *
 * 提交时创建待处理记录（PENDING），取得中转任务编号后进入生成中（IN_PROGRESS）。
 * 中转生成成功后，每首歌曲单独进入转存中（SAVING），音频转存成功后置为已完成（SUCCESS）。
 * 确认任务失败或超时后置为已失败（FAIL）。
 *
 * @author xiaoxin
 */
@Service
@Slf4j
public class AiMusicServiceImpl implements AiMusicService {

    /**
     * 任务超时时间：从创建时间起算
     */
    @VisibleForTesting
    static final int TIMEOUT_MINUTES = 30;
    /**
     * 每轮最多处理的行数：生成中（查询中转）、转存中（下载文件）分别计算
     */
    private static final int SYNC_LIMIT = 200;
    /**
     * 单次批量查询中转的任务数
     */
    private static final int FETCH_BATCH_SIZE = 20;

    /**
     * 允许上传的音频类型（按文件内容识别）：mp3、wav
     */
    private static final Set<String> UPLOAD_AUDIO_TYPES = asSet("audio/mpeg", "audio/vnd.wave");

    /**
     * 下载文件的最大字节数
     */
    // TODO @AI：不限制噢；（保留：文件服务没有大小限制，下载内容整体读入内存，不限制时异常大的文件会占满内存）
    // TODO @AI：不限制；
    private static final int MAX_DOWNLOAD_BYTES = 50 * 1024 * 1024;
    // TODO @AI：使用 httputil 噢；简化下；（保留：hutool 基于 HttpURLConnection，无法在实际连接前校验 IP，会放行内网）
    // TODO @AI：就按照我说的；
    /**
     * 下载文件的 HTTP 客户端：不走代理，自动跟随跳转；每次建立连接时校验实际连接的 IP，参见 {@link PublicSocketFactory}
     */
    private static final OkHttpClient DOWNLOAD_CLIENT = new OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY).socketFactory(new PublicSocketFactory())
            .connectTimeout(60, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 歌词提示词：第一行为歌曲名称，参见 {@link #parseLyric(String)}
     */
    private static final String LYRIC_PROMPT_TEMPLATE =
            "你是一位才华横溢的作曲家，拥有丰富的情感和细腻的笔触，你对文字有着独特的感悟力，能将各种情感和意境巧妙地融入歌词中。\n"
            + "请以【{}】为主题创作一首歌曲，歌曲时间不要太短，3分钟左右，不要输出任何解释性的内容。\n"
            + "下面是一个标准的歌词输出模板：\n"
            + "歌曲名称\n\n"
            + "[Verse]\n[歌词]\n\n"
            + "[Verse 2]\n[歌词]\n\n"
            + "[Chorus]\n[歌词]\n\n"
            + "[Verse 3]\n[歌词]\n\n"
            + "[Bridge]\n[歌词]\n\n"
            + "[Chorus]\n[歌词]\n";

    @Resource
    private AiMusicMapper musicMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 创作 ==========

    @Override
    public Long generateMusic(Long userId, AiMusicGenerateReqVO reqVO) {
        // 1. 校验模型与其当前密钥
        AiModelDO model = validateMusicModel(reqVO.getModelId());
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());

        // 2. 插入占位行
        boolean inspiration = Objects.equals(reqVO.getGenerateMode(), AiMusicGenerateModeEnum.INSPIRATION.getMode());
        boolean instrumental = Boolean.TRUE.equals(reqVO.getMakeInstrumental());
        AiMusicDO music = buildPendingMusic(userId, reqVO.getGenerateMode(), model, apiKey.getId())
                .setPrompt(inspiration ? reqVO.getPrompt() : null)
                .setLyric(inspiration || instrumental ? null : reqVO.getLyric())
                .setTags(inspiration ? null : reqVO.getTags())
                .setTitle(inspiration ? null : reqVO.getTitle())
                .setMakeInstrumental(instrumental);
        musicMapper.insert(music);

        // 3. 提交中转
        SunoApi.MusicRequest request = inspiration
                ? new SunoApi.MusicRequest(music.getPrompt(), null, null, null, model.getModel(), instrumental, null, null, null)
                : new SunoApi.MusicRequest(null, StrUtil.nullToEmpty(music.getLyric()), music.getTags(), music.getTitle(),
                model.getModel(), instrumental, null, null, null);
        submitMusic(music, apiKey, sunoApi -> sunoApi.submitMusic(request));
        return music.getId();
    }

    @Override
    public Long extendMusic(Long userId, AiMusicExtendReqVO reqVO) {
        // 1.1 校验来源：本人、已完成、有中转歌曲编号
        AiMusicDO ref = validateRefMusic(userId, reqVO.getId());
        if (ref.getDuration() != null && reqVO.getExtendSeconds() > ref.getDuration()) {
            throw exception(MUSIC_EXTEND_SECONDS_EXCEED);
        }
        // 1.2 沿用来源的密钥：保证 task_id、clip_id 由同一中转识别
        AiApiKeyDO apiKey = validateRefApiKey(ref);

        // 2. 插入占位行：模型、密钥、是否纯音乐沿用来源
        String tags = StrUtil.blankToDefault(reqVO.getTags(), ref.getTags());
        AiMusicDO music = buildPendingMusic(userId, AiMusicGenerateModeEnum.EXTEND.getMode(), ref)
                .setRefMusicId(ref.getId()).setExtendSeconds(reqVO.getExtendSeconds())
                .setLyric(reqVO.getLyric()).setTags(tags).setTitle(reqVO.getTitle());
        musicMapper.insert(music);

        // 3. 提交中转：task_id、continue_clip_id 一律从来源行读取
        SunoApi.MusicRequest request = new SunoApi.MusicRequest(null, StrUtil.nullToEmpty(reqVO.getLyric()), tags,
                reqVO.getTitle(), ref.getModel(), ref.getMakeInstrumental(),
                ref.getTaskId(), ref.getClipId(), reqVO.getExtendSeconds());
        submitMusic(music, apiKey, sunoApi -> sunoApi.submitMusic(request));
        return music.getId();
    }

    @Override
    public Long concatMusic(Long userId, AiMusicConcatReqVO reqVO) {
        // 1. 校验来源：本人已完成的续写作品
        AiMusicDO ref = validateRefMusic(userId, reqVO.getId());
        if (ObjUtil.notEqual(ref.getGenerateMode(), AiMusicGenerateModeEnum.EXTEND.getMode())) {
            throw exception(MUSIC_REF_NOT_EXTEND);
        }
        AiApiKeyDO apiKey = validateRefApiKey(ref);

        // 2. 插入占位行
        AiMusicDO music = buildPendingMusic(userId, AiMusicGenerateModeEnum.CONCAT.getMode(), ref)
                .setRefMusicId(ref.getId()).setTitle(ref.getTitle()).setTags(ref.getTags());
        musicMapper.insert(music);

        // 3. 提交中转
        submitMusic(music, apiKey, sunoApi -> sunoApi.submitConcat(ref.getClipId()));
        return music.getId();
    }

    @Override
    @SneakyThrows
    public Long uploadMusic(Long userId, AiMusicUploadReqVO reqVO) {
        // 1.1 校验音频：按文件内容识别类型，只接受 mp3、wav
        byte[] content = reqVO.getFile().getBytes();
        if (!UPLOAD_AUDIO_TYPES.contains(FileTypeUtils.getMineType(content))) {
            throw exception(MUSIC_UPLOAD_FILE_INVALID);
        }
        // 1.2 校验模型与其当前密钥
        AiModelDO model = validateMusicModel(reqVO.getModelId());
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());

        // 2. 转存到文件服务：交给中转的只有文件服务的地址，不接受调用方传入的地址
        String fileUrl = fileApi.createFile(content);

        // 3. 插入占位行：提交前先记录文件地址
        AiMusicDO music = buildPendingMusic(userId, AiMusicGenerateModeEnum.UPLOAD.getMode(), model, apiKey.getId())
                .setTitle(StrUtil.blankToDefault(reqVO.getTitle(), FileUtil.mainName(reqVO.getFile().getOriginalFilename())))
                .setAudioUrl(fileUrl).setMakeInstrumental(false);
        musicMapper.insert(music);

        // 4. 提交中转：中转需要能访问该地址，私有存储时使用预签名地址；本地等存储不支持预签名，直接使用原地址
        // DONE @AI：别的模块，目前有这么搞么？presignGetUrl？如果没有，我们先简化掉；（OA 文件、PMS 文档分享也在用；只有 S3 实现，本地等存储抛 UnsupportedOperationException，所以保留兜底）
        String accessUrl;
        try {
            accessUrl = fileApi.presignGetUrl(fileUrl, null);
        } catch (UnsupportedOperationException e) {
            accessUrl = fileUrl;
        }
        String finalAccessUrl = accessUrl;
        submitMusic(music, apiKey, sunoApi -> sunoApi.uploadAudioUrl(finalAccessUrl));
        return music.getId();
    }

    // DONE @AI：方法内注释；看看要不要拆几个点；
    @Override
    public AiMusicLyricRespVO generateLyric(Long userId, AiMusicLyricReqVO reqVO) {
        // 1.1 获得默认的对话模型
        AiModelDO model = modelService.getRequiredDefaultModel(AiModelTypeEnum.CHAT.getType());
        ChatModel chatModel = modelService.getChatModel(model.getId());
        // 1.2 构建提示词
        AiPlatformEnum platform = AiPlatformEnum.validatePlatform(model.getPlatform());
        ChatOptions options = AiChatUtils.buildChatOptions(platform, model.getModel(), model.getTemperature(), model.getMaxTokens());
        Prompt prompt = new Prompt(Collections.singletonList(
                new UserMessage(StrUtil.format(LYRIC_PROMPT_TEMPLATE, reqVO.getPrompt()))), options);

        // 2. 同步生成
        String content;
        try {
            ChatResponse response = chatModel.call(prompt);
            content = response != null && response.getResult() != null ? response.getResult().getOutput().getText() : null;
        } catch (Exception e) {
            log.error("[generateLyric][userId({}) prompt({}) 生成失败]", userId, reqVO.getPrompt(), e);
            throw exception(MUSIC_LYRIC_GENERATE_FAIL);
        }
        if (StrUtil.isBlank(content)) {
            throw exception(MUSIC_LYRIC_GENERATE_FAIL);
        }

        // 3. 解析歌曲名称、歌词
        return parseLyric(content);
    }

    /**
     * 解析生成的歌词：第一行非空内容作为歌曲名称，其余作为歌词
     */
    @VisibleForTesting
    static AiMusicLyricRespVO parseLyric(String content) {
        // 1. 第一行非空内容作为名称：去掉模型常带的 Markdown 标记、书名号与“歌曲名称：”前缀
        List<String> lines = new ArrayList<>(StrUtil.split(StrUtil.trim(content), '\n'));
        String title = null;
        while (CollUtil.isNotEmpty(lines)) {
            String line = StrUtil.trim(lines.remove(0));
            if (StrUtil.isNotBlank(line)) {
                title = StrUtil.trim(ReUtil.delAll("^(#+|\\*+)|\\*+$|[《》]|^(歌曲名称|歌名|标题)[:：]", line));
                break;
            }
        }
        // 2. 首行就是段落标记（例如 [Verse]）时，不当作名称
        if (title != null && title.startsWith("[")) {
            lines.add(0, title);
            title = null;
        }
        // 3. 其余作为歌词
        return new AiMusicLyricRespVO(title, StrUtil.trim(String.join("\n", lines)));
    }

    private AiModelDO validateMusicModel(Long modelId) {
        AiModelDO model = modelService.validateModel(modelId);
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.MUSIC.getType())
                || ObjUtil.notEqual(model.getPlatform(), AiPlatformEnum.SUNO.getPlatform())) {
            throw exception(MUSIC_MODEL_TYPE_ERROR);
        }
        return model;
    }

    private AiMusicDO validateRefMusic(Long userId, Long refId) {
        AiMusicDO ref = musicMapper.selectById(refId);
        if (ref == null || ObjUtil.notEqual(ref.getUserId(), userId)
                || ObjUtil.notEqual(ref.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus())
                || StrUtil.hasBlank(ref.getTaskId(), ref.getClipId())) {
            throw exception(MUSIC_REF_NOT_AVAILABLE);
        }
        return ref;
    }

    private AiApiKeyDO validateRefApiKey(AiMusicDO ref) {
        AiApiKeyDO apiKey = ref.getApiKeyId() != null ? apiKeyService.getApiKey(ref.getApiKeyId()) : null;
        if (apiKey == null || CommonStatusEnum.isDisable(apiKey.getStatus())) {
            throw exception(MUSIC_REF_API_KEY_UNAVAILABLE);
        }
        return apiKey;
    }

    private static AiMusicDO buildPendingMusic(Long userId, Integer generateMode, AiModelDO model, Long apiKeyId) {
        return new AiMusicDO().setUserId(userId).setGenerateMode(generateMode)
                .setPlatform(model.getPlatform()).setModelId(model.getId()).setModel(model.getModel()).setApiKeyId(apiKeyId)
                .setStatus(AiMusicStatusEnum.PENDING.getStatus()).setProgress(0)
                .setPublicStatus(false).setPlayCount(0);
    }

    private static AiMusicDO buildPendingMusic(Long userId, Integer generateMode, AiMusicDO ref) {
        return new AiMusicDO().setUserId(userId).setGenerateMode(generateMode)
                .setPlatform(ref.getPlatform()).setModelId(ref.getModelId()).setModel(ref.getModel()).setApiKeyId(ref.getApiKeyId())
                .setMakeInstrumental(Boolean.TRUE.equals(ref.getMakeInstrumental()))
                .setStatus(AiMusicStatusEnum.PENDING.getStatus()).setProgress(0)
                .setPublicStatus(false).setPlayCount(0);
    }

    /**
     * 提交中转：成功更新为生成中，失败更新为已失败并记录原因；都不抛出，前端按状态展示
     */
    private void submitMusic(AiMusicDO music, AiApiKeyDO apiKey, Function<SunoApi, String> submitter) {
        try {
            // 1. 提交中转，更新为生成中
            String taskId = submitter.apply(modelService.getSunoApi(apiKey));
            musicMapper.updateByIdAndStatus(music.getId(), AiMusicStatusEnum.PENDING.getStatus(), new AiMusicDO()
                    .setTaskId(taskId).setStatus(AiMusicStatusEnum.IN_PROGRESS.getStatus()).setProgress(1));
        } catch (Exception e) {
            // 2. 提交失败，更新为已失败：中转明确返回的原因原样保留；网络异常、超时只给出概括，不暴露中转地址
            log.error("[submitMusic][music({}) apiKey({}) 提交中转失败]", music.getId(), apiKey.getId(), e);
            String errorMessage = e instanceof IllegalStateException && StrUtil.isNotBlank(e.getMessage()) ? e.getMessage()
                    : ExceptionUtil.isCausedBy(e, TimeoutException.class) ? "中转服务响应超时，请稍后重试" : "中转服务请求失败，请稍后重试";
            updateMusicFail(music.getId(), AiMusicStatusEnum.PENDING.getStatus(), errorMessage);
        }
    }

    // ========== 我的作品、音乐广场 ==========

    @Override
    public PageResult<AiMusicDO> getMusicMyPage(Long userId, PageParam pageReqVO) {
        return musicMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public PageResult<AiMusicDO> getMusicPublicPage(AiMusicPublicPageReqVO pageReqVO) {
        return musicMapper.selectPageByPublic(pageReqVO);
    }

    @Override
    public AiMusicDO getMusicDetail(Long id, Long userId) {
        return validateMusicVisible(id, userId);
    }

    @Override
    public void updateMyMusic(AiMusicUpdateMyReqVO updateReqVO, Long userId) {
        // 1. 校验：本人、已完成
        AiMusicDO music = validateMusicExists(updateReqVO.getId());
        if (ObjUtil.notEqual(music.getUserId(), userId)) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        if (ObjUtil.notEqual(music.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus())) {
            throw exception(MUSIC_STATUS_NOT_SUCCESS);
        }

        // 2. 更新
        // DONE @AI：StrUtil.emptyToNull(StrUtil.trim(、StrUtil.emptyToNull( 类似这种都去掉，简化下；
        musicMapper.updateById(BeanUtils.toBean(updateReqVO, AiMusicDO.class));
    }

    @Override
    public void deleteMusicMy(Long id, Long userId) {
        // 1. 校验：本人、已结束
        AiMusicDO music = validateMusicExists(id);
        if (ObjUtil.notEqual(music.getUserId(), userId)) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        validateMusicFinished(music);

        // 2. 删除记录
        musicMapper.deleteById(id);
    }

    // DONE @AI：方法内注释；
    @Override
    public void playMusic(Long id, Long userId) {
        // 1. 校验：可见、已完成
        AiMusicDO music = validateMusicVisible(id, userId);
        if (ObjUtil.notEqual(music.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus())) {
            throw exception(MUSIC_STATUS_NOT_SUCCESS);
        }

        // 2. 增加播放次数
        musicMapper.updatePlayCountIncr(id);
    }

    /**
     * 校验可见：本人，或已公开且已完成；否则统一按不存在处理
     */
    private AiMusicDO validateMusicVisible(Long id, Long userId) {
        AiMusicDO music = musicMapper.selectById(id);
        if (music == null) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        if (Objects.equals(music.getUserId(), userId)) {
            return music;
        }
        if (Boolean.TRUE.equals(music.getPublicStatus())
                && Objects.equals(music.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus())) {
            return music;
        }
        throw exception(MUSIC_NOT_EXISTS);
    }

    // ========== 音乐管理 ==========

    @Override
    public PageResult<AiMusicDO> getMusicPage(AiMusicPageReqVO pageReqVO) {
        return musicMapper.selectPage(pageReqVO);
    }

    @Override
    public AiMusicDO getMusic(Long id) {
        return musicMapper.selectById(id);
    }

    @Override
    public void updateMusic(AiMusicUpdateReqVO updateReqVO) {
        // 校验存在、已完成
        AiMusicDO music = validateMusicExists(updateReqVO.getId());
        if (ObjUtil.notEqual(music.getStatus(), AiMusicStatusEnum.SUCCESS.getStatus())) {
            throw exception(MUSIC_STATUS_NOT_SUCCESS);
        }
        // 更新
        musicMapper.updateById(new AiMusicDO().setId(updateReqVO.getId()).setPublicStatus(updateReqVO.getPublicStatus()));
    }

    @Override
    public void deleteMusic(Long id) {
        // 校验存在、已结束：未结束的删除后，同步任务会把结果写回已删除的行
        validateMusicFinished(validateMusicExists(id));
        // 删除
        musicMapper.deleteById(id);
    }

    private AiMusicDO validateMusicExists(Long id) {
        AiMusicDO music = musicMapper.selectById(id);
        if (music == null) {
            throw exception(MUSIC_NOT_EXISTS);
        }
        return music;
    }

    private static void validateMusicFinished(AiMusicDO music) {
        if (AiMusicStatusEnum.isRunning(music.getStatus())) {
            throw exception(MUSIC_STATUS_NOT_FINISHED);
        }
    }

    // ========== 同步 ==========

    @Override
    public int syncMusic() {
        // 三步互相独立：任一步异常都不影响其它步骤
        int count = 0;
        try {
            count += fetchMusic();
        } catch (Exception e) {
            log.error("[syncMusic][查询中转异常]", e);
        }
        try {
            count += transferMusic();
        } catch (Exception e) {
            log.error("[syncMusic][转存异常]", e);
        }
        try {
            count += timeoutMusic();
        } catch (Exception e) {
            log.error("[syncMusic][超时处理异常]", e);
        }
        return count;
    }

    /**
     * ① 查询：生成中、且未拆分的占位行，按提交时绑定的密钥分组，批量查询中转
     *
     * @return 处理的数量
     */
    // DONE @AI：方法内注释；
    // DONE @AI：看看一些能不能使用 CollectionUtils 简化；别的方法也是；
    @VisibleForTesting
    int fetchMusic() {
        // 1. 查询生成中、且未拆分的占位行
        List<AiMusicDO> list = musicMapper.selectListForFetch(AiMusicStatusEnum.IN_PROGRESS.getStatus(), SYNC_LIMIT);
        // 2. 按密钥分组查询：某个渠道异常（例如中转地址配置错误）时，只跳过该渠道，下轮重试
        // DONE @AI：先转换出来，再 for 循环；convertMultiMap(list, AiMusicDO::getApiKeyId).entrySet())
        Map<Long, List<AiMusicDO>> musicMap = convertMultiMap(list, AiMusicDO::getApiKeyId);
        int count = 0;
        for (Map.Entry<Long, List<AiMusicDO>> entry : musicMap.entrySet()) {
            try {
                count += fetchMusic(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("[fetchMusic][apiKey({}) 查询失败，跳过该渠道]", entry.getKey(), e);
            }
        }
        return count;
    }

    /**
     * 查询同一密钥下的占位行
     *
     * @param apiKeyId 提交时绑定的密钥编号
     * @param list 占位行
     * @return 处理的数量
     */
    private int fetchMusic(Long apiKeyId, List<AiMusicDO> list) {
        // 1. 密钥只停用也照常查询；已删除则都置为已失败
        AiApiKeyDO apiKey = apiKeyService.getApiKey(apiKeyId);
        if (apiKey == null) {
            list.forEach(music -> updateMusicFail(music.getId(), AiMusicStatusEnum.IN_PROGRESS.getStatus(), "渠道密钥已删除"));
            return list.size();
        }
        // 2. 分批查询中转
        SunoApi sunoApi = modelService.getSunoApi(apiKey);
        int count = 0;
        for (List<AiMusicDO> batch : CollUtil.split(list, FETCH_BATCH_SIZE)) {
            count += fetchMusic(sunoApi, batch);
        }
        return count;
    }

    /**
     * 批量查询一批占位行的中转任务，再逐个处理
     *
     * @return 处理的数量
     */
    private int fetchMusic(SunoApi sunoApi, List<AiMusicDO> batch) {
        // 1. 批量查询中转：失败时保持生成中，下轮重试；一直失败由超时兜底
        List<SunoApi.Task> tasks;
        try {
            tasks = sunoApi.fetchTasks(convertList(batch, AiMusicDO::getTaskId));
        } catch (Exception e) {
            log.warn("[fetchMusic][music({}) 查询中转失败]", convertList(batch, AiMusicDO::getId), e);
            return 0;
        }
        // 2. 逐个处理：单个失败（例如拆分时数据库异常而整体回滚）时保持生成中，下轮重来
        Map<String, SunoApi.Task> taskMap = convertMap(tasks, SunoApi.Task::taskId);
        int count = 0;
        for (AiMusicDO music : batch) {
            SunoApi.Task task = taskMap.get(music.getTaskId());
            if (task == null) {
                log.warn("[fetchMusic][music({}) task({}) 中转未返回]", music.getId(), music.getTaskId());
                continue;
            }
            try {
                handleTask(music, task);
                count++;
            } catch (Exception e) {
                log.error("[fetchMusic][music({}) task({}) 处理失败]", music.getId(), music.getTaskId(), e);
            }
        }
        return count;
    }

    /**
     * 处理中转任务：中转状态字符串只在这里判断，不写入本地 status
     */
    private void handleTask(AiMusicDO music, SunoApi.Task task) {
        // 1. 成功：拆分为每首一行；没有歌曲时置为已失败
        if (task.isSuccess()) {
            // 跳过没有编号、编号重复的：重复会撞唯一键，导致整批回滚
            Set<String> clipIds = new HashSet<>();
            List<SunoApi.Clip> clips = filterList(task.data(),
                    clip -> clip != null && StrUtil.isNotBlank(clip.id()) && clipIds.add(clip.id()));
            // DONE @AI：CollUtil 类似的，都改成这种判断噢；
            if (CollUtil.isEmpty(clips)) {
                updateMusicFail(music.getId(), AiMusicStatusEnum.IN_PROGRESS.getStatus(), "中转返回成功，但没有有效的歌曲");
                return;
            }
            getSelf().splitMusic(music, clips);
            return;
        }
        // 2. 失败：置为已失败
        if (task.isFailure()) {
            updateMusicFail(music.getId(), AiMusicStatusEnum.IN_PROGRESS.getStatus(),
                    StrUtil.blankToDefault(task.failReason(), "生成失败"));
            return;
        }
        // 3. 其它状态（SUBMITTED、IN_PROGRESS 等）：只更新进度，最多 99
        Integer progress = parseProgress(task.progress());
        if (progress != null && ObjUtil.notEqual(progress, music.getProgress())) {
            musicMapper.updateByIdAndStatus(music.getId(), AiMusicStatusEnum.IN_PROGRESS.getStatus(),
                    new AiMusicDO().setProgress(progress));
        }
    }

    /**
     * ② 拆分：占位行更新为第 1 首，其余首新增行，共享 task_id；单事务、不做文件下载。
     * 插入撞唯一键 (api_key_id, task_id, clip_id) 时抛出异常整体回滚
     *
     * @param music 占位行
     * @param clips 中转返回的歌曲
     */
    @Transactional(rollbackFor = Exception.class)
    public void splitMusic(AiMusicDO music, List<SunoApi.Clip> clips) {
        // 1. 占位行更新为第 1 首：影响 0 行，说明已被其它执行者拆分，整体跳过
        int updated = musicMapper.updateByIdAndUnsplit(music.getId(), AiMusicStatusEnum.IN_PROGRESS.getStatus(),
                buildClipMusic(new AiMusicDO(), clips.get(0)));
        if (updated == 0) {
            log.info("[splitMusic][music({}) 已被拆分，跳过]", music.getId());
            return;
        }
        // 2. 其余首新增行：复制占位行（用户、模型、密钥、task_id 等），再填入该首的结果
        for (int i = 1; i < clips.size(); i++) {
            musicMapper.insert(buildClipMusic(BeanUtils.toBean(music, AiMusicDO.class).setId(null), clips.get(i)));
        }
    }

    /**
     * 填入中转返回的一首：有音频的进入转存中，出错或没有音频的置为已失败
     *
     * @param music 要填入的行：占位行的更新对象（空对象），或复制的占位行；名称、歌词、风格中转没返回时，沿用该行的
     * @param clip 中转返回的歌曲
     * @return music 本身
     */
    private static AiMusicDO buildClipMusic(AiMusicDO music, SunoApi.Clip clip) {
        // 1. 歌曲信息：时长 NaN、Infinity 无法写入数据库，与负数一起留空，避免整批回滚
        // 视频地址不转存，前端直接作为链接，只保留 http(s) 的
        SunoApi.Metadata metadata = clip.metadata();
        Double duration = metadata != null ? metadata.duration() : null;
        String videoUrl = HttpUtil.isHttp(clip.videoUrl()) || HttpUtil.isHttps(clip.videoUrl()) ? clip.videoUrl() : null;
        // DONE @AI：一些 trim blank 看看简化掉，没必要哈；包括 128 1024 阶段，也先不弄；简化！
        // （去掉 128、1024 截断：Suno 的名称、风格本身在字段长度内；名称、歌词、风格中转为空时沿用提交时的，保留）
        music.setClipId(clip.id()).setProgress(100)
                .setTitle(StrUtil.blankToDefault(clip.title(), music.getTitle()))
                .setLyric(metadata != null ? StrUtil.blankToDefault(metadata.prompt(), music.getLyric()) : music.getLyric())
                .setTags(metadata != null ? StrUtil.blankToDefault(metadata.tags(), music.getTags()) : music.getTags())
                .setDuration(duration != null && Double.isFinite(duration) && duration >= 0 ? duration : null)
                .setCoverUrl(clip.coverUrl()).setAudioUrl(clip.audioUrl()).setVideoUrl(videoUrl);
        // 2. 状态：出错或没有音频的置为已失败，其余进入转存中
        String errorMessage = ObjUtil.defaultIfNull(clip.errorMessage(),
                StrUtil.isBlank(clip.audioUrl()) ? "中转未返回音频" : null);
        if (errorMessage != null) {
            return music.setStatus(AiMusicStatusEnum.FAIL.getStatus()).setFinishTime(LocalDateTime.now())
                    .setErrorMessage(errorMessage);
        }
        return music.setStatus(AiMusicStatusEnum.SAVING.getStatus());
    }

    /**
     * ③ 转存：逐行独立，把转存中的音频、封面存到文件服务；视频不转存。
     * 失败时保持转存中，下轮重试；一直失败由超时兜底
     *
     * @return 转存成功的数量
     */
    @VisibleForTesting
    int transferMusic() {
        List<AiMusicDO> list = musicMapper.selectListByStatus(AiMusicStatusEnum.SAVING.getStatus(), SYNC_LIMIT);
        int count = 0;
        for (AiMusicDO music : list) {
            try {
                transferMusic(music);
                count++;
            } catch (Exception e) {
                log.warn("[transferMusic][music({}) 转存失败，下轮重试]", music.getId(), e);
            }
        }
        return count;
    }

    /**
     * 转存一首：音频必须成功；封面可选，失败时置空，不阻塞完成
     */
    private void transferMusic(AiMusicDO music) throws IOException {
        // 1. 转存音频
        String audioUrl = fileApi.createFile(downloadFile(music.getAudioUrl()));
        // 2. 转存封面
        String coverUrl = null;
        if (StrUtil.isNotBlank(music.getCoverUrl())) {
            try {
                coverUrl = fileApi.createFile(downloadFile(music.getCoverUrl()));
            } catch (Exception e) {
                log.warn("[transferMusic][music({}) 封面转存失败，置空]", music.getId(), e);
            }
        }
        // 3. 更新为已完成：仍为转存中时才更新，已被其它执行者处理时影响 0 行
        musicMapper.updateForSave(music.getId(), AiMusicStatusEnum.SAVING.getStatus(), new AiMusicDO()
                .setCoverUrl(coverUrl).setAudioUrl(audioUrl)
                .setStatus(AiMusicStatusEnum.SUCCESS.getStatus()).setFinishTime(LocalDateTime.now()));
    }

    // TODO @AI：使用 hutool httputil 下载噢；（保留：原因同 DOWNLOAD_CLIENT）
    /**
     * 下载中转返回的文件：只支持 http(s)，最多读取 {@link #MAX_DOWNLOAD_BYTES} 字节
     */
    @VisibleForTesting
    byte[] downloadFile(String url) throws IOException {
        try (Response response = DOWNLOAD_CLIENT.newCall(new Request.Builder().url(url).build()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("下载失败（HTTP " + response.code() + "）");
            }
            // 先缓冲到上限 + 1 字节：能缓冲到，说明超过上限
            BufferedSource source = response.body().source();
            if (source.request(MAX_DOWNLOAD_BYTES + 1L)) {
                throw new IOException("文件超过 " + MAX_DOWNLOAD_BYTES + " 字节");
            }
            return source.readByteArray();
        }
    }

    /**
     * 只连接公网地址的 SocketFactory：OkHttp 解析域名（或 IP 字面量）后，用选中的 IP 建立连接，
     * 在真正连接前校验这个 IP，避免校验与连接各自解析、结果不一致；跳转后的每次连接同样经过这里
     */
    private static class PublicSocketFactory extends SocketFactory {

        @Override
        public Socket createSocket() {
            return new Socket() {

                @Override
                public void connect(SocketAddress endpoint, int timeout) throws IOException {
                    InetAddress address = ((InetSocketAddress) endpoint).getAddress();
                    if (address == null || !isPublicAddress(address)) {
                        throw new ConnectException("文件地址不是公网地址：" + endpoint);
                    }
                    super.connect(endpoint, timeout);
                }

            };
        }

        @Override
        public Socket createSocket(String host, int port) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress host, int port) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
            throw new UnsupportedOperationException();
        }

    }

    // TODO @AI：去掉，有点冗余了；（保留：这是下载时唯一的内网限制，去掉后服务端可被引导访问内网）
    /**
     * 是否为公网地址：拒绝本机、内网、链路本地（含云元数据 169.254.169.254）、
     * 运营商级 NAT（100.64.0.0/10，含阿里云元数据 100.100.100.200）、组播与保留地址；
     * IPv6 只放行全球单播 2000::/3，内嵌 IPv4 的按内嵌地址判断
     */
    @VisibleForTesting
    static boolean isPublicAddress(InetAddress address) {
        if (address instanceof Inet6Address && InetAddresses.hasEmbeddedIPv4ClientAddress((Inet6Address) address)) {
            address = InetAddresses.getEmbeddedIPv4ClientAddress((Inet6Address) address);
        }
        byte[] bytes = address.getAddress();
        if (bytes.length == 16) {
            return (bytes[0] & 0xE0) == 0x20;
        }
        int first = bytes[0] & 0xFF;
        int second = bytes[1] & 0xFF;
        return !address.isLoopbackAddress() && !address.isSiteLocalAddress() && !address.isLinkLocalAddress()
                && first != 0 && first < 224 && !(first == 100 && second >= 64 && second < 128);
    }

    /**
     * ④ 超时：待处理、生成中、转存中的，从创建时间起超过 {@link #TIMEOUT_MINUTES} 分钟，置为已失败
     *
     * @return 处理的数量
     */
    @VisibleForTesting
    int timeoutMusic() {
        // 1. 查询超时的
        List<AiMusicDO> list = musicMapper.selectListByStatusAndCreateTimeLt(
                Arrays.asList(AiMusicStatusEnum.PENDING.getStatus(), AiMusicStatusEnum.IN_PROGRESS.getStatus(),
                        AiMusicStatusEnum.SAVING.getStatus()),
                LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES));
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        // 2. 逐个置为已失败
        list.forEach(music -> updateMusicFail(music.getId(), music.getStatus(), "任务超时"));
        return list.size();
    }

    /**
     * 更新为已失败：仍为 whereStatus 时才更新，避免覆盖其它执行者的结果
     *
     * @param id 编号
     * @param whereStatus 当前状态
     * @param errorMessage 失败原因
     */
    private void updateMusicFail(Long id, Integer whereStatus, String errorMessage) {
        musicMapper.updateByIdAndStatus(id, whereStatus, new AiMusicDO()
                .setStatus(AiMusicStatusEnum.FAIL.getStatus()).setFinishTime(LocalDateTime.now())
                .setErrorMessage(StrUtil.sub(errorMessage, 0, 1024)));
    }

    @VisibleForTesting
    static Integer parseProgress(String progress) {
        if (StrUtil.isBlank(progress)) {
            return null;
        }
        String number = ReUtil.getGroup0("\\d+(\\.\\d+)?", progress);
        if (number == null) {
            return null;
        }
        return Math.max(1, Math.min(99, (int) Double.parseDouble(number)));
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private AiMusicServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
