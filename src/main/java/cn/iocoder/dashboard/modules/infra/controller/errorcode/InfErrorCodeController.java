package cn.iocoder.dashboard.modules.infra.controller.errorcode;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.framework.logger.operatelog.core.annotations.OperateLog;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.*;
import cn.iocoder.dashboard.modules.infra.convert.errorcode.InfErrorCodeConvert;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;
import cn.iocoder.dashboard.modules.infra.service.errorcode.InfErrorCodeService;
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

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;
import static cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "错误码")
@RestController
@RequestMapping("/infra/error-code")
@Validated
public class InfErrorCodeController {

    @Resource
    private InfErrorCodeService errorCodeService;

    @PostMapping("/create")
    @ApiOperation("创建错误码")
    @PreAuthorize("@ss.hasPermission('infra:error-code:create')")
    public CommonResult<Long> createErrorCode(@Valid @RequestBody InfErrorCodeCreateReqVO createReqVO) {
        return success(errorCodeService.createErrorCode(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新错误码")
    @PreAuthorize("@ss.hasPermission('infra:error-code:update')")
    public CommonResult<Boolean> updateErrorCode(@Valid @RequestBody InfErrorCodeUpdateReqVO updateReqVO) {
        errorCodeService.updateErrorCode(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除错误码")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('infra:error-code:delete')")
    public CommonResult<Boolean> deleteErrorCode(@RequestParam("id") Long id) {
        errorCodeService.deleteErrorCode(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得错误码")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('infra:error-code:query')")
    public CommonResult<InfErrorCodeRespVO> getErrorCode(@RequestParam("id") Long id) {
        InfErrorCodeDO errorCode = errorCodeService.getErrorCode(id);
        return success(InfErrorCodeConvert.INSTANCE.convert(errorCode));
    }

    @GetMapping("/page")
    @ApiOperation("获得错误码分页")
    @PreAuthorize("@ss.hasPermission('infra:error-code:query')")
    public CommonResult<PageResult<InfErrorCodeRespVO>> getErrorCodePage(@Valid InfErrorCodePageReqVO pageVO) {
        PageResult<InfErrorCodeDO> pageResult = errorCodeService.getErrorCodePage(pageVO);
        return success(InfErrorCodeConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出错误码 Excel")
    @PreAuthorize("@ss.hasPermission('infra:error-code:export')")
    @OperateLog(type = EXPORT)
    public void exportErrorCodeExcel(@Valid InfErrorCodeExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<InfErrorCodeDO> list = errorCodeService.getErrorCodeList(exportReqVO);
        // 导出 Excel
        List<InfErrorCodeExcelVO> datas = InfErrorCodeConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "错误码.xls", "数据", InfErrorCodeExcelVO.class, datas);
    }

}
