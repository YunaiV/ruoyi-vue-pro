package cn.iocoder.yudao.adminserver.modules.activiti.controller.oa;

import cn.iocoder.yudao.adminserver.modules.activiti.controller.oa.vo.*;
import cn.iocoder.yudao.adminserver.modules.activiti.convert.oa.OALeaveConvert;
import cn.iocoder.yudao.adminserver.modules.activiti.dal.dataobject.oa.OALeaveDO;
import cn.iocoder.yudao.adminserver.modules.activiti.service.oa.OALeaveService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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


@Api(tags = "请假申请")
@RestController
@RequestMapping("/oa/leave")
@Validated
public class OALeaveController {

    @Resource
    private OALeaveService leaveService;

    @PostMapping("/create")
    @ApiOperation("创建请假申请")
    @PreAuthorize("@ss.hasPermission('oa:leave:create')")
    public CommonResult<Long> createLeave(@Valid @RequestBody OALeaveCreateReqVO createReqVO) {
        // TODO @芋艿：processKey 自己去理解下。不过得把 leave 变成枚举
        createReqVO.setProcessKey("leave");
        return success(leaveService.createLeave(createReqVO));
    }

    @PostMapping("/form-key/create")
    @ApiOperation("创建外置请假申请")
    public CommonResult<Long> createFormKeyLeave(@Valid @RequestBody OALeaveCreateReqVO createReqVO) {
        // TODO @芋艿：processKey 自己去理解下。不过得把 formkey 变成枚举
        createReqVO.setProcessKey("leave-formkey");
        return success(leaveService.createLeave(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新请假申请")
    @PreAuthorize("@ss.hasPermission('oa:leave:update')")
    public CommonResult<Boolean> updateLeave(@Valid @RequestBody OALeaveUpdateReqVO updateReqVO) {
        leaveService.updateLeave(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除请假申请")
    @ApiImplicitParam(name = "id", value = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('oa:leave:delete')")
    public CommonResult<Boolean> deleteLeave(@RequestParam("id") Long id) {
        leaveService.deleteLeave(id);
        return success(true);
    }

    @GetMapping("/get")
    @ApiOperation("获得请假申请")
    @ApiImplicitParam(name = "id", value = "编号", required = true, example = "1024", dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('oa:leave:query')")
    public CommonResult<OALeaveRespVO> getLeave(@RequestParam("id") Long id) {
        OALeaveDO leave = leaveService.getLeave(id);
        return success(OALeaveConvert.INSTANCE.convert(leave));
    }

    @GetMapping("/list")
    @ApiOperation("获得请假申请列表")
    @ApiImplicitParam(name = "ids", value = "编号列表", required = true, example = "1024,2048", dataTypeClass = List.class)
    @PreAuthorize("@ss.hasPermission('oa:leave:query')")
    public CommonResult<List<OALeaveRespVO>> getLeaveList(@RequestParam("ids") Collection<Long> ids) {
        List<OALeaveDO> list = leaveService.getLeaveList(ids);
        return success(OALeaveConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @ApiOperation("获得请假申请分页")
    @PreAuthorize("@ss.hasPermission('oa:leave:query')")
    public CommonResult<PageResult<OALeaveRespVO>> getLeavePage(@Valid OALeavePageReqVO pageVO) {
        //值查询自己申请请假
        // TODO @芋艿：这里的传值，到底前端搞，还是后端搞。
        pageVO.setUserId(SecurityFrameworkUtils.getLoginUser().getUsername());
        PageResult<OALeaveDO> pageResult = leaveService.getLeavePage(pageVO);
        return success(OALeaveConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @ApiOperation("导出请假申请 Excel")
    @PreAuthorize("@ss.hasPermission('oa:leave:export')")
    @OperateLog(type = EXPORT)
    public void exportLeaveExcel(@Valid OALeaveExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<OALeaveDO> list = leaveService.getLeaveList(exportReqVO);
        // 导出 Excel
        List<OALeaveExcelVO> datas = OALeaveConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "请假申请.xls", "数据", OALeaveExcelVO.class, datas);
    }

}
