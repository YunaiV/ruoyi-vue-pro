package cn.iocoder.yudao.module.ai.service.asr;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.asr.AiAsrDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiApiKeyDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.mysql.asr.AiAsrMapper;
import cn.iocoder.yudao.module.ai.enums.asr.AiAsrStatusEnum;
import cn.iocoder.yudao.module.ai.enums.model.AiModelTypeEnum;
import cn.iocoder.yudao.module.ai.service.model.AiApiKeyService;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 语音识别 Service 实现类
 *
 * 识别是同步的：不开事务，远程调用结束后只做一次插入，失败记录不会被回滚吞掉
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiAsrServiceImpl implements AiAsrService {

    @Resource
    private AiAsrMapper asrMapper;

    @Resource
    private AiModelService modelService;
    @Resource
    private AiApiKeyService apiKeyService;

    @Resource
    private FileApi fileApi;

    @Override
    public AiAsrDO recognizeAsr(Long userId, AiAsrRecognizeReqVO reqVO) {
        // 1.1 校验模型与其当前密钥
        AiModelDO model = modelService.validateModel(reqVO.getModelId());
        if (ObjUtil.notEqual(model.getType(), AiModelTypeEnum.ASR.getType())) {
            throw exception(MODEL_USE_TYPE_ERROR);
        }
        AiApiKeyDO apiKey = apiKeyService.validateApiKey(model.getKeyId());
        if (ObjUtil.notEqual(model.getPlatform(), apiKey.getPlatform())) {
            throw exception(ASR_MODEL_PLATFORM_MISMATCH, model.getPlatform(), apiKey.getPlatform());
        }
        // 1.2 创建模型：不发请求
        TranscriptionModel transcriptionModel;
        try {
            transcriptionModel = modelService.getTranscriptionModel(apiKey, model.getModel());
        } catch (IllegalArgumentException e) {
            throw exception(ASR_MODEL_NOT_SUPPORTED, model.getName(), e.getMessage());
        }
        // 1.3 下载音频：私有存储时使用预签名地址下载
        byte[] audio = HttpUtil.downloadBytes(fileApi.presignGetUrlQuietly(reqVO.getFileUrl()));

        // 2. 构建记录：文件名取地址里的文件名
        String fileName = FileUtil.getName(HttpUtils.decodeUrlPath(
                HttpUtils.removeUrlPathQueryAndFragment(reqVO.getFileUrl())));
        AiAsrDO asr = new AiAsrDO().setUserId(userId).setModelId(model.getId()).setModelName(model.getName())
                .setPlatform(model.getPlatform()).setModel(model.getModel())
                .setFileName(fileName).setAudioUrl(reqVO.getFileUrl());

        // 3.1 同步识别：音频格式、大小、采样率由各平台的模型在调用前校验，其它参数由平台返回错误
        long startTime = System.currentTimeMillis();
        try {
            String text = transcriptionModel.call(new AudioTranscriptionPrompt(new ByteArrayResource(audio)))
                    .getResult().getOutput();
            asr.setStatus(AiAsrStatusEnum.SUCCESS.getStatus()).setOriginalText(StrUtil.nullToEmpty(text));
        } catch (IllegalArgumentException ex) { // 音频校验不通过：消息不含敏感信息
            asr.setStatus(AiAsrStatusEnum.FAIL.getStatus()).setErrorMessage("音频不正确：" + ex.getMessage());
        } catch (IllegalStateException ex) { // 平台返回失败：消息已脱敏
            log.warn("[recognizeAsr][userId({}) modelId({}) 识别失败]", userId, model.getId(), ex);
            asr.setStatus(AiAsrStatusEnum.FAIL.getStatus()).setErrorMessage(ex.getMessage());
        } catch (Exception ex) {
            // 非预期异常：消息可能包含请求地址等敏感信息，只记录异常类型
            log.error("[recognizeAsr][userId({}) modelId({}) 识别异常({})]", userId, model.getId(), ex.getClass().getName());
            asr.setStatus(AiAsrStatusEnum.FAIL.getStatus()).setErrorMessage("识别服务异常，请稍后重试");
        }
        // 3.2 记录：成功、失败都保存
        asr.setDuration((int) (System.currentTimeMillis() - startTime));
        asrMapper.insert(asr);
        return asr;
    }

    @Override
    public PageResult<AiAsrDO> getAsrMyPage(Long userId, PageParam pageReqVO) {
        return asrMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public AiAsrDO getAsrMy(Long id, Long userId) {
        return validateAsrMyExists(id, userId);
    }

    @Override
    public void deleteAsrMy(Long id, Long userId) {
        // 校验存在
        validateAsrMyExists(id, userId);
        // 删除：逻辑删除，音频文件保留
        asrMapper.deleteById(id);
    }

    private AiAsrDO validateAsrMyExists(Long id, Long userId) {
        AiAsrDO asr = asrMapper.selectById(id);
        if (asr == null || ObjUtil.notEqual(asr.getUserId(), userId)) {
            throw exception(ASR_NOT_EXISTS);
        }
        return asr;
    }

    // ========== 语音识别管理 ==========

    @Override
    public PageResult<AiAsrDO> getAsrPage(AiAsrPageReqVO pageReqVO) {
        return asrMapper.selectPage(pageReqVO);
    }

    @Override
    public AiAsrDO getAsr(Long id) {
        return asrMapper.selectById(id);
    }

    @Override
    public void deleteAsr(Long id) {
        // 校验存在
        validateAsrExists(id);
        // 删除：逻辑删除，音频文件保留
        asrMapper.deleteById(id);
    }

    private void validateAsrExists(Long id) {
        if (asrMapper.selectById(id) == null) {
            throw exception(ASR_NOT_EXISTS);
        }
    }

}
