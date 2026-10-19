package cn.iocoder.yudao.module.pms.controller.admin.kb.interaction;

import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.pms.controller.admin.kb.interaction.vo.share.PmsKnowledgeDocumentSharePublicRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.kb.content.PmsKnowledgeDocumentDO;
import cn.iocoder.yudao.module.pms.enums.kb.content.PmsKnowledgeDocumentTypeEnum;
import cn.iocoder.yudao.module.pms.service.kb.interaction.PmsKnowledgeDocumentShareService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PmsKnowledgeDocumentShareController} 的单元测试类
 *
 * @author 芋道源码
 */
public class PmsKnowledgeDocumentShareControllerTest {

    @Test
    public void testGetDocumentByToken_fileUsesDefaultPreviewUrl() {
        // mock 数据
        String token = "share-token";
        String rawUrl = "https://file.example.com/private/demo.pdf";
        String previewUrl = "https://file.example.com/private/demo.pdf?signature=temporary";
        PmsKnowledgeDocumentDO document = new PmsKnowledgeDocumentDO().setId(1L).setTitle("测试文件")
                .setType(PmsKnowledgeDocumentTypeEnum.FILE.getType()).setContent(rawUrl).setFileType("pdf");
        PmsKnowledgeDocumentShareService shareService = mock(PmsKnowledgeDocumentShareService.class);
        FileApi fileApi = mock(FileApi.class);
        when(shareService.getDocumentByShareToken(token)).thenReturn(document);
        when(fileApi.presignGetUrl(rawUrl, null)).thenReturn(previewUrl);
        PmsKnowledgeDocumentShareController controller = new PmsKnowledgeDocumentShareController();
        ReflectionTestUtils.setField(controller, "documentShareService", shareService);
        ReflectionTestUtils.setField(controller, "fileApi", fileApi);

        // 调用
        PmsKnowledgeDocumentSharePublicRespVO result = controller.getDocumentByToken(token).getData();

        // 断言
        assertNull(result.getContent());
        assertEquals(previewUrl, result.getPreviewUrl());
        verify(fileApi).presignGetUrl(rawUrl, null);
    }

}
