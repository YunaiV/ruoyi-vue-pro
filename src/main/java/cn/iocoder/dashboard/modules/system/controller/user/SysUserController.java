package cn.iocoder.dashboard.modules.system.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.excel.core.util.ExcelUtils;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.*;
import cn.iocoder.dashboard.modules.system.convert.user.SysUserConvert;
import cn.iocoder.dashboard.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.enums.common.SysSexEnum;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import cn.iocoder.dashboard.util.collection.MapUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "用户")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Resource
    private SysUserService userService;
    @Resource
    private SysDeptService deptService;

    @GetMapping("/page")
    @ApiOperation("获得用户分页列表")
    @PreAuthorize("@ss.hasPermission('system:user:list')")
    public CommonResult<PageResult<SysUserPageItemRespVO>> pageUsers(@Validated SysUserPageReqVO reqVO) {
        // 获得用户分页列表
        PageResult<SysUserDO> pageResult = userService.pageUsers(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal())); // 返回空
        }

        // 获得拼接需要的数据
        Collection<Long> deptIds = CollectionUtils.convertList(pageResult.getList(), SysUserDO::getDeptId);
        Map<Long, SysDeptDO> deptMap = deptService.getDeptMap(deptIds);
        // 拼接结果返回
        List<SysUserPageItemRespVO> userList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(user -> {
            SysUserPageItemRespVO respVO = SysUserConvert.INSTANCE.convert(user);
            respVO.setDept(SysUserConvert.INSTANCE.convert(deptMap.get(user.getDeptId())));
            userList.add(respVO);
        });
        return success(new PageResult<>(userList, pageResult.getTotal()));
    }

    /**
     * 根据用户编号获取详细信息
     */
    @GetMapping("/get")
    @ApiOperation("获得用户详情")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
//    @PreAuthorize("@ss.hasPermi('system:user:query')")
    public CommonResult<SysUserRespVO> getInfo(@RequestParam("id") Long id) {
        return success(SysUserConvert.INSTANCE.convert(userService.getUser(id)));
    }

    @ApiOperation("新增用户")
    @PostMapping("/create")
//    @PreAuthorize("@ss.hasPermi('system:user:add')")
//    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public CommonResult<Long> createUser(@Validated @RequestBody SysUserCreateReqVO reqVO) {
        Long id = userService.createUser(reqVO);
        return success(id);
    }

    @ApiOperation("修改用户")
    @PostMapping("update")
//    @PreAuthorize("@ss.hasPermi('system:user:edit')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateUser(@Validated @RequestBody SysUserUpdateReqVO reqVO) {
        userService.updateUser(reqVO);
        return success(true);
    }

    @ApiOperation("删除用户")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PostMapping("/delete")
//    @PreAuthorize("@ss.hasPermi('system:user:remove')")
//    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public CommonResult<Boolean> deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return success(true);
    }

    @ApiOperation("重置用户密码")
    @PostMapping("/update-password")
//    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateUserPassword(@Validated @RequestBody SysUserUpdatePasswordReqVO reqVO) {
        userService.updateUserPassword(reqVO.getId(), reqVO.getPassword());
        return success(true);
    }

    @ApiOperation("修改用户状态")
    @PostMapping("/update-status")
//    @PreAuthorize("@ss.hasPermi('system:user:edit')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public CommonResult<Boolean> updateUserStatus(@Validated @RequestBody SysUserUpdateStatusReqVO reqVO) {
        userService.updateUserStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @ApiOperation("导出用户")
    @GetMapping("/export")
//    @PreAuthorize("@ss.hasPermi('system:user:export')") , @Validated SysUserExportReqVO reqVO
//    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    public void exportUsers(@Validated SysUserExportReqVO reqVO,
                            HttpServletResponse response) throws IOException {
        // 获得用户列表
        List<SysUserDO> users = userService.listUsers(reqVO);

        // 获得拼接需要的数据
        Collection<Long> deptIds = CollectionUtils.convertList(users, SysUserDO::getDeptId);
        Map<Long, SysDeptDO> deptMap = deptService.getDeptMap(deptIds);
        // 拼接数据
        List<SysUserExcelVO> excelUsers = new ArrayList<>(users.size());
        users.forEach(user -> {
            SysUserExcelVO excelVO = SysUserConvert.INSTANCE.convert02(user);
            MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> {
                excelVO.setDeptName(dept.getName());
                excelVO.setDeptLeader(dept.getLeader());
            });
            excelUsers.add(excelVO);
        });

        // 输出
        ExcelUtils.write(response, "用户数据.xls", "用户列表", SysUserExcelVO.class, excelUsers);
    }

    @ApiOperation("获得导入用户模板")
    @GetMapping("/get-import-template")
    public void importTemplate(HttpServletResponse response) throws IOException {
        // 手动创建导出 demo
        List<SysUserImportExcelVO> list = Arrays.asList(
                SysUserImportExcelVO.builder().username("yudao").deptId(1L).email("yudao@iocoder.cn").mobile("15601691300")
                        .nickname("芋道").status(CommonStatusEnum.ENABLE.getStatus()).sex(SysSexEnum.MALE.getSEX()).build(),
                SysUserImportExcelVO.builder().username("yuanma").deptId(2L).email("yuanma@iocoder.cn").mobile("15601701300")
                        .nickname("源码").status(CommonStatusEnum.DISABLE.getStatus()).sex(SysSexEnum.FEMALE.getSEX()).build()
        );

        // 输出
        ExcelUtils.write(response, "用户导入模板.xls", "用户列表",
                SysUserImportExcelVO.class, list);

    }

    @ApiOperation("导入用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "Excel 文件", required = true, dataTypeClass = MultipartFile.class),
            @ApiImplicitParam(name = "updateSupport", value = "是否支持更新，默认为 false", example = "true", dataTypeClass = Boolean.class)
    })
    @PostMapping("/import")
//    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
//    @PreAuthorize("@ss.hasPermi('system:user:import')")
    public CommonResult<SysUserImportRespVO> importExcel(@RequestParam("file") MultipartFile file,
                                                         @RequestParam(value = "updateSupport", required = false, defaultValue = "false") Boolean updateSupport) throws Exception {
        List<SysUserImportExcelVO> list = ExcelUtils.raed(file, SysUserImportExcelVO.class);
        return success(userService.importUsers(list, updateSupport));
    }

}
