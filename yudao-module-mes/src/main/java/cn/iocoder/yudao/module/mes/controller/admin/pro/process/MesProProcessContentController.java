package cn.iocoder.yudao.module.mes.controller.admin.pro.process;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content.MesProProcessContentRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.process.vo.content.MesProProcessContentSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessContentDO;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 生产工序内容")
@RestController
@RequestMapping("/mes/pro/process-content")
@Validated
public class MesProProcessContentController {

    @Resource
    private MesProProcessContentService processContentService;

    @PostMapping("/create")
    @Operation(summary = "创建生产工序内容")
    @PreAuthorize("@ss.hasPermission('mes:pro-process:update')")
    public CommonResult<Long> createProcessContent(@Valid @RequestBody MesProProcessContentSaveReqVO createReqVO) {
        return success(processContentService.createProcessContent(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新生产工序内容")
    @PreAuthorize("@ss.hasPermission('mes:pro-process:update')")
    public CommonResult<Boolean> updateProcessContent(@Valid @RequestBody MesProProcessContentSaveReqVO updateReqVO) {
        processContentService.updateProcessContent(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除生产工序内容")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-process:update')")
    public CommonResult<Boolean> deleteProcessContent(@RequestParam("id") Long id) {
        processContentService.deleteProcessContent(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得生产工序内容")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-process:query')")
    public CommonResult<MesProProcessContentRespVO> getProcessContent(@RequestParam("id") Long id) {
        MesProProcessContentDO content = processContentService.getProcessContent(id);
        return success(BeanUtils.toBean(content, MesProProcessContentRespVO.class));
    }

    @GetMapping("/list-by-process")
    @Operation(summary = "获得生产工序内容列表（按工序编号）")
    @Parameter(name = "processId", description = "工序编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-process:query')")
    public CommonResult<List<MesProProcessContentRespVO>> getProcessContentListByProcessId(@RequestParam("processId") Long processId) {
        List<MesProProcessContentDO> list = processContentService.getProcessContentListByProcessId(processId);
        return success(BeanUtils.toBean(list, MesProProcessContentRespVO.class));
    }

}
