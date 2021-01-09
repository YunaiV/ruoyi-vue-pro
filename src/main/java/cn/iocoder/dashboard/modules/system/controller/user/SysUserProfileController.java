package cn.iocoder.dashboard.modules.system.controller.user;

public class SysUserProfileController {

//    /**
//     * 个人信息
//     */
//    @GetMapping
//    public AjaxResult profile()
//    {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        SysUser user = loginUser.getUser();
//        AjaxResult ajax = AjaxResult.success(user);
//        ajax.put("roleGroup", userService.selectUserRoleGroup(loginUser.getUsername()));
//        ajax.put("postGroup", userService.selectUserPostGroup(loginUser.getUsername()));
//        return ajax;
//    }
//
//    /**
//     * 修改用户
//     */
//    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
//    @PutMapping
//    public AjaxResult updateProfile(@RequestBody SysUser user)
//    {
//        if (userService.updateUserProfile(user) > 0)
//        {
//            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//            // 更新缓存用户信息
//            loginUser.getUser().setNickName(user.getNickName());
//            loginUser.getUser().setPhonenumber(user.getPhonenumber());
//            loginUser.getUser().setEmail(user.getEmail());
//            loginUser.getUser().setSex(user.getSex());
//            tokenService.setLoginUser(loginUser);
//            return AjaxResult.success();
//        }
//        return AjaxResult.error("修改个人信息异常，请联系管理员");
//    }
//
//    /**
//     * 重置密码
//     */
//    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
//    @PutMapping("/updatePwd")
//    public AjaxResult updatePwd(String oldPassword, String newPassword)
//    {
//        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//        String userName = loginUser.getUsername();
//        String password = loginUser.getPassword();
//        if (!SecurityUtils.matchesPassword(oldPassword, password))
//        {
//            return AjaxResult.error("修改密码失败，旧密码错误");
//        }
//        if (SecurityUtils.matchesPassword(newPassword, password))
//        {
//            return AjaxResult.error("新密码不能与旧密码相同");
//        }
//        if (userService.resetUserPwd(userName, SecurityUtils.encryptPassword(newPassword)) > 0)
//        {
//            // 更新缓存用户密码
//            loginUser.getUser().setPassword(SecurityUtils.encryptPassword(newPassword));
//            tokenService.setLoginUser(loginUser);
//            return AjaxResult.success();
//        }
//        return AjaxResult.error("修改密码异常，请联系管理员");
//    }
//
//    /**
//     * 头像上传
//     */
//    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
//    @PostMapping("/avatar")
//    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws IOException
//    {
//        if (!file.isEmpty())
//        {
//            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
//            String avatar = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file);
//            if (userService.updateUserAvatar(loginUser.getUsername(), avatar))
//            {
//                AjaxResult ajax = AjaxResult.success();
//                ajax.put("imgUrl", avatar);
//                // 更新缓存用户头像
//                loginUser.getUser().setAvatar(avatar);
//                tokenService.setLoginUser(loginUser);
//                return ajax;
//            }
//        }
//        return AjaxResult.error("上传图片异常，请联系管理员");
//    }

}
