package cn.iocoder.yudao.module.mes.controller.admin.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.workshop.MesMdWorkshopSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkshopDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkshopService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - MES 车间")
@RestController
@RequestMapping("/mes/md-workshop")
@Validated
public class MesMdWorkshopController {

    @Resource
    private MesMdWorkshopService workshopService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建车间")
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:create')")
    public CommonResult<Long> createWorkshop(@Valid @RequestBody MesMdWorkshopSaveReqVO createReqVO) {
        return success(workshopService.createWorkshop(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新车间")
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:update')")
    public CommonResult<Boolean> updateWorkshop(@Valid @RequestBody MesMdWorkshopSaveReqVO updateReqVO) {
        workshopService.updateWorkshop(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除车间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:delete')")
    public CommonResult<Boolean> deleteWorkshop(@RequestParam("id") Long id) {
        workshopService.deleteWorkshop(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得车间")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:query')")
    public CommonResult<MesMdWorkshopRespVO> getWorkshop(@RequestParam("id") Long id) {
        MesMdWorkshopDO workshop = workshopService.getWorkshop(id);
        return success(BeanUtils.toBean(workshop, MesMdWorkshopRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得车间分页")
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:query')")
    public CommonResult<PageResult<MesMdWorkshopRespVO>> getWorkshopPage(@Valid MesMdWorkshopPageReqVO pageReqVO) {
        PageResult<MesMdWorkshopDO> pageResult = workshopService.getWorkshopPage(pageReqVO);
        return success(new PageResult<>(buildWorkshopRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得车间精简列表", description = "只包含被开启的车间，主要用于前端的下拉选项")
    public CommonResult<List<MesMdWorkshopRespVO>> getWorkshopSimpleList() {
        List<MesMdWorkshopDO> list = workshopService.getWorkshopListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, ws -> new MesMdWorkshopRespVO()
                .setId(ws.getId()).setCode(ws.getCode()).setName(ws.getName()).setArea(ws.getArea())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出车间 Excel")
    @PreAuthorize("@ss.hasPermission('mes:md-workshop:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportWorkshopExcel(@Valid MesMdWorkshopPageReqVO pageReqVO,
                                     HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesMdWorkshopDO> list = workshopService.getWorkshopPage(pageReqVO).getList();
        List<MesMdWorkshopRespVO> voList = buildWorkshopRespVOList(list);
        ExcelUtils.write(response, "车间.xls", "数据", MesMdWorkshopRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesMdWorkshopRespVO> buildWorkshopRespVOList(List<MesMdWorkshopDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取负责人信息
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesMdWorkshopDO::getChargeUserId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdWorkshopRespVO.class, vo -> {
            MapUtils.findAndThen(userMap, vo.getChargeUserId(),
                    user -> vo.setChargeUserName(user.getNickname()));
        });
    }

}
