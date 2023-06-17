package cn.iocoder.yudao.module.point.controller.admin.signinconfig;

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

import cn.iocoder.yudao.module.point.controller.admin.signinconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.signinconfig.SignInConfigDO;
import cn.iocoder.yudao.module.point.convert.signinconfig.SignInConfigConvert;
import cn.iocoder.yudao.module.point.service.signinconfig.SignInConfigService;

@Tag(name = "管理后台 - 积分签到规则")
@RestController
@RequestMapping("/point/sign-in-config")
@Validated
public class SignInConfigController {

    @Resource
    private SignInConfigService signInConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建积分签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:create')")
    public CommonResult<Integer> createSignInConfig(@Valid @RequestBody SignInConfigCreateReqVO createReqVO) {
        return success(signInConfigService.createSignInConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:update')")
    public CommonResult<Boolean> updateSignInConfig(@Valid @RequestBody SignInConfigUpdateReqVO updateReqVO) {
        signInConfigService.updateSignInConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分签到规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:delete')")
    public CommonResult<Boolean> deleteSignInConfig(@RequestParam("id") Integer id) {
        signInConfigService.deleteSignInConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分签到规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<SignInConfigRespVO> getSignInConfig(@RequestParam("id") Integer id) {
        SignInConfigDO signInConfig = signInConfigService.getSignInConfig(id);
        return success(SignInConfigConvert.INSTANCE.convert(signInConfig));
    }

    @GetMapping("/list")
    @Operation(summary = "获得积分签到规则列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<List<SignInConfigRespVO>> getSignInConfigList(@RequestParam("ids") Collection<Integer> ids) {
        List<SignInConfigDO> list = signInConfigService.getSignInConfigList(ids);
        return success(SignInConfigConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分签到规则分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<PageResult<SignInConfigRespVO>> getSignInConfigPage(@Valid SignInConfigPageReqVO pageVO) {
        PageResult<SignInConfigDO> pageResult = signInConfigService.getSignInConfigPage(pageVO);
        return success(SignInConfigConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出积分签到规则 Excel")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:export')")
    @OperateLog(type = EXPORT)
    public void exportSignInConfigExcel(@Valid SignInConfigExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SignInConfigDO> list = signInConfigService.getSignInConfigList(exportReqVO);
        // 导出 Excel
        List<SignInConfigExcelVO> datas = SignInConfigConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "积分签到规则.xls", "数据", SignInConfigExcelVO.class, datas);
    }

}
