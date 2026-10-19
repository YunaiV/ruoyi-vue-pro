package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.label.PmsWorkItemLabelSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemLabelDO;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemLabelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - PMS 工作项标签")
@RestController
@RequestMapping("/pms/pm/work-item-label")
@Validated
public class PmsWorkItemLabelController {

    @Resource
    private PmsWorkItemLabelService labelService;

    @PostMapping("/create")
    @Operation(summary = "创建工作项标签")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Long> createWorkItemLabel(@Valid @RequestBody PmsWorkItemLabelSaveReqVO saveReqVO) {
        return success(labelService.createWorkItemLabel(saveReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改工作项标签")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> updateWorkItemLabel(@Valid @RequestBody PmsWorkItemLabelSaveReqVO saveReqVO) {
        labelService.updateWorkItemLabel(saveReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除工作项标签")
    @Parameter(name = "id", description = "标签编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:update')")
    public CommonResult<Boolean> deleteWorkItemLabel(@RequestParam("id") Long id) {
        labelService.deleteWorkItemLabel(id);
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得工作项标签列表")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<List<PmsWorkItemLabelRespVO>> getWorkItemLabelList(
            @RequestParam(value = "name", required = false) String name) {
        List<PmsWorkItemLabelDO> labels = labelService.getWorkItemLabelList(name);
        return success(BeanUtils.toBean(labels, PmsWorkItemLabelRespVO.class));
    }

}
