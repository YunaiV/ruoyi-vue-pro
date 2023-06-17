package cn.iocoder.yudao.module.point.controller.admin.signinrecord;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.point.controller.admin.signinrecord.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinrecord.SignInRecordDO;
import cn.iocoder.yudao.module.point.convert.signinrecord.SignInRecordConvert;
import cn.iocoder.yudao.module.point.service.signinrecord.SignInRecordService;

@Tag(name = "管理后台 - 用户签到积分")
@RestController
@RequestMapping("/point/sign-in-record")
@Validated
public class SignInRecordController {

    @Resource
    private SignInRecordService signInRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建用户签到积分")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:create')")
    public CommonResult<Long> createSignInRecord(@Valid @RequestBody SignInRecordCreateReqVO createReqVO) {
        return success(signInRecordService.createSignInRecord(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户签到积分")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:update')")
    public CommonResult<Boolean> updateSignInRecord(@Valid @RequestBody SignInRecordUpdateReqVO updateReqVO) {
        signInRecordService.updateSignInRecord(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户签到积分")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:delete')")
    public CommonResult<Boolean> deleteSignInRecord(@RequestParam("id") Long id) {
        signInRecordService.deleteSignInRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户签到积分")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<SignInRecordRespVO> getSignInRecord(@RequestParam("id") Long id) {
        SignInRecordDO signInRecord = signInRecordService.getSignInRecord(id);
        return success(SignInRecordConvert.INSTANCE.convert(signInRecord));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户签到积分列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<List<SignInRecordRespVO>> getSignInRecordList(@RequestParam("ids") Collection<Long> ids) {
        List<SignInRecordDO> list = signInRecordService.getSignInRecordList(ids);
        return success(SignInRecordConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户签到积分分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<PageResult<SignInRecordRespVO>> getSignInRecordPage(@Valid SignInRecordPageReqVO pageVO) {
        PageResult<SignInRecordDO> pageResult = signInRecordService.getSignInRecordPage(pageVO);
        return success(SignInRecordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户签到积分 Excel")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:export')")
    @OperateLog(type = EXPORT)
    public void exportSignInRecordExcel(@Valid SignInRecordExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SignInRecordDO> list = signInRecordService.getSignInRecordList(exportReqVO);
        // 导出 Excel
        List<SignInRecordExcelVO> datas = SignInRecordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用户签到积分.xls", "数据", SignInRecordExcelVO.class, datas);
    }

}
