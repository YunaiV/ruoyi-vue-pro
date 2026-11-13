package cn.iocoder.yudao.module.ai.service.ocr;

import cn.hutool.http.HttpUtil;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrUpdateReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ocr.AiOcrDO;
import cn.iocoder.yudao.module.ai.dal.mysql.ocr.AiOcrMapper;
import cn.iocoder.yudao.module.ai.enums.model.AiPlatformEnum;
import cn.iocoder.yudao.module.ai.enums.ocr.AiOcrStatusEnum;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrException;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrModel;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResponse;
import cn.iocoder.yudao.module.ai.framework.ai.core.ocr.OcrResult;
import cn.iocoder.yudao.module.ai.service.model.AiModelService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link AiOcrServiceImpl} 的单元测试类
 *
 * 重点：图片按文件内容校验、成功失败都记录且失败原因脱敏、【我的】记录隔离、修订文本的空字符串语义
 *
 * @author 芋道源码
 */
public class AiOcrServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long MODEL_ID = 10L;
    private static final Long OCR_ID = 100L;
    private static final String FILE_URL = "https://files.example.com/ai/ocr/%E5%8F%91%E7%A5%A8_1731.png";
    private static final String ACCESS_URL = FILE_URL + "?sign=1";

    @InjectMocks
    private AiOcrServiceImpl ocrService;

    @Mock
    private AiOcrMapper ocrMapper;
    @Mock
    private AiModelService modelService;
    @Mock
    private FileApi fileApi;

    // ========== recognizeOcr ==========

    @Test
    public void testRecognizeOcr_success() {
        // mock 方法：按预签名地址下载，识别出文字
        mockModel(prompt -> {
            assertEquals("image/png", prompt.getInstructions().getMimeType().toString());
            return new OcrResponse(new OcrResult("芋道源码\n2026"));
        });

        // 调用
        AiOcrDO ocr = recognizeOcr(png());
        // 断言：记录模型快照、文件地址（不带签名）、原文；未修订
        AiOcrDO inserted = captureInsert();
        assertSame(ocr, inserted);
        assertEquals(USER_ID, inserted.getUserId());
        assertEquals(MODEL_ID, inserted.getModelId());
        assertEquals("腾讯云高精度", inserted.getModelName());
        assertEquals(AiPlatformEnum.TENCENT.getPlatform(), inserted.getPlatform());
        assertEquals("GeneralAccurateOCR", inserted.getModel());
        assertEquals("发票_1731.png", inserted.getFileName());
        assertEquals(FILE_URL, inserted.getImageUrl());
        assertEquals(AiOcrStatusEnum.SUCCESS.getStatus(), inserted.getStatus());
        assertEquals("芋道源码\n2026", inserted.getOriginalText());
        assertNull(inserted.getEditedText());
        assertNull(inserted.getErrorMessage());
        assertNotNull(inserted.getDuration());
    }

    @Test
    public void testRecognizeOcr_jpegNoText() {
        // mock 方法：图片中没有文字
        mockModel(prompt -> {
            assertEquals("image/jpeg", prompt.getInstructions().getMimeType().toString());
            return new OcrResponse(new OcrResult(""));
        });

        // 调用
        recognizeOcr(jpeg());
        // 断言：没有文字是空字符串，不是失败
        AiOcrDO inserted = captureInsert();
        assertEquals(AiOcrStatusEnum.SUCCESS.getStatus(), inserted.getStatus());
        assertEquals("", inserted.getOriginalText());
    }

    @Test
    public void testRecognizeOcr_ocrException() {
        // mock 方法：厂商超时
        OcrException ex = new OcrException("TENCENT", OcrException.Type.TIMEOUT, "连接或读取超时");
        mockModel(prompt -> {
            throw ex;
        });

        // 调用
        AiOcrDO ocr = recognizeOcr(png());
        // 断言：失败也记录，失败原因为已脱敏的异常消息
        AiOcrDO inserted = captureInsert();
        assertSame(ocr, inserted);
        assertEquals(AiOcrStatusEnum.FAIL.getStatus(), inserted.getStatus());
        assertEquals(FILE_URL, inserted.getImageUrl());
        assertNull(inserted.getOriginalText());
        assertEquals(ex.getMessage(), inserted.getErrorMessage());
    }

    @Test
    public void testRecognizeOcr_unexpectedException() {
        // mock 方法：非预期异常，消息里带有敏感信息
        mockModel(prompt -> {
            throw new IllegalStateException("https://aip.baidubce.com/x?access_token=secret-token");
        });

        // 调用
        recognizeOcr(png());
        // 断言：只给通用提示，不落库原始消息
        AiOcrDO inserted = captureInsert();
        assertEquals(AiOcrStatusEnum.FAIL.getStatus(), inserted.getStatus());
        assertEquals("识别服务异常，请稍后重试", inserted.getErrorMessage());
    }

    @Test
    public void testRecognizeOcr_fakeImage() {
        // 调用，并断言异常：地址是 png，内容是文本
        assertImageInvalid("hello world".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testRecognizeOcr_pdf() {
        // 调用，并断言异常
        assertImageInvalid("%PDF-1.7\n%âãÏÓ\n1 0 obj\n".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testRecognizeOcr_tooLarge() {
        // 调用，并断言异常
        assertImageInvalid(Arrays.copyOf(png(), AiOcrServiceImpl.IMAGE_MAX_SIZE_MB * 1024 * 1024 + 1));
    }

    @Test
    public void testRecognizeOcr_empty() {
        // 调用，并断言异常
        assertImageInvalid(new byte[0]);
    }

    @Test
    public void testRecognizeOcr_modelInvalid() {
        // mock 方法：模型配置不正确
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID));
        when(modelService.getOcrModel(MODEL_ID)).thenThrow(exception(OCR_MODEL_PLATFORM_NOT_SUPPORTED, "OPENAI"));

        // 调用，并断言异常：不记录
        assertThrows(ServiceException.class, () -> recognizeOcr(png()));
        verifyNoInteractions(ocrMapper);
    }

    // ========== getOcrMyPage、getOcrMy ==========

    @Test
    public void testGetOcrMyPage() {
        // mock 方法
        PageParam pageParam = new PageParam();
        PageResult<AiOcrDO> pageResult = new PageResult<>();
        when(ocrMapper.selectPageByUserId(USER_ID, pageParam)).thenReturn(pageResult);

        // 调用，并断言
        assertSame(pageResult, ocrService.getOcrMyPage(USER_ID, pageParam));
    }

    @Test
    public void testGetOcrMy_success() {
        // mock 数据
        AiOcrDO ocr = mockOcr(USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用，并断言
        assertSame(ocr, ocrService.getOcrMy(OCR_ID, USER_ID));
    }

    @Test
    public void testGetOcrMy_otherUser() {
        // mock 数据：别人的记录
        mockOcr(OTHER_USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用，并断言异常
        assertServiceException(() -> ocrService.getOcrMy(OCR_ID, USER_ID), OCR_NOT_EXISTS);
    }

    @Test
    public void testGetOcrMy_notExists() {
        // 调用，并断言异常
        assertServiceException(() -> ocrService.getOcrMy(OCR_ID, USER_ID), OCR_NOT_EXISTS);
    }

    // ========== updateOcrMy ==========

    @Test
    public void testUpdateOcrMy_emptyString() {
        // mock 数据
        mockOcr(USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用：清空内容
        ocrService.updateOcrMy(buildUpdateReqVO(""), USER_ID);
        // 断言：空字符串原样保存，不转成 null；只更新修订文本
        AiOcrDO updateObj = captureUpdate();
        assertEquals(OCR_ID, updateObj.getId());
        assertEquals("", updateObj.getEditedText());
        assertNull(updateObj.getUserId());
        assertNull(updateObj.getStatus());
        assertNull(updateObj.getOriginalText());
    }

    @Test
    public void testUpdateOcrMy_success() {
        // mock 数据
        mockOcr(USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用
        ocrService.updateOcrMy(buildUpdateReqVO("修订后的文本"), USER_ID);
        // 断言
        assertEquals("修订后的文本", captureUpdate().getEditedText());
    }

    @Test
    public void testUpdateOcrMy_otherUser() {
        // mock 数据：别人的记录
        mockOcr(OTHER_USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用，并断言异常
        assertServiceException(() -> ocrService.updateOcrMy(buildUpdateReqVO("x"), USER_ID), OCR_NOT_EXISTS);
        verify(ocrMapper, never()).updateById(any(AiOcrDO.class));
    }

    @Test
    public void testUpdateOcrMy_notSuccess() {
        // mock 数据：识别失败的记录
        mockOcr(USER_ID, AiOcrStatusEnum.FAIL);

        // 调用，并断言异常
        assertServiceException(() -> ocrService.updateOcrMy(buildUpdateReqVO("x"), USER_ID), OCR_STATUS_NOT_SUCCESS);
        verify(ocrMapper, never()).updateById(any(AiOcrDO.class));
    }

    // ========== deleteOcrMy ==========

    @Test
    public void testDeleteOcrMy_success() {
        // mock 数据
        mockOcr(USER_ID, AiOcrStatusEnum.FAIL);

        // 调用
        ocrService.deleteOcrMy(OCR_ID, USER_ID);
        // 断言
        verify(ocrMapper).deleteById(OCR_ID);
    }

    @Test
    public void testDeleteOcrMy_otherUser() {
        // mock 数据：别人的记录
        mockOcr(OTHER_USER_ID, AiOcrStatusEnum.SUCCESS);

        // 调用，并断言异常
        assertServiceException(() -> ocrService.deleteOcrMy(OCR_ID, USER_ID), OCR_NOT_EXISTS);
        verify(ocrMapper, never()).deleteById(OCR_ID);
    }

    // ========== 工具方法 ==========

    /**
     * 调用识别：文件服务返回预签名地址，按预签名地址下载出 content
     */
    private AiOcrDO recognizeOcr(byte[] content) {
        when(fileApi.presignGetUrlQuietly(FILE_URL)).thenReturn(ACCESS_URL);
        AiOcrRecognizeReqVO reqVO = new AiOcrRecognizeReqVO();
        reqVO.setModelId(MODEL_ID);
        reqVO.setFileUrl(FILE_URL);
        try (MockedStatic<HttpUtil> httpUtilMock = mockStatic(HttpUtil.class)) {
            httpUtilMock.when(() -> HttpUtil.downloadBytes(ACCESS_URL)).thenReturn(content);
            return ocrService.recognizeOcr(USER_ID, reqVO);
        }
    }

    /**
     * 断言图片不合法：不校验模型、不记录
     */
    private void assertImageInvalid(byte[] content) {
        Executable executable = () -> recognizeOcr(content);
        assertServiceException(executable, OCR_IMAGE_INVALID, AiOcrServiceImpl.IMAGE_MAX_SIZE_MB);
        verifyNoInteractions(modelService, ocrMapper);
    }

    private void mockModel(OcrModel ocrModel) {
        when(modelService.validateModel(MODEL_ID)).thenReturn(new AiModelDO().setId(MODEL_ID).setName("腾讯云高精度")
                .setPlatform(AiPlatformEnum.TENCENT.getPlatform()).setModel("GeneralAccurateOCR"));
        when(modelService.getOcrModel(MODEL_ID)).thenReturn(ocrModel);
    }

    private AiOcrDO mockOcr(Long userId, AiOcrStatusEnum status) {
        AiOcrDO ocr = new AiOcrDO().setId(OCR_ID).setUserId(userId).setStatus(status.getStatus())
                .setOriginalText("原文");
        when(ocrMapper.selectById(OCR_ID)).thenReturn(ocr);
        return ocr;
    }

    private AiOcrDO captureInsert() {
        ArgumentCaptor<AiOcrDO> captor = ArgumentCaptor.forClass(AiOcrDO.class);
        verify(ocrMapper).insert(captor.capture());
        return captor.getValue();
    }

    private AiOcrDO captureUpdate() {
        ArgumentCaptor<AiOcrDO> captor = ArgumentCaptor.forClass(AiOcrDO.class);
        verify(ocrMapper).updateById(captor.capture());
        return captor.getValue();
    }

    private static AiOcrUpdateReqVO buildUpdateReqVO(String editedText) {
        AiOcrUpdateReqVO reqVO = new AiOcrUpdateReqVO();
        reqVO.setId(OCR_ID);
        reqVO.setEditedText(editedText);
        return reqVO;
    }

    private static byte[] png() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13, 'I', 'H', 'D', 'R',
                0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0};
    }

    private static byte[] jpeg() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F', 0,
                1, 1, 0, 0, 1, 0, 1, 0, 0};
    }

}
