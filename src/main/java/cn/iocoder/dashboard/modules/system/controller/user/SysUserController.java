package cn.iocoder.dashboard.modules.system.controller.user;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageItemRespVO;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageReqVO;
import cn.iocoder.dashboard.modules.system.convert.user.SysUserConvert;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import cn.iocoder.dashboard.modules.system.service.dept.SysDeptService;
import cn.iocoder.dashboard.modules.system.service.user.SysUserService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//
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
//     * 根据用户编号获取详细信息
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:query')")
//    @GetMapping(value = { "/", "/{userId}" })
//    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
//    {
//        AjaxResult ajax = AjaxResult.success();
//        List<SysRole> roles = roleService.selectRoleAll();
//        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
//        ajax.put("posts", postService.selectPostAll());
//        if (StringUtils.isNotNull(userId))
//        {
//            ajax.put(AjaxResult.DATA_TAG, userService.selectUserById(userId));
//            ajax.put("postIds", postService.selectPostListByUserId(userId));
//            ajax.put("roleIds", roleService.selectRoleListByUserId(userId));
//        }
//        return ajax;
//    }
//
//    /**
//     * 新增用户
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:add')")
//    @Log(title = "用户管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody SysUser user)
//    {
//        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName())))
//        {
//            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getPhonenumber())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user)))
//        {
//            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user)))
//        {
//            return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
//        }
//        user.setCreateBy(SecurityUtils.getUsername());
//        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
//        return toAjax(userService.insertUser(user));
//    }
//
//    /**
//     * 修改用户
//     */
//    @PreAuthorize("@ss.hasPermi('system:user:edit')")
//    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult edit(@Validated @RequestBody SysUser user)
//    {
//        userService.checkUserAllowed(user);
//        if (StringUtils.isNotEmpty(user.getPhonenumber())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user)))
//        {
//            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail())
//                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user)))
//        {
//            return AjaxResult.error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
//        }
//        user.setUpdateBy(SecurityUtils.getUsername());
//        return toAjax(userService.updateUser(user));
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
