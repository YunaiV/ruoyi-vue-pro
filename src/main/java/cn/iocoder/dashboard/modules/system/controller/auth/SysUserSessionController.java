package cn.iocoder.dashboard.modules.system.controller.auth;

import cn.iocoder.dashboard.common.pojo.CommonResult;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageItemRespVO;
import cn.iocoder.dashboard.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.auth.SysUserSessionDO;
import cn.iocoder.dashboard.modules.system.service.auth.SysUserSessionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static cn.iocoder.dashboard.common.pojo.CommonResult.success;

@Api("用户 Session API")
@RestController
@RequestMapping("/user-session")
public class SysUserSessionController {

    @Resource
    private SysUserSessionService userSessionService;

    @ApiOperation("获得 Session 分页列表")
    @PreAuthorize("@ss.hasPermission('system:user-session:page')")
    @GetMapping("/page")
    public CommonResult<PageResult<SysUserSessionPageItemRespVO>> getUserSessionPage(@Validated SysUserSessionPageReqVO reqVO) {
        // 获得 Session 分页
        PageResult<SysUserSessionDO> sessionPage = userSessionService.getUserSessionPage(reqVO);

        //
        return null;
    }

    @ApiOperation("删除 Session")
    @PreAuthorize("@ss.hasPermission('system:user-session:delete')")
    @DeleteMapping("/delete")
    @ApiImplicitParam(name = "id", value = "Session 编号", required = true, dataTypeClass = String.class,
            example = "fe50b9f6-d177-44b1-8da9-72ea34f63db7")
    public CommonResult<Boolean> delete(@RequestParam("id") String id) {
        userSessionService.deleteUserSession(id);
        return success(true);
    }

}
