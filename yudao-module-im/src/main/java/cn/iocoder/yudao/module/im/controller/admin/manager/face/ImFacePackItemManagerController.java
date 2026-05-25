package cn.iocoder.yudao.module.im.controller.admin.manager.face;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.face.vo.item.ImFacePackItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import cn.iocoder.yudao.module.im.service.face.ImFacePackItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IM 表情包项")
@RestController
@RequestMapping("/im/manager/face-pack-item")
@Validated
public class ImFacePackItemManagerController {

    @Resource
    private ImFacePackItemService facePackItemService;

    @PostMapping("/create")
    @Operation(summary = "新增表情")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:create')")
    public CommonResult<Long> createFacePackItem(@Valid @RequestBody ImFacePackItemSaveReqVO reqVO) {
        return success(facePackItemService.createFacePackItem(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改表情")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:update')")
    public CommonResult<Boolean> updateFacePackItem(@Valid @RequestBody ImFacePackItemSaveReqVO reqVO) {
        facePackItemService.updateFacePackItem(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除表情")
    @Parameter(name = "id", description = "编号", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:delete')")
    public CommonResult<Boolean> deleteFacePackItem(@RequestParam("id") Long id) {
        facePackItemService.deleteFacePackItem(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除表情")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:delete')")
    public CommonResult<Boolean> deleteFacePackItemList(
            @RequestParam("ids") @Size(max = 100, message = "批量删除最多 100 条") List<Long> ids) {
        facePackItemService.deleteFacePackItemList(ids);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得表情分页")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:query')")
    public CommonResult<PageResult<ImFacePackItemRespVO>> getFacePackItemPage(@Valid ImFacePackItemPageReqVO pageReqVO) {
        PageResult<ImFacePackItemDO> pageResult = facePackItemService.getFacePackItemPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ImFacePackItemRespVO.class));
    }

    @GetMapping("/get")
    @Operation(summary = "获得表情详情")
    @Parameter(name = "id", description = "编号", required = true, example = "2048")
    @PreAuthorize("@ss.hasPermission('im:manager:face-pack-item:query')")
    public CommonResult<ImFacePackItemRespVO> getFacePackItem(@RequestParam("id") Long id) {
        ImFacePackItemDO item = facePackItemService.getFacePackItem(id);
        return success(BeanUtils.toBean(item, ImFacePackItemRespVO.class));
    }

}
