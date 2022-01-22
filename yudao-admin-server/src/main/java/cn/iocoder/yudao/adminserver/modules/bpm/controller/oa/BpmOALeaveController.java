package cn.iocoder.yudao.adminserver.modules.bpm.controller.oa;

import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeavePageReqVO;
import cn.iocoder.yudao.adminserver.modules.bpm.controller.oa.vo.OALeaveRespVO;
import cn.iocoder.yudao.adminserver.modules.bpm.convert.oa.OALeaveConvert;
import cn.iocoder.yudao.adminserver.modules.bpm.dal.dataobject.leave.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.bpm.service.oa.BpmOALeaveService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * OA 请假申请 Controller，用于演示自己存储数据，接入工作流的例子
 *
 * @author jason
 * @author 芋道源码
 */
@Api(tags = "OA 请假申请")
@RestController
@RequestMapping("/bpm/oa/leave")
@Validated
public class BpmOALeaveController {

    @Resource
    private BpmOALeaveService leaveService;

    // TODO @芋艿：权限

    @PostMapping("/create")
    @ApiOperation("创建请求申请")
    public CommonResult<Long> createLeave(@Valid @RequestBody OALeaveCreateReqVO createReqVO) {
        return success(leaveService.createLeave(getLoginUserId(), createReqVO));
    }

    @DeleteMapping("/cancel")
    @ApiOperation("取消请假申请")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    public CommonResult<Boolean> cancelLeave(@RequestParam("id") Long id) {
        leaveService.cancelLeave(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得请假申请")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    public CommonResult<OALeaveRespVO> getLeave(@RequestParam("id") Long id) {
        OALeaveDO leave = leaveService.getLeave(id);
        return success(OALeaveConvert.INSTANCE.convert(leave));
    }

    @GetMapping("/page")
    @ApiOperation("获得请假申请分页")
    public CommonResult<PageResult<OALeaveRespVO>> getLeavePage(@Valid OALeavePageReqVO pageVO) {
        PageResult<OALeaveDO> pageResult = leaveService.getLeavePage(getLoginUserId(), pageVO);
        return success(OALeaveConvert.INSTANCE.convertPage(pageResult));
    }

}
