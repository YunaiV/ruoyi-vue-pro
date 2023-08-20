package cn.iocoder.yudao.module.member.controller.admin.tag;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.*;
import cn.iocoder.yudao.module.member.convert.tag.MemberTagConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.module.member.service.tag.MemberTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "管理后台 - 会员标签")
@RestController
@RequestMapping("/member/tag")
@Validated
public class MemberTagController {

    @Resource
    private MemberTagService tagService;

    @PostMapping("/create")
    @Operation(summary = "创建会员标签")
    @PreAuthorize("@ss.hasPermission('member:tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody MemberTagCreateReqVO createReqVO) {
        return success(tagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新会员标签")
    @PreAuthorize("@ss.hasPermission('member:tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody MemberTagUpdateReqVO updateReqVO) {
        tagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除会员标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('member:tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        tagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得会员标签")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<MemberTagRespVO> getMemberTag(@RequestParam("id") Long id) {
        MemberTagDO tag = tagService.getTag(id);
        return success(MemberTagConvert.INSTANCE.convert(tag));
    }

    @GetMapping("/list")
    @Operation(summary = "获得会员标签列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<List<MemberTagRespVO>> getMemberTagList(@RequestParam("ids") Collection<Long> ids) {
        List<MemberTagDO> list = tagService.getTagList(ids);
        return success(MemberTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得会员标签分页")
    @PreAuthorize("@ss.hasPermission('member:tag:query')")
    public CommonResult<PageResult<MemberTagRespVO>> getTagPage(@Valid MemberTagPageReqVO pageVO) {
        PageResult<MemberTagDO> pageResult = tagService.getTagPage(pageVO);
        return success(MemberTagConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出会员标签 Excel")
    @PreAuthorize("@ss.hasPermission('member:tag:export')")
    @OperateLog(type = EXPORT)
    public void exportTagExcel(@Valid MemberTagExportReqVO exportReqVO,
                               HttpServletResponse response) throws IOException {
        List<MemberTagDO> list = tagService.getTagList(exportReqVO);
        // 导出 Excel
        List<MemberTagExcelVO> datas = MemberTagConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "会员标签.xls", "数据", MemberTagExcelVO.class, datas);
    }

}
