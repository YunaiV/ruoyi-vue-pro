package cn.iocoder.yudao.module.promotion.controller.admin.diy;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.diy.vo.page.*;
import cn.iocoder.yudao.module.promotion.convert.diy.DiyPageConvert;
import cn.iocoder.yudao.module.promotion.dal.dataobject.diy.DiyPageDO;
import cn.iocoder.yudao.module.promotion.service.diy.DiyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 装修页面")
@RestController
@RequestMapping("/promotion/diy-page")
@Validated
public class DiyPageController {

    @Resource
    private DiyPageService diyPageService;

    @PostMapping("/create")
    @Operation(summary = "创建装修页面")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:create')")
    public CommonResult<Long> createDiyPage(@Valid @RequestBody DiyPageCreateReqVO createReqVO) {
        return success(diyPageService.createDiyPage(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新装修页面")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:update')")
    public CommonResult<Boolean> updateDiyPage(@Valid @RequestBody DiyPageUpdateReqVO updateReqVO) {
        diyPageService.updateDiyPage(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除装修页面")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:delete')")
    public CommonResult<Boolean> deleteDiyPage(@RequestParam("id") Long id) {
        diyPageService.deleteDiyPage(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得装修页面")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:query')")
    public CommonResult<DiyPageRespVO> getDiyPage(@RequestParam("id") Long id) {
        DiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(DiyPageConvert.INSTANCE.convert(diyPage));
    }

    @GetMapping("/list")
    @Operation(summary = "获得装修页面列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:query')")
    public CommonResult<List<DiyPageRespVO>> getDiyPageList(@RequestParam("ids") Collection<Long> ids) {
        List<DiyPageDO> list = diyPageService.getDiyPageList(ids);
        return success(DiyPageConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得装修页面分页")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:query')")
    public CommonResult<PageResult<DiyPageRespVO>> getDiyPagePage(@Valid DiyPagePageReqVO pageVO) {
        PageResult<DiyPageDO> pageResult = diyPageService.getDiyPagePage(pageVO);
        return success(DiyPageConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get-property")
    @Operation(summary = "获得装修页面属性")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:query')")
    public CommonResult<DiyPagePropertyRespVO> getDiyPageProperty(@RequestParam("id") Long id) {
        DiyPageDO diyPage = diyPageService.getDiyPage(id);
        return success(DiyPageConvert.INSTANCE.convertPropertyVo(diyPage));
    }

    @PutMapping("/update-property")
    @Operation(summary = "更新装修页面属性")
    @PreAuthorize("@ss.hasPermission('promotion:diy-page:update')")
    public CommonResult<Boolean> updateDiyPageProperty(@Valid @RequestBody DiyPagePropertyUpdateRequestVO updateReqVO) {
        diyPageService.updateDiyPageProperty(updateReqVO);
        return success(true);
    }

}
