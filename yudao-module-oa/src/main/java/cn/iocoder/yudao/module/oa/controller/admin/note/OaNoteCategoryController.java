package cn.iocoder.yudao.module.oa.controller.admin.note;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.category.OaNoteCategoryRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.note.vo.category.OaNoteCategorySaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteCategoryDO;
import cn.iocoder.yudao.module.oa.service.note.OaNoteCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - OA 笔记目录")
@RestController
@RequestMapping("/oa/note-category")
@Validated
public class OaNoteCategoryController {

    @Resource
    private OaNoteCategoryService noteCategoryService;

    // ==================== 目录维护 ====================

    @PostMapping("/create")
    @Operation(summary = "创建笔记目录")
    public CommonResult<Long> createNoteCategory(@Valid @RequestBody OaNoteCategorySaveReqVO createReqVO) {
        return success(noteCategoryService.createNoteCategory(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新笔记目录")
    public CommonResult<Boolean> updateNoteCategory(@Valid @RequestBody OaNoteCategorySaveReqVO updateReqVO) {
        noteCategoryService.updateNoteCategory(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除笔记目录")
    @Parameter(name = "id", description = "目录编号", required = true, example = "1024")
    public CommonResult<Boolean> deleteNoteCategory(@RequestParam("id") Long id) {
        noteCategoryService.deleteNoteCategory(id, getLoginUserId());
        return success(true);
    }

    // ==================== 目录查询 ====================

    @GetMapping("/get")
    @Operation(summary = "获得笔记目录")
    @Parameter(name = "id", description = "目录编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:note:query')")
    public CommonResult<OaNoteCategoryRespVO> getNoteCategory(@RequestParam("id") Long id) {
        OaNoteCategoryDO category = noteCategoryService.getNoteCategory(id, getLoginUserId());
        return success(BeanUtils.toBean(category, OaNoteCategoryRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得笔记目录精简列表")
    public CommonResult<List<OaNoteCategoryRespVO>> getSimpleNoteCategoryList() {
        List<OaNoteCategoryDO> categories = noteCategoryService.getNoteCategoryList(getLoginUserId());
        return success(convertList(categories, category -> new OaNoteCategoryRespVO()
                .setId(category.getId()).setName(category.getName())));
    }

    @GetMapping("/list")
    @Operation(summary = "获得笔记目录列表")
    @PreAuthorize("@ss.hasPermission('oa:note:query')")
    public CommonResult<List<OaNoteCategoryRespVO>> getNoteCategoryList() {
        List<OaNoteCategoryDO> categories = noteCategoryService.getNoteCategoryList(getLoginUserId());
        return success(BeanUtils.toBean(categories, OaNoteCategoryRespVO.class));
    }

}
