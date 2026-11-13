package cn.iocoder.yudao.module.ai.service.speech;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.speech.vo.AiSpeechPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.speech.AiSpeechDO;
import cn.iocoder.yudao.module.ai.dal.mysql.speech.AiSpeechMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.enums.speech.AiSpeechStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.speech.AiSpeechUtils;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.TencentSpeechModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.model.tencent.api.TencentTtsApi;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 语音 Service 实现类
 *
 * 语音的生成流程：
 * 1. 提交：校验模型、密钥、参数后，插入生成中（IN_PROGRESS）的记录，异步执行；外部调用都不在事务中
 * 2. 合成：后台通过 {@link AiSpeechModel#synthesize} 调用服务商一次（不自动重试），拿到完整音频，转存后更新为已完成（SUCCESS）。
 *    腾讯云长文本原生为异步任务，由模型提交后立即交出任务编号（这里马上保存），再在时限内等待结果；
 *    时限内未完成的保持生成中，之后由 {@link #syncSpeech()}、{@link #getSpeechMy(Long, Long)} 按任务编号继续查询，不重新提交
 * 3. 失败：服务商失败、超时（没有任务编号的 {@link #SYNC_TIMEOUT}，有任务编号的 {@link #TASK_TIMEOUT}），更新为已失败（FAIL）
 *
 * 终态只通过「仍为生成中」的条件更新写入，迟到的结果不会覆盖
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class AiSpeechServiceImpl implements AiSpeechService {

    /**
     * 没有任务编号的超时时间：从创建时间起算。单次请求最多 180 秒，再加上转存，留足余量；用于服务重启等中断后的兜底
     */
    @VisibleForTesting
    static final Duration SYNC_TIMEOUT = Duration.ofMinutes(10);
    /**
     * 有任务编号的超时时间：从创建时间起算，与腾讯云长文本语音合成结果的保留时间（24 小时）一致
     */
    @VisibleForTesting
    static final Duration TASK_TIMEOUT = Duration.ofHours(24);
    /**
     * 每轮最多查询的任务数
     */
    private static final int SYNC_LIMIT = 200;

    @Resource
    private AiSpeechMapper speechMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    // ========== 生成 ==========

    @Override
    public Long generateSpeech(Long userId, AiSpeechGenerateReqVO reqVO) {
        // 1.1 校验模型与其当前密钥
        AiModelDO model = modelService.validateModel(reqVO.getModelId());
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.VOICE.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        if (ObjUtil.notEqual(model.getPlatform(), apiKey.getPlatform())) {
            throw exception(SPEECH_MODEL_PLATFORM_MISMATCH, model.getPlatform(), apiKey.getPlatform());
        }
        // 1.2 创建模型：不发请求；平台、模型标识不支持，凭证格式不正确等配置问题，在插入记录前报错
        AiSpeechModel speechModel;
        try {
            speechModel = modelService.getTextToSpeechModel(apiKey, model.getModel());
        } catch (IllegalArgumentException e) {
            throw exception(SPEECH_MODEL_NOT_SUPPORTED, model.getName(), e.getMessage());
        }
        // 1.3 校验参数：由模型自身校验，全部在付费调用之前；超限、不支持的直接拒绝，不截断文本，也不替换为其它值
        try {
            speechModel.validateOptions(reqVO.getInputText(), reqVO.getVoice(), reqVO.getFormat(), reqVO.getSpeed());
        } catch (IllegalArgumentException e) {
            throw exception(SPEECH_OPTION_INVALID, e.getMessage());
        }

        // 2. 插入生成中的记录：请求参数由 BeanUtils 复制；用户、平台、模型、密钥、状态为提交时的快照，不取自请求
        AiSpeechDO speech = BeanUtils.toBean(reqVO, AiSpeechDO.class).setUserId(userId)
                .setPlatform(model.getPlatform()).setModel(model.getModel()).setApiKeyId(apiKey.getId())
                .setStatus(AiSpeechStatusEnum.IN_PROGRESS.getStatus());
        speechMapper.insert(speech);

        // 3. 异步合成：绑定当前租户，不依赖登录上下文；前端按返回的编号轮询结果
        getSelf().executeSpeech(TenantContextHolder.getTenantId(), speech, speechModel);
        return speech.getId();
    }

    /**
     * 合成：调用服务商一次拿到完整音频，转存后更新为已完成；失败更新为已失败
     *
     * 原生为异步任务的平台，提交后立即保存任务编号；时限内未完成的，保持生成中，之后按任务编号继续查询
     *
     * @param tenantId 租户编号
     * @param speech 语音
     * @param speechModel 语音模型
     */
    @Async
    public void executeSpeech(Long tenantId, AiSpeechDO speech, AiSpeechModel speechModel) {
        TenantUtils.execute(tenantId, () -> {
            try {
                // 1. 调用服务商：只调用一次，不自动重试
                byte[] audio = speechModel.synthesize(speech.getInputText(), speech.getVoice(), speech.getFormat(),
                        speech.getSpeed(), taskId -> updateSpeechTaskId(speech, taskId));
                // 2.1 时限内未完成：保持生成中
                if (audio == null) {
                    log.info("[executeSpeech][speech({}) task({}) 时限内未完成，稍后按任务编号继续查询]",
                            speech.getId(), speech.getTaskId());
                    return;
                }
                // 2.2 转存音频，更新为已完成
                saveSpeech(speech, audio);
            } catch (Exception e) {
                log.error("[executeSpeech][speech({}) 生成失败]", speech.getId(), e);
                updateSpeechFail(speech.getId(), buildErrorMessage(e));
            }
        });
    }

    /**
     * 保存任务编号：提交成功后立即保存，之后只按任务编号查询，不重新提交
     */
    private void updateSpeechTaskId(AiSpeechDO speech, String taskId) {
        speech.setTaskId(taskId);
        speechMapper.updateByIdAndStatus(speech.getId(), AiSpeechStatusEnum.IN_PROGRESS.getStatus(),
                new AiSpeechDO().setTaskId(taskId));
    }

    /**
     * 转存音频，更新为已完成：仍为生成中时才更新，已超时等已结束的不再覆盖
     *
     * @param speech 语音
     * @param audio 音频
     */
    private void saveSpeech(AiSpeechDO speech, byte[] audio) {
        // 1. 校验音频：非空、不超过上限、文件头与格式一致
        AiSpeechUtils.validateAudio(audio, speech.getFormat());
        if (audio.length > AiSpeechUtils.MAX_AUDIO_SIZE) {
            throw new IllegalStateException("音频超过大小上限");
        }

        // 2.1 转存到文件服务：不保存服务商的临时地址
        String audioUrl = fileApi.createFile(audio, StrUtil.format("speech-{}.{}",
                speech.getId(), AiSpeechUtils.getFileExtension(speech.getFormat())));
        // 2.2 更新为已完成
        int updated = speechMapper.updateByIdAndStatus(speech.getId(), AiSpeechStatusEnum.IN_PROGRESS.getStatus(),
                new AiSpeechDO().setStatus(AiSpeechStatusEnum.SUCCESS.getStatus())
                        .setAudioUrl(audioUrl).setAudioSize(audio.length).setFinishTime(LocalDateTime.now()));
        if (updated == 0) {
            log.warn("[saveSpeech][speech({}) 已结束，不再更新为已完成]", speech.getId());
        }
    }

    // ========== 我的语音 ==========

    @Override
    public PageResult<AiSpeechDO> getSpeechMyPage(Long userId, PageParam pageReqVO) {
        return speechMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public AiSpeechDO getSpeechMy(Long id, Long userId) {
        // 1. 校验本人的语音存在；已结束的直接返回
        AiSpeechDO speech = validateMySpeechExists(id, userId);
        if (ObjUtil.notEqual(speech.getStatus(), AiSpeechStatusEnum.IN_PROGRESS.getStatus())) {
            return speech;
        }
        // 2. 生成中的，先同步一次：超时的置为已失败；异步任务查询一次结果。未开启定时任务时，前端轮询也能拿到结果
        try {
            if (!timeoutSpeech(speech, LocalDateTime.now()) && speech.getTaskId() != null) {
                fetchSpeech(speech);
            }
        } catch (Exception e) {
            log.warn("[getSpeechMy][speech({}) 同步失败，稍后重试]", id, e);
        }
        return speechMapper.selectById(id);
    }

    @Override
    public void deleteSpeechMy(Long id, Long userId) {
        // 1. 校验：本人、已结束（生成中的删除后，迟到的结果会写回已删除的记录）
        validateSpeechFinished(validateMySpeechExists(id, userId));
        // 2. 删除记录：文件保留
        speechMapper.deleteById(id);
    }

    /**
     * 校验本人的语音存在：不是本人的，按不存在处理
     */
    private AiSpeechDO validateMySpeechExists(Long id, Long userId) {
        AiSpeechDO speech = speechMapper.selectById(id);
        if (speech == null || ObjUtil.notEqual(speech.getUserId(), userId)) {
            throw exception(SPEECH_NOT_EXISTS);
        }
        return speech;
    }

    // ========== 语音管理 ==========

    @Override
    public PageResult<AiSpeechDO> getSpeechPage(AiSpeechPageReqVO pageReqVO) {
        return speechMapper.selectPage(pageReqVO);
    }

    @Override
    public AiSpeechDO getSpeech(Long id) {
        return speechMapper.selectById(id);
    }

    @Override
    public void deleteSpeech(Long id) {
        // 1. 校验：存在、已结束（生成中的删除后，迟到的结果会写回已删除的记录）
        validateSpeechFinished(validateSpeechExists(id));
        // 2. 删除记录：文件保留
        speechMapper.deleteById(id);
    }

    private AiSpeechDO validateSpeechExists(Long id) {
        AiSpeechDO speech = speechMapper.selectById(id);
        if (speech == null) {
            throw exception(SPEECH_NOT_EXISTS);
        }
        return speech;
    }

    private static void validateSpeechFinished(AiSpeechDO speech) {
        if (ObjUtil.equal(speech.getStatus(), AiSpeechStatusEnum.IN_PROGRESS.getStatus())) {
            throw exception(SPEECH_STATUS_NOT_FINISHED);
        }
    }

    // ========== 同步 ==========

    @Override
    public int syncSpeech() {
        int count = 0;
        // 1. 查询异步任务的结果；各步互相独立，异常不影响后续步骤
        try {
            count += fetchSpeech();
        } catch (Exception e) {
            log.error("[syncSpeech][查询任务异常]", e);
        }
        // 2. 超时：超过时限未完成的，置为已失败
        try {
            count += timeoutSpeech();
        } catch (Exception e) {
            log.error("[syncSpeech][超时处理异常]", e);
        }
        return count;
    }

    /**
     * 查询生成中、且已有任务编号的异步任务；单个失败时保持生成中，下轮重试，一直失败由超时兜底
     *
     * @return 已结束的数量
     */
    @VisibleForTesting
    int fetchSpeech() {
        List<AiSpeechDO> list = speechMapper.selectListByStatusAndTaskIdNotNull(
                AiSpeechStatusEnum.IN_PROGRESS.getStatus(), SYNC_LIMIT);
        int count = 0;
        for (AiSpeechDO speech : list) {
            try {
                if (fetchSpeech(speech)) {
                    count++;
                }
            } catch (Exception e) {
                log.warn("[fetchSpeech][speech({}) task({}) 查询失败，下轮重试]", speech.getId(), speech.getTaskId(), e);
            }
        }
        return count;
    }

    /**
     * 查询一个异步任务一次：只查询，不重新提交
     *
     * @param speech 语音，任务编号非空
     * @return 是否已结束
     */
    private boolean fetchSpeech(AiSpeechDO speech) {
        // 1. 等待时限内，由合成的线程等待结果，这里不重复查询（避免重复下载、转存）
        if (speech.getCreateTime() != null
                && speech.getCreateTime().isAfter(LocalDateTime.now().minus(TencentSpeechModel.WAIT_TIMEOUT))) {
            return false;
        }

        // 2. 使用提交时绑定的密钥：只停用也照常查询；已删除、平台已变更的，无法查询，置为已失败
        AiApiKeyDO apiKey = apiKeyService.getApiKey(speech.getApiKeyId());
        if (apiKey == null) {
            updateSpeechFail(speech.getId(), "渠道密钥已删除，无法查询任务结果");
            return true;
        }
        if (ObjUtil.notEqual(apiKey.getPlatform(), speech.getPlatform())) {
            updateSpeechFail(speech.getId(), "渠道密钥的平台已变更，无法查询任务结果");
            return true;
        }

        // 3. 查询任务一次：成功的转存；失败的置为已失败；等待、处理中的不变
        TencentTtsApi.TtsTaskResult result = modelService.getTencentTtsApi(apiKey)
                .getTask(speech.getTaskId(), speech.getFormat());
        if (result.getStatus() == TencentTtsApi.TtsTaskStatus.SUCCESS) {
            saveSpeech(speech, result.getAudio());
            return true;
        }
        if (result.getStatus() == TencentTtsApi.TtsTaskStatus.FAILED) {
            updateSpeechFail(speech.getId(), StrUtil.blankToDefault(result.getErrorMessage(), "腾讯云语音合成任务失败"));
            return true;
        }
        return false;
    }

    /**
     * 超时：生成中、从创建时间起超过时限的，置为已失败
     *
     * @return 处理的数量
     */
    @VisibleForTesting
    int timeoutSpeech() {
        LocalDateTime now = LocalDateTime.now();
        List<AiSpeechDO> list = speechMapper.selectListByStatusAndCreateTimeLt(
                AiSpeechStatusEnum.IN_PROGRESS.getStatus(), now.minus(SYNC_TIMEOUT));
        int count = 0;
        for (AiSpeechDO speech : list) {
            if (timeoutSpeech(speech, now)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 超时一条：同步合成、还没拿到任务编号的，按 {@link #SYNC_TIMEOUT}；已有任务编号的，按 {@link #TASK_TIMEOUT}
     *
     * @return 是否已置为已失败
     */
    private boolean timeoutSpeech(AiSpeechDO speech, LocalDateTime now) {
        Duration timeout = speech.getTaskId() != null ? TASK_TIMEOUT : SYNC_TIMEOUT;
        if (speech.getCreateTime() == null || !speech.getCreateTime().isBefore(now.minus(timeout))) {
            return false;
        }
        String errorMessage = speech.getTaskId() != null
                ? StrUtil.format("生成超时：超过 {} 小时未完成", timeout.toHours())
                : StrUtil.format("生成超时：超过 {} 分钟未完成", timeout.toMinutes());
        return updateSpeechFail(speech.getId(), errorMessage) > 0;
    }

    /**
     * 更新为已失败：仍为生成中时才更新，避免覆盖其它执行者的结果
     *
     * @return 影响行数
     */
    private int updateSpeechFail(Long id, String errorMessage) {
        return speechMapper.updateByIdAndStatus(id, AiSpeechStatusEnum.IN_PROGRESS.getStatus(), new AiSpeechDO()
                .setStatus(AiSpeechStatusEnum.FAIL.getStatus()).setFinishTime(LocalDateTime.now())
                .setErrorMessage(StrUtil.sub(errorMessage, 0, 1024)));
    }

    /**
     * 构建失败原因：服务商、文件服务明确给出的原因脱敏后保留；其它异常只给出概括，避免暴露内部信息
     */
    @VisibleForTesting
    static String buildErrorMessage(Exception e) {
        String message = e instanceof IllegalStateException || e instanceof IllegalArgumentException
                || e instanceof ServiceException ? AiSpeechUtils.sanitize(e.getMessage()) : null;
        return StrUtil.blankToDefault(message, "语音生成失败，请稍后重试");
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private AiSpeechServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
