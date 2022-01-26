package cn.iocoder.yudao.adminserver.modules.system.controller.auth;

import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.session.SysUserSessionPageItemRespVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.auth.vo.session.SysUserSessionPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.convert.auth.SysUserSessionConvert;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysDeptDO;
import cn.iocoder.yudao.adminserver.modules.system.service.auth.SysUserSessionService;
import cn.iocoder.yudao.adminserver.modules.system.service.dept.SysDeptService;
import cn.iocoder.yudao.adminserver.modules.system.service.user.SysUserService;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.auth.SysUserSessionDO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.coreservice.modules.system.service.auth.SysUserSessionCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.dept.SysDeptCoreService;
import cn.iocoder.yudao.coreservice.modules.system.service.user.SysUserCoreService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Api(tags = "用户 Session")
@RestController
@RequestMapping("/system/user-session")
public class SysUserSessionController {

    @Resource
    private SysUserSessionService userSessionService;
    @Resource
    private SysUserSessionCoreService userSessionCoreService;
    @Resource
    private SysUserCoreService userCoreService;

    @Resource
    private SysDeptCoreService deptCoreService;

    @GetMapping("/page")
    @ApiOperation("获得 Session 分页列表")
    @PreAuthorize("@ss.hasPermission('system:user-session:page')")
    public CommonResult<PageResult<SysUserSessionPageItemRespVO>> getUserSessionPage(@Validated SysUserSessionPageReqVO reqVO) {
        // 获得 Session 分页
        PageResult<SysUserSessionDO> pageResult = userSessionService.getUserSessionPage(reqVO);

        // 获得拼接需要的数据
        Map<Long, SysUserDO> userMap = userCoreService.getUserMap(
                convertList(pageResult.getList(), SysUserSessionDO::getUserId));
        Map<Long, SysDeptDO> deptMap = deptCoreService.getDeptMap(
                convertList(userMap.values(), SysUserDO::getDeptId));
        // 拼接结果返回
        List<SysUserSessionPageItemRespVO> sessionList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(session -> {
            SysUserSessionPageItemRespVO respVO = SysUserSessionConvert.INSTANCE.convert(session);
            sessionList.add(respVO);
            // 设置用户账号
            MapUtils.findAndThen(userMap, session.getUserId(), user -> {
                respVO.setUsername(user.getUsername());
                // 设置用户部门
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> respVO.setDeptName(dept.getName()));
            });
        });
        return success(new PageResult<>(sessionList, pageResult.getTotal()));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除 Session")
    @ApiImplicitParam(name = "id", value = "Session 编号", required = true, dataTypeClass = String.class,
            example = "fe50b9f6-d177-44b1-8da9-72ea34f63db7")
    @PreAuthorize("@ss.hasPermission('system:user-session:delete')")
    public CommonResult<Boolean> deleteUserSession(@RequestParam("id") String id) {
        userSessionCoreService.deleteUserSession(id);
        return success(true);
    }

}
