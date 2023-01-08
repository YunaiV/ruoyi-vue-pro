package cn.iocoder.yudao.module.mp.controller.admin.tag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagRespVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagUpdateReqVO;
import cn.iocoder.yudao.module.mp.convert.tag.MpTagConvert;
import cn.iocoder.yudao.module.mp.dal.dataobject.tag.MpTagDO;
import cn.iocoder.yudao.module.mp.service.tag.MpTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 公众号标签")
@RestController
@RequestMapping("/mp/tag")
@Validated
public class MpTagController {

    @Resource
    private MpTagService mpTagService;

    @PostMapping("/create")
    @ApiOperation("创建公众号标签")
    @PreAuthorize("@ss.hasPermission('mp:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody MpTagCreateReqVO createReqVO) {
        return success(mpTagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @ApiOperation("更新公众号标签")
    @PreAuthorize("@ss.hasPermission('mp:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody MpTagUpdateReqVO updateReqVO) {
        mpTagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除公众号标签")
    @ApiImplicitParam(name = "id", value = "编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        mpTagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/page")
    @ApiOperation("获取公众号标签分页")
    @PreAuthorize("@ss.hasPermission('mp:tag:query')")
    public CommonResult<PageResult<MpTagRespVO>> getTagPage(MpTagPageReqVO pageReqVO) {
        PageResult<MpTagDO> pageResult = mpTagService.getTagPage(pageReqVO);
        return success(MpTagConvert.INSTANCE.convertPage(pageResult));
    }

    @PostMapping("/sync")
    @ApiOperation("同步公众标签")
    @ApiImplicitParam(name = "id", value = "公众号账号的编号", required = true, dataTypeClass = Long.class)
    @PreAuthorize("@ss.hasPermission('mp:tag:sync')")
    public CommonResult<Boolean> syncTag(@RequestParam("accountId") Long accountId) {
        mpTagService.syncTag(accountId);
        return success(true);
    }

}
