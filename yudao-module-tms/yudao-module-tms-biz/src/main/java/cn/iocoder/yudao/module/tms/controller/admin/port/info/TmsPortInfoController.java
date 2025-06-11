package cn.iocoder.yudao.module.tms.controller.admin.port.info;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.idempotent.core.annotation.Idempotent;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.utils.Validation;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoRespVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoSaveReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoSimpleRespVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.port.info.TmsPortInfoDO;
import cn.iocoder.yudao.module.tms.service.port.info.TmsPortInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.IMPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - TMS港口信息")
@RestController
@RequestMapping("/tms/port-info")
@Validated
public class TmsPortInfoController {

    @Resource
    private TmsPortInfoService portInfoService;

    @PostMapping("/create")
    @Operation(summary = "创建TMS港口信息")
    @Idempotent
    @PreAuthorize("@ss.hasPermission('tms:port-info:create')")
    public CommonResult<Long> createPortInfo(
        @Validated(Validation.OnCreate.class) @RequestBody TmsPortInfoSaveReqVO createReqVO) {
        return success(portInfoService.createPortInfo(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新TMS港口信息")
    @PreAuthorize("@ss.hasPermission('tms:port-info:update')")
    public CommonResult<Boolean> updatePortInfo(
        @Validated(Validation.OnUpdate.class) @RequestBody TmsPortInfoSaveReqVO updateReqVO) {
        portInfoService.updatePortInfo(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除TMS港口信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('tms:port-info:delete')")
    public CommonResult<Boolean> deletePortInfo(@RequestParam("id") Long id) {
        portInfoService.deletePortInfo(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得TMS港口信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('tms:port-info:query')")
    public CommonResult<TmsPortInfoRespVO> getPortInfo(@RequestParam("id") Long id) {
        TmsPortInfoDO portInfo = portInfoService.getPortInfo(id);
        TmsPortInfoRespVO tmsPortInfoRespVO = BeanUtils.toBean(portInfo, TmsPortInfoRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(Collections.singleton(tmsPortInfoRespVO))
            .mapping(TmsPortInfoRespVO::getCreator, TmsPortInfoRespVO::setCreatorName)
            .mapping(TmsPortInfoRespVO::getUpdater, TmsPortInfoRespVO::setUpdaterName).fill();
        return success(tmsPortInfoRespVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得TMS港口信息分页")
    @PreAuthorize("@ss.hasPermission('tms:port-info:query')")
    public CommonResult<PageResult<TmsPortInfoRespVO>> getPortInfoPage(@Valid TmsPortInfoPageReqVO pageReqVO) {
        PageResult<TmsPortInfoDO> pageResult = portInfoService.getPortInfoPage(pageReqVO);
        PageResult<TmsPortInfoRespVO> voPageResult = BeanUtils.toBean(pageResult, TmsPortInfoRespVO.class);
        // 人员姓名填充
        AdminUserApi.inst().prepareFill(voPageResult.getList())
            .mapping(TmsPortInfoRespVO::getCreator, TmsPortInfoRespVO::setCreatorName)
            .mapping(TmsPortInfoRespVO::getUpdater, TmsPortInfoRespVO::setUpdaterName).fill();
        return success(voPageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出TMS港口信息 Excel")
    @PreAuthorize("@ss.hasPermission('tms:port-info:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPortInfoExcel(@Valid TmsPortInfoPageReqVO pageReqVO, HttpServletResponse response)
        throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<TmsPortInfoDO> list = portInfoService.getPortInfoPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "TMS港口信息.xls", "数据", TmsPortInfoRespVO.class,
            BeanUtils.toBean(list, TmsPortInfoRespVO.class));
    }

    @GetMapping("/list-simple")
    @Operation(summary = "获得TMS港口信息精简列表")
    public CommonResult<List<TmsPortInfoSimpleRespVO>> getPortInfoSimpleList() {
        List<TmsPortInfoDO> list = portInfoService.getPortInfoList();
        return success(BeanUtils.toBean(list, TmsPortInfoSimpleRespVO.class));
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入TMS港口信息 Excel")
    @ApiAccessLog(operateType = IMPORT)
    @PreAuthorize("@ss.hasPermission('tms:port-info:import')")
    public CommonResult<Boolean> importPortInfoExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<TmsPortInfoSaveReqVO> list = ExcelUtils.read(file, TmsPortInfoSaveReqVO.class);
        // 可根据业务需要批量保存或校验
        return success(true);
    }

}