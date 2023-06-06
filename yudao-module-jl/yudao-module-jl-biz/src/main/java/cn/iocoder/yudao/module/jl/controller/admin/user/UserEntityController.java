package cn.iocoder.yudao.module.jl.controller.admin.user;

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
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.jl.controller.admin.user.vo.*;
import cn.iocoder.yudao.module.jl.entity.user.User;
import cn.iocoder.yudao.module.jl.mapper.user.UserMapper;
import cn.iocoder.yudao.module.jl.service.user.UserService;

@Tag(name = "管理后台 - 用户信息")
@RestController
@RequestMapping("/jl/user")
@Validated
public class UserEntityController {

    @Resource
    private UserService userService;

    @Resource
    private UserMapper userMapper;

    @PostMapping("/create")
    @Operation(summary = "创建用户信息")
    @PreAuthorize("@ss.hasPermission('jl:user:create')")
    public CommonResult<Long> createUser(@Valid @RequestBody UserCreateReqVO createReqVO) {
        return success(userService.createUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    @PreAuthorize("@ss.hasPermission('jl:user:update')")
    public CommonResult<Boolean> updateUser(@Valid @RequestBody UserUpdateReqVO updateReqVO) {
        userService.updateUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "通过 ID 删除用户信息")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:user:delete')")
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "通过 ID 获得用户信息")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:user:query')")
    public CommonResult<UserRespVO> getUser(@RequestParam("id") Long id) {
            Optional<User> user = userService.getUser(id);
        return success(user.map(userMapper::toDto).orElseThrow(() -> exception(USER_NOT_EXISTS)));
    }

    @GetMapping("/page")
    @Operation(summary = "(分页)获得用户信息列表")
    @PreAuthorize("@ss.hasPermission('jl:user:query')")
    public CommonResult<PageResult<UserRespVO>> getUserPage(@Valid UserPageReqVO pageVO, @Valid UserPageOrder orderV0) {
        PageResult<User> pageResult = userService.getUserPage(pageVO, orderV0);
        return success(userMapper.toPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户信息 Excel")
    @PreAuthorize("@ss.hasPermission('jl:user:export')")
    @OperateLog(type = EXPORT)
    public void exportUserExcel(@Valid UserExportReqVO exportReqVO, HttpServletResponse response) throws IOException {
        List<User> list = userService.getUserList(exportReqVO);
        // 导出 Excel
        List<UserExcelVO> excelData = userMapper.toExcelList(list);
        ExcelUtils.write(response, "用户信息.xls", "数据", UserExcelVO.class, excelData);
    }

}
