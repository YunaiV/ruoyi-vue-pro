package cn.iocoder.yudao.module.ai.controller.admin.drama;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaCanvasRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaCanvasSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaDirectorRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaDirectorSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCanvasDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaDirectorDO;
import cn.iocoder.yudao.module.ai.service.drama.AiDramaCanvasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 短剧画布")
@RestController
@RequestMapping("/ai/drama/canvas")
public class AiDramaCanvasController {

    @Resource
    private AiDramaCanvasService canvasService;

    @GetMapping("/get")
    @Operation(summary = "获得【我的】项目的画布", description = "未保存过时，修订号为 0、文档为空")
    @Parameter(name = "projectId", required = true, description = "项目编号", example = "1024")
    public CommonResult<AiDramaCanvasRespVO> getCanvas(@RequestParam("projectId") Long projectId) {
        AiDramaCanvasDO canvas = canvasService.getCanvasMy(getLoginUserId(), projectId);
        AiDramaCanvasRespVO respVO = new AiDramaCanvasRespVO();
        respVO.setRevision(canvas != null ? canvas.getRevision() : 0L);
        respVO.setDocument(canvas != null ? canvas.getDocument() : null);
        return success(respVO);
    }

    @PutMapping("/save")
    @Operation(summary = "保存【我的】项目的画布", description = "修订号与当前不一致时拒绝；返回新的修订号")
    public CommonResult<Long> saveCanvas(@Valid @RequestBody AiDramaCanvasSaveReqVO reqVO) {
        return success(canvasService.saveCanvasMy(getLoginUserId(), reqVO));
    }

    @GetMapping("/get-director")
    @Operation(summary = "获得【我的】项目的 3D 导演工程", description = "未保存过时，修订号为 0、文档为空")
    @Parameter(name = "projectId", required = true, description = "项目编号", example = "1024")
    @Parameter(name = "nodeId", required = true, description = "画布预演节点编号", example = "n-1f0e")
    public CommonResult<AiDramaDirectorRespVO> getDirector(@RequestParam("projectId") Long projectId,
                                                         @RequestParam("nodeId") String nodeId) {
        AiDramaDirectorDO director = canvasService.getDirectorMy(getLoginUserId(), projectId, nodeId);
        AiDramaDirectorRespVO respVO = new AiDramaDirectorRespVO();
        respVO.setRevision(director != null ? director.getRevision() : 0L);
        respVO.setDocument(director != null ? director.getDocument() : null);
        return success(respVO);
    }

    @PutMapping("/save-director")
    @Operation(summary = "保存【我的】项目的 3D 导演工程", description = "预演节点需已保存在画布中；修订号与当前不一致时拒绝；返回新的修订号")
    public CommonResult<Long> saveDirector(@Valid @RequestBody AiDramaDirectorSaveReqVO reqVO) {
        return success(canvasService.saveDirectorMy(getLoginUserId(), reqVO));
    }

}
