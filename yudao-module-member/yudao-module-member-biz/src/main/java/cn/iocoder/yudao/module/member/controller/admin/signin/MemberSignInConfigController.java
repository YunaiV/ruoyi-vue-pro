package cn.iocoder.yudao.module.member.controller.admin.signin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.signin.vo.*;
import cn.iocoder.yudao.module.member.convert.signin.MemberSignInConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.signin.MemberSignInConfigDO;
import cn.iocoder.yudao.module.member.service.signin.MemberSignInConfigService;
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

@Tag(name = "管理后台 - 积分签到规则")
@RestController
@RequestMapping("/point/sign-in-config")
@Validated
public class MemberSignInConfigController {

    @Resource
    private MemberSignInConfigService memberSignInConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建积分签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:create')")
    public CommonResult<Integer> createSignInConfig(@Valid @RequestBody MemberSignInConfigCreateReqVO createReqVO) {
        return success(memberSignInConfigService.createSignInConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新积分签到规则")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:update')")
    public CommonResult<Boolean> updateSignInConfig(@Valid @RequestBody MemberSignInConfigUpdateReqVO updateReqVO) {
        memberSignInConfigService.updateSignInConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除积分签到规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:delete')")
    public CommonResult<Boolean> deleteSignInConfig(@RequestParam("id") Integer id) {
        memberSignInConfigService.deleteSignInConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得积分签到规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<MemberSignInConfigRespVO> getSignInConfig(@RequestParam("id") Integer id) {
        MemberSignInConfigDO signInConfig = memberSignInConfigService.getSignInConfig(id);
        return success(MemberSignInConfigConvert.INSTANCE.convert(signInConfig));
    }

    @GetMapping("/list")
    @Operation(summary = "获得积分签到规则列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<List<MemberSignInConfigRespVO>> getSignInConfigList(@RequestParam("ids") Collection<Integer> ids) {
        List<MemberSignInConfigDO> list = memberSignInConfigService.getSignInConfigList(ids);
        return success(MemberSignInConfigConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得积分签到规则分页")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:query')")
    public CommonResult<PageResult<MemberSignInConfigRespVO>> getSignInConfigPage(@Valid MemberSignInConfigPageReqVO pageVO) {
        PageResult<MemberSignInConfigDO> pageResult = memberSignInConfigService.getSignInConfigPage(pageVO);
        return success(MemberSignInConfigConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出积分签到规则 Excel")
    @PreAuthorize("@ss.hasPermission('point:sign-in-config:export')")
    @OperateLog(type = EXPORT)
    public void exportSignInConfigExcel(@Valid MemberSignInConfigExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<MemberSignInConfigDO> list = memberSignInConfigService.getSignInConfigList(exportReqVO);
        // 导出 Excel
        List<MemberSignInConfigExcelVO> datas = MemberSignInConfigConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "积分签到规则.xls", "数据", MemberSignInConfigExcelVO.class, datas);
    }

}
