package cn.iocoder.yudao.module.system.controller.admin.group;

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

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.*;

import cn.iocoder.yudao.module.system.controller.admin.group.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.group.GroupDO;
import cn.iocoder.yudao.module.system.service.group.GroupService;

@Tag(name = "管理后台 - 单表生成用户组")
@RestController
@RequestMapping("/system/group")
@Validated
public class GroupController {

    @Resource
    private GroupService groupService;

    @PostMapping("/create")
    @Operation(summary = "创建单表生成用户组")
    @PreAuthorize("@ss.hasPermission('system:group:create')")
    public CommonResult<Long> createGroup(@Valid @RequestBody GroupSaveReqVO createReqVO) {
        return success(groupService.createGroup(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新单表生成用户组")
    @PreAuthorize("@ss.hasPermission('system:group:update')")
    public CommonResult<Boolean> updateGroup(@Valid @RequestBody GroupSaveReqVO updateReqVO) {
        groupService.updateGroup(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除单表生成用户组")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('system:group:delete')")
    public CommonResult<Boolean> deleteGroup(@RequestParam("id") Long id) {
        groupService.deleteGroup(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除单表生成用户组")
                @PreAuthorize("@ss.hasPermission('system:group:delete')")
    public CommonResult<Boolean> deleteGroupList(@RequestParam("ids") List<Long> ids) {
        groupService.deleteGroupListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得单表生成用户组")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:group:query')")
    public CommonResult<GroupRespVO> getGroup(@RequestParam("id") Long id) {
        GroupDO group = groupService.getGroup(id);
        return success(BeanUtils.toBean(group, GroupRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得单表生成用户组分页")
    @PreAuthorize("@ss.hasPermission('system:group:query')")
    public CommonResult<PageResult<GroupRespVO>> getGroupPage(@Valid GroupPageReqVO pageReqVO) {
        PageResult<GroupDO> pageResult = groupService.getGroupPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, GroupRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出单表生成用户组 Excel")
    @PreAuthorize("@ss.hasPermission('system:group:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportGroupExcel(@Valid GroupPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<GroupDO> list = groupService.getGroupPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "单表生成用户组.xls", "数据", GroupRespVO.class,
                        BeanUtils.toBean(list, GroupRespVO.class));
    }

}