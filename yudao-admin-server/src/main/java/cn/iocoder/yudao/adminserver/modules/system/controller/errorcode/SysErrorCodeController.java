package cn.iocoder.yudao.adminserver.modules.system.controller.errorcode;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.adminserver.modules.system.convert.errorcode.SysErrorCodeConvert;
import cn.iocoder.yudao.adminserver.modules.system.controller.errorcode.vo.*;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.errorcode.SysErrorCodeDO;
import cn.iocoder.yudao.adminserver.modules.system.service.errorcode.SysErrorCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "错误码")
@RestController
@RequestMapping("/system/error-code")
@Validated
public class SysErrorCodeController {

    @Resource
    private SysErrorCodeService errorCodeService;

    @PostMapping("/create")
    @ApiOperation("创建错误码")
    @PreAuthorize("@ss.hasPermission('system:error-code:create')")
    public CommonResult<Long> createErrorCode(@Valid @RequestBody SysErrorCodeCreateReqVO createReqVO) {
        return success(errorCodeService.createErrorCode(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新错误码")
    @PreAuthorize("@ss.hasPermission('system:error-code:update')")
    public CommonResult<Boolean> updateErrorCode(@Valid @RequestBody SysErrorCodeUpdateReqVO updateReqVO) {
        errorCodeService.updateErrorCode(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除错误码")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:error-code:delete')")
    public CommonResult<Boolean> deleteErrorCode(@RequestParam("id") Long id) {
        errorCodeService.deleteErrorCode(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得错误码")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('system:error-code:query')")
    public CommonResult<SysErrorCodeRespVO> getErrorCode(@RequestParam("id") Long id) {
        SysErrorCodeDO errorCode = errorCodeService.getErrorCode(id);
        return success(SysErrorCodeConvert.INSTANCE.convert(errorCode));
    }

    @GetMapping("/page")
    @ApiOperation("获得错误码分页")
    @PreAuthorize("@ss.hasPermission('system:error-code:query')")
    public CommonResult<PageResult<SysErrorCodeRespVO>> getErrorCodePage(@Valid SysErrorCodePageReqVO pageVO) {
        PageResult<SysErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(pageVO);
        return success(SysErrorCodeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出错误码 Excel")
    @PreAuthorize("@ss.hasPermission('system:error-code:export')")
    @OperateLog(type = EXPORT)
    public void exportErrorCodeExcel(@Valid SysErrorCodeExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SysErrorCodeDO> list = errorCodeService.getErrorCodeList(exportReqVO);
        // 导出 Excel
        List<SysErrorCodeExcelVO> datas = SysErrorCodeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "错误码.xls", "数据", SysErrorCodeExcelVO.class, datas);
    }

}
