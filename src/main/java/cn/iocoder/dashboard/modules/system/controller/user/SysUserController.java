package cn.iocoder.dashboard.modules.system.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.*;
import cn.iocoder.dashboard.modules.system.convert.user.SysUserConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api(tags = "用户 API")
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Resource
    private SysUserService userService;
    @Resource
    private SysDeptService deptService;

    @ApiOperation("获得用户分页列表")
    @GetMapping("/page")
//    @PreAuthorize("@ss.hasPermi('system:user:list')")
    public CommonResult<PageResult<SysUserPageItemRespVO>> pageUsers(SysUserPageReqVO reqVO) {
        // 获得用户分页列表
        PageResult<SysUserDO> pageResult = userService.pageUsers(reqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal())); // 返回空
        }

        // 获得拼接需要的数据
        Map<Long, SysDeptDO> deptMap;
        Collection<Long> deptIds = CollectionUtils.convertList(pageResult.getList(), SysUserDO::getDeptId);
        if (CollUtil.isNotEmpty(deptIds)) {
            deptMap = CollectionUtils.convertMap(deptService.listDepts(deptIds), SysDeptDO::getId);
        } else {
            deptMap = Collections.emptyMap();
        }
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
    @ApiOperation("获得用户详情")
    @ApiImplicitParam(name = "id", value = "编号", readOnly = true, example = "1024")
    @GetMapping("/get")
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

//    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
//    @PreAuthorize("@ss.hasPermi('system:user:export')")
//    @GetMapping("/export")
//    public AjaxResult export(SysUser user)
//    {
//        List<SysUser> list = userService.selectUserList(user);
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        return util.exportExcel(list, "用户数据");
//    }
//
//    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
//    @PreAuthorize("@ss.hasPermi('system:user:import')")
//    @PostMapping("/importData")
//    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
//    {
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        List<SysUser> userList = util.importExcel(file.getInputStream());
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        String operName = loginUser.getUsername();
//        String message = userService.importUser(userList, updateSupport, operName);
//        return AjaxResult.success(message);
//    }
//
//    @GetMapping("/importTemplate")
//    public AjaxResult importTemplate()
//    {
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        return util.importTemplateExcel("用户数据");
//    }
//


//    /**
//     * 删除用户
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:remove')")
//    @Log(title = "用户管理", businessType = BusinessType.DELETE)
//    @DeleteMapping("/{userIds}")
//    public AjaxResult remove(@PathVariable Long[] userIds)
//    {
//        return toAjax(userService.deleteUserByIds(userIds));
//    }
//
//    /**
//     * 重置密码
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping("/resetPwd")
//    public AjaxResult resetPwd(@RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.resetPwd(user));
//    }
//
//    /**
//     * 状态修改
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:edit')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping("/changeStatus")
//    public AjaxResult changeStatus(@RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.updateUserStatus(user));
//    }

}
