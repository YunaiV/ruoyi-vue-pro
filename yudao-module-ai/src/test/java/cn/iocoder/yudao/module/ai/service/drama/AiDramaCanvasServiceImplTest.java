package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaCanvasSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaDirectorSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCanvasDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaDirectorDO;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaCanvasMapper;
import cn.iocoder.yudao.module.ai.dal.mysql.drama.AiDramaDirectorMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.ai.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link AiDramaCanvasServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
public class AiDramaCanvasServiceImplTest extends BaseMockitoUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;

    @InjectMocks
    private AiDramaCanvasServiceImpl canvasService;

    @Mock
    private AiDramaCanvasMapper canvasMapper;
    @Mock
    private AiDramaDirectorMapper directorMapper;
    @Mock
    private AiDramaProjectService projectService;

    // ========== 画布 ==========

    @Test
    public void testSaveCanvasMy_first() {
        assertEquals(1L, canvasService.saveCanvasMy(USER_ID, canvasReqVO(0L, document())));

        verify(canvasMapper).insert(any(AiDramaCanvasDO.class));
    }

    @Test
    public void testSaveCanvasMy_firstConcurrent() {
        when(canvasMapper.insert(any(AiDramaCanvasDO.class))).thenThrow(new DuplicateKeyException("uk_project_id"));

        assertServiceException(() -> canvasService.saveCanvasMy(USER_ID, canvasReqVO(0L, document())),
                DRAMA_CANVAS_REVISION_CONFLICT);
    }

    @Test
    public void testSaveCanvasMy_update() {
        when(canvasMapper.selectByProjectId(PROJECT_ID)).thenReturn(new AiDramaCanvasDO().setId(5L).setRevision(3L));
        when(canvasMapper.updateByIdAndRevision(eq(5L), eq(3L), any())).thenReturn(1);

        assertEquals(4L, canvasService.saveCanvasMy(USER_ID, canvasReqVO(3L, document())));
    }

    @Test
    public void testSaveCanvasMy_revisionConflict() {
        when(canvasMapper.selectByProjectId(PROJECT_ID)).thenReturn(new AiDramaCanvasDO().setId(5L).setRevision(4L));

        assertServiceException(() -> canvasService.saveCanvasMy(USER_ID, canvasReqVO(3L, document())),
                DRAMA_CANVAS_REVISION_CONFLICT);
        verify(canvasMapper, never()).updateByIdAndRevision(any(), any(), any());
    }

    @Test
    public void testValidateCanvasDocument_cycle() {
        Map<String, Object> document = Map.of("schema", 1,
                "nodes", List.of(node("a", "text"), node("b", "image")),
                "edges", List.of(Map.of("id", "e1", "source", "a", "target", "b"),
                        Map.of("id", "e2", "source", "b", "target", "a")));

        assertServiceException(() -> AiDramaCanvasServiceImpl.validateCanvasDocument(JsonUtils.parseTree(
                JsonUtils.toJsonString(document))), DRAMA_CANVAS_INVALID, "连线不能形成循环");
    }

    @Test
    public void testValidateCanvasDocument_edgeToGroup() {
        Map<String, Object> document = Map.of("schema", 1,
                "nodes", List.of(node("a", "text"), node("g", "group")),
                "edges", List.of(Map.of("id", "e1", "source", "a", "target", "g")));

        assertServiceException(() -> AiDramaCanvasServiceImpl.validateCanvasDocument(JsonUtils.parseTree(
                JsonUtils.toJsonString(document))), DRAMA_CANVAS_INVALID, "连线端点无效");
    }

    // ========== 3D 导演 ==========

    @Test
    public void testSaveDirectorMy_nodeNotInCanvas() {
        // 画布中的同名节点不是预演节点
        when(canvasMapper.selectByProjectId(PROJECT_ID)).thenReturn(new AiDramaCanvasDO().setId(5L).setRevision(1L)
                .setDocument(document()));
        AiDramaDirectorSaveReqVO reqVO = new AiDramaDirectorSaveReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setNodeId("a");
        reqVO.setRevision(0L);
        reqVO.setDocument(Map.of("schema", 1));

        assertServiceException(() -> canvasService.saveDirectorMy(USER_ID, reqVO), DRAMA_DIRECTOR_INVALID,
                "预演节点不在画布中，请先保存画布");
        verify(directorMapper, never()).insert(any(AiDramaDirectorDO.class));
    }

    // ========== 工具 ==========

    private static Map<String, Object> document() {
        return Map.of("schema", 1, "nodes", List.of(node("a", "text"), node("b", "previs")),
                "edges", List.of(Map.of("id", "e1", "source", "a", "target", "b")));
    }

    private static Map<String, Object> node(String id, String kind) {
        return Map.of("id", id, "kind", kind, "data", Map.of(), "position", Map.of("x", 0, "y", 0));
    }

    private static AiDramaCanvasSaveReqVO canvasReqVO(Long revision, Map<String, Object> document) {
        AiDramaCanvasSaveReqVO reqVO = new AiDramaCanvasSaveReqVO();
        reqVO.setProjectId(PROJECT_ID);
        reqVO.setRevision(revision);
        reqVO.setDocument(document);
        return reqVO;
    }

}
