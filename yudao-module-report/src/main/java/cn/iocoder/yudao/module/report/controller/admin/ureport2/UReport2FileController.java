package cn.iocoder.yudao.module.report.controller.admin.ureport2;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FilePageReqVO;
import cn.iocoder.yudao.module.report.controller.admin.ureport2.vo.UReport2FileRespVO;
import cn.iocoder.yudao.module.report.dal.dataobject.ureport2.UReport2FileDO;
import cn.iocoder.yudao.module.report.service.ureport2.UReport2FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - UReport2 报表模板
 *
 * 模板的新建、设计、预览由 UReport2 自带的页面完成，这里只提供列表与删除；权限与 UReport2 设计器一致
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - UReport2 报表模板")
@RestController
@RequestMapping("/report/ureport2/file")
@Validated
public class UReport2FileController {

    @Resource
    private UReport2FileService ureport2FileService;

    @DeleteMapping("/delete")
    @Operation(summary = "删除 UReport2 报表模板")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('report:ureport:design')")
    public CommonResult<Boolean> deleteFile(@RequestParam("id") Long id) {
        ureport2FileService.deleteFile(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得 UReport2 报表模板分页")
    @PreAuthorize("@ss.hasAnyPermissions('report:ureport:query', 'report:ureport:design')")
    public CommonResult<PageResult<UReport2FileRespVO>> getFilePage(@Valid UReport2FilePageReqVO pageReqVO) {
        PageResult<UReport2FileDO> pageResult = ureport2FileService.getFilePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, UReport2FileRespVO.class));
    }

}
