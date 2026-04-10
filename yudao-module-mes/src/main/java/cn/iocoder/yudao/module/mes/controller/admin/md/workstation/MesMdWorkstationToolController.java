package cn.iocoder.yudao.module.mes.controller.admin.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.tool.MesMdWorkstationToolRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.tool.MesMdWorkstationToolSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationToolDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.tm.tool.MesTmToolTypeDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationToolService;
import cn.iocoder.yudao.module.mes.service.tm.tool.MesTmToolTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 工装夹具资源")
@RestController
@RequestMapping("/mes/md-workstation-tool")
@Validated
public class MesMdWorkstationToolController {

    @Resource
    private MesMdWorkstationToolService workstationToolService;

    @Resource
    private MesTmToolTypeService toolTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建工装夹具资源")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Long> createWorkstationTool(@Valid @RequestBody MesMdWorkstationToolSaveReqVO createReqVO) {
        return success(workstationToolService.createWorkstationTool(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工装夹具资源")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> updateWorkstationTool(@Valid @RequestBody MesMdWorkstationToolSaveReqVO updateReqVO) {
        workstationToolService.updateWorkstationTool(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工装夹具资源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> deleteWorkstationTool(@RequestParam("id") Long id) {
        workstationToolService.deleteWorkstationTool(id);
        return success(true);
    }

    @GetMapping("/list-by-workstation")
    @Operation(summary = "获得工装夹具资源列表")
    @Parameter(name = "workstationId", description = "工作站编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:query')")
    public CommonResult<List<MesMdWorkstationToolRespVO>> getWorkstationToolList(
            @RequestParam("workstationId") Long workstationId) {
        List<MesMdWorkstationToolDO> list = workstationToolService.getWorkstationToolListByWorkstationId(workstationId);
        return success(buildWorkstationToolRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesMdWorkstationToolRespVO> buildWorkstationToolRespVOList(List<MesMdWorkstationToolDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取工具类型信息
        Map<Long, MesTmToolTypeDO> toolTypeMap = toolTypeService.getToolTypeMap(
                convertSet(list, MesMdWorkstationToolDO::getToolTypeId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdWorkstationToolRespVO.class, vo ->
                MapUtils.findAndThen(toolTypeMap, vo.getToolTypeId(),
                        toolType -> vo.setToolTypeName(toolType.getName())));
    }

}
