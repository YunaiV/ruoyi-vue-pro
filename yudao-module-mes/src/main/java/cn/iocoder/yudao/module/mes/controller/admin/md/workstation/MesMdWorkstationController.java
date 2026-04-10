package cn.iocoder.yudao.module.mes.controller.admin.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.MesMdWorkstationSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkshopService;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - MES 工作站")
@RestController
@RequestMapping("/mes/md-workstation")
@Validated
public class MesMdWorkstationController {

    @Resource
    private MesMdWorkstationService workstationService;
    @Resource
    private MesMdWorkshopService workshopService;
    @Resource
    private MesProProcessService processService;

    @PostMapping("/create")
    @Operation(summary = "创建工作站")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:create')")
    public CommonResult<Long> createWorkstation(@Valid @RequestBody MesMdWorkstationSaveReqVO createReqVO) {
        return success(workstationService.createWorkstation(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新工作站")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> updateWorkstation(@Valid @RequestBody MesMdWorkstationSaveReqVO updateReqVO) {
        workstationService.updateWorkstation(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作站")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:delete')")
    public CommonResult<Boolean> deleteWorkstation(@RequestParam("id") Long id) {
        workstationService.deleteWorkstation(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得工作站")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:query')")
    public CommonResult<MesMdWorkstationRespVO> getWorkstation(@RequestParam("id") Long id) {
        MesMdWorkstationDO workstation = workstationService.getWorkstation(id);
        return success(BeanUtils.toBean(workstation, MesMdWorkstationRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得工作站分页")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:query')")
    public CommonResult<PageResult<MesMdWorkstationRespVO>> getWorkstationPage(@Valid MesMdWorkstationPageReqVO pageReqVO) {
        PageResult<MesMdWorkstationDO> pageResult = workstationService.getWorkstationPage(pageReqVO);
        return success(new PageResult<>(buildWorkstationRespVOList(pageResult.getList()), pageResult.getTotal()));
    }


    @GetMapping("/export-excel")
    @Operation(summary = "导出工作站 Excel")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkstationExcel(@Valid MesMdWorkstationPageReqVO pageReqVO,
                                        HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdWorkstationDO> list = workstationService.getWorkstationPage(pageReqVO).getList();
        List<MesMdWorkstationRespVO> voList = buildWorkstationRespVOList(list);
        ExcelUtils.write(response, "工作站.xls", "数据", MesMdWorkstationRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    @SuppressWarnings("CodeBlock2Expr")
    private List<MesMdWorkstationRespVO> buildWorkstationRespVOList(List<MesMdWorkstationDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取车间、工序信息
        Map<Long, MesMdWorkshopDO> workshopMap = workshopService.getWorkshopMap(
                convertSet(list, MesMdWorkstationDO::getWorkshopId));
        Map<Long, MesProProcessDO> processMap = processService.getProcessMap(
                convertSet(list, MesMdWorkstationDO::getProcessId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdWorkstationRespVO.class, vo -> {
            MapUtils.findAndThen(workshopMap, vo.getWorkshopId(),
                    workshop -> vo.setWorkshopName(workshop.getName()));
            MapUtils.findAndThen(processMap, vo.getProcessId(),
                    process -> vo.setProcessName(process.getName()));
        });
    }

}
