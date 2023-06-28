package cn.iocoder.yudao.module.member.controller.admin.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.*;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInRecordConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInRecordDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Tag(name = "管理后台 - 用户签到积分")
@RestController
@RequestMapping("/point/sign-in-record")
@Validated
public class MemberSignInRecordController {

    @Resource
    private MemberSignInRecordService memberSignInRecordService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户签到积分")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:delete')")
    public CommonResult<Boolean> deleteSignInRecord(@RequestParam("id") Long id) {
        memberSignInRecordService.deleteSignInRecord(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户签到积分")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<MemberSignInRecordRespVO> getSignInRecord(@RequestParam("id") Long id) {
        MemberSignInRecordDO signInRecord = memberSignInRecordService.getSignInRecord(id);
        return success(MemberSignInRecordConvert.INSTANCE.convert(signInRecord));
    }

    @GetMapping("/list")
    @Operation(summary = "获得用户签到积分列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<List<MemberSignInRecordRespVO>> getSignInRecordList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberSignInRecordDO> list = memberSignInRecordService.getSignInRecordList(ids);
        return success(MemberSignInRecordConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户签到积分分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:query')")
    public CommonResult<PageResult<MemberSignInRecordRespVO>> getSignInRecordPage(@Valid MemberSignInRecordPageReqVO pageVO) {
        PageResult<MemberSignInRecordDO> pageResult = memberSignInRecordService.getSignInRecordPage(pageVO);
        return success(MemberSignInRecordConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户签到积分 Excel")
    @PreAuthorize("@ss.hasPermission('point:sign-in-record:export')")
    @OperateLog(type = EXPORT)
    public void exportSignInRecordExcel(@Valid MemberSignInRecordExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MemberSignInRecordDO> list = memberSignInRecordService.getSignInRecordList(exportReqVO);
        // 导出 Excel
        List<MemberSignInRecordExcelVO> datas = MemberSignInRecordConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "用户签到积分.xls", "数据", MemberSignInRecordExcelVO.class, datas);
    }

}
