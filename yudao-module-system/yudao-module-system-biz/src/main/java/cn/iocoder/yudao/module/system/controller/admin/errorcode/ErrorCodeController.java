package cn.iocoder.yudao.module.system.controller.admin.errorcode;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.system.convert.errorcode.ErrorCodeConvert;
import cn.iocoder.yudao.module.system.controller.admin.errorcode.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.errorcode.ErrorCodeDO;
import cn.iocoder.yudao.module.system.service.errorcode.ErrorCodeService;
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

@Api(tags = "管理后台 - 错误码")
@RestController
@RequestMapping("/system/error-code")
@Validated
public class ErrorCodeController {

    @Resource
    private ErrorCodeService errorCodeService;

    @PostMapping("/create")
    @ApiOperation("创建错误码")
    @PreAuthorize("@ss.hasPermission('system:error-code:create')")
    public CommonResult<Long> createErrorCode(@Valid @RequestBody ErrorCodeCreateReqVO createReqVO) {
        return success(errorCodeService.createErrorCode(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新错误码")
    @PreAuthorize("@ss.hasPermission('system:error-code:update')")
    public CommonResult<Boolean> updateErrorCode(@Valid @RequestBody ErrorCodeUpdateReqVO updateReqVO) {
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
    public CommonResult<ErrorCodeRespVO> getErrorCode(@RequestParam("id") Long id) {
        ErrorCodeDO errorCode = errorCodeService.getErrorCode(id);
        return success(ErrorCodeConvert.INSTANCE.convert(errorCode));
    }

    @GetMapping("/page")
    @ApiOperation("获得错误码分页")
    @PreAuthorize("@ss.hasPermission('system:error-code:query')")
    public CommonResult<PageResult<ErrorCodeRespVO>> getErrorCodePage(@Valid ErrorCodePageReqVO pageVO) {
        PageResult<ErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(pageVO);
        return success(ErrorCodeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出错误码 Excel")
    @PreAuthorize("@ss.hasPermission('system:error-code:export')")
    @OperateLog(type = EXPORT)
    public void exportErrorCodeExcel(@Valid ErrorCodeExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ErrorCodeDO> list = errorCodeService.getErrorCodeList(exportReqVO);
        // 导出 Excel
        List<ErrorCodeExcelVO> datas = ErrorCodeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "错误码.xls", "数据", ErrorCodeExcelVO.class, datas);
    }

}
