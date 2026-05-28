package cn.iocoder.yudao.module.yaya.service.content;

import cn.iocoder.yudao.module.yaya.service.content.vo.YayaImportResultResp;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YayaImportServiceImplTest {

    private final YayaImportServiceImpl importService = new YayaImportServiceImpl();

    @Test
    void previewImportShouldReturnEmptyPreviewUntilPipelineIsWired() {
        YayaImportResultResp result = importService.previewImport("26Q1");

        assertEquals("26Q1", result.getSeasonKey());
        assertEquals(0, result.getTopics());
        assertEquals(0, result.getQuestions());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void runImportShouldFailUntilPipelineIsWired() {
        assertServiceException(() -> importService.runImport("26Q1"), NOT_IMPLEMENTED);
    }

}
