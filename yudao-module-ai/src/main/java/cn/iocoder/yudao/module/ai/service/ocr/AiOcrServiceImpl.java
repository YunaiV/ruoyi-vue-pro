package cn.iocoder.yudao.module.ai.service.ocr;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.http.HttpUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ocr.AiOcrDO;
import cn.iocoder.yudao.module.ai.dal.mysql.ocr.AiOcrMapper;
import cn.iocoder.yudao.module.ai.enums.ocr.AiOcrStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrPrompt;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.ai.util.FileTypeUtils;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.validation.annotation.Validated;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;

/**
 * AI 文字识别 Service 实现类
 *
 * 识别是同步的：不开事务，远程调用结束后只做一次插入，失败记录不会被回滚吞掉
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class AiOcrServiceImpl implements AiOcrService {

    @Resource
    private AiOcrMapper ocrMapper;

    @Resource
    private AiModelService modelService;

    @Resource
    private FileApi fileApi;

    @Override
    public AiOcrDO recognizeOcr(Long userId, AiOcrRecognizeReqVO reqVO) {
        // 1.1 下载图片：私有存储时使用预签名地址下载；不限制大小、类型，由识别服务判断
        byte[] content = HttpUtil.downloadBytes(fileApi.presignGetUrlQuietly(reqVO.getFileUrl()));
        String mimeType = FileTypeUtils.getMineType(content);
        // 1.2 校验模型：模型、密钥的状态、类型、平台，以及密钥格式
        AiModelDO model = modelService.validateModel(reqVO.getModelId());
        OcrModel ocrModel = modelService.getOcrModel(model.getId());

        // 2. 构建记录：文件名取地址里的文件名
        String fileName = FileUtil.getName(HttpUtils.decodeUrlPath(
                HttpUtils.removeUrlPathQueryAndFragment(reqVO.getFileUrl())));
        AiOcrDO ocr = new AiOcrDO().setUserId(userId).setModelId(model.getId()).setModelName(model.getName())
                .setPlatform(model.getPlatform()).setModel(model.getModel())
                .setFileName(fileName).setImageUrl(reqVO.getFileUrl());

        // 3.1 同步识别：使用下载的图片内容
        long startTime = System.currentTimeMillis();
        try {
            String text = ocrModel.call(new OcrPrompt(new Media(MimeType.valueOf(mimeType),
                    new ByteArrayResource(content)))).getResult().getOutput();
            ocr.setStatus(AiOcrStatusEnum.SUCCESS.getStatus()).setOriginalText(text);
        } catch (OcrException ex) { // 异常消息已脱敏，可直接展示
            ocr.setStatus(AiOcrStatusEnum.FAIL.getStatus()).setErrorMessage(ex.getMessage());
        } catch (Exception ex) {
            // 非预期异常：消息可能包含请求地址等敏感信息，只记录异常类型
            log.error("[recognizeOcr][userId({}) modelId({}) 识别异常({})]", userId, model.getId(), ex.getClass().getName());
            ocr.setStatus(AiOcrStatusEnum.FAIL.getStatus()).setErrorMessage("识别服务异常，请稍后重试");
        }
        // 3.2 记录：成功、失败都保存
        ocr.setDuration((int) (System.currentTimeMillis() - startTime));
        ocrMapper.insert(ocr);
        return ocr;
    }

    @Override
    public PageResult<AiOcrDO> getOcrMyPage(Long userId, PageParam pageReqVO) {
        return ocrMapper.selectPageByUserId(userId, pageReqVO);
    }

    @Override
    public AiOcrDO getOcrMy(Long id, Long userId) {
        return validateOcrMyExists(id, userId);
    }

    @Override
    public AiOcrDO updateOcrMy(AiOcrUpdateReqVO updateReqVO, Long userId) {
        // 1. 校验
        AiOcrDO ocr = validateOcrMyExists(updateReqVO.getId(), userId);
        if (ObjUtil.notEqual(ocr.getStatus(), AiOcrStatusEnum.SUCCESS.getStatus())) {
            throw exception(OCR_STATUS_NOT_SUCCESS);
        }

        // 2. 更新：修订文本已由 VO 校验非 null，空字符串（用户清空）原样保存
        AiOcrDO updateObj = BeanUtils.toBean(updateReqVO, AiOcrDO.class);
        ocrMapper.updateById(updateObj);
        return ocrMapper.selectById(ocr.getId());
    }

    @Override
    public void deleteOcrMy(Long id, Long userId) {
        // 校验存在
        validateOcrMyExists(id, userId);
        // 删除：逻辑删除，图片文件保留
        ocrMapper.deleteById(id);
    }

    private AiOcrDO validateOcrMyExists(Long id, Long userId) {
        AiOcrDO ocr = ocrMapper.selectById(id);
        if (ocr == null || ObjUtil.notEqual(ocr.getUserId(), userId)) {
            throw exception(OCR_NOT_EXISTS);
        }
        return ocr;
    }

    // ========== OCR 管理 ==========

    @Override
    public PageResult<AiOcrDO> getOcrPage(AiOcrPageReqVO pageReqVO) {
        return ocrMapper.selectPage(pageReqVO);
    }

    @Override
    public AiOcrDO getOcr(Long id) {
        return ocrMapper.selectById(id);
    }

    @Override
    public void deleteOcr(Long id) {
        // 校验存在
        validateOcrExists(id);
        // 删除：逻辑删除，图片文件保留
        ocrMapper.deleteById(id);
    }

    private void validateOcrExists(Long id) {
        if (ocrMapper.selectById(id) == null) {
            throw exception(OCR_NOT_EXISTS);
        }
    }

}
