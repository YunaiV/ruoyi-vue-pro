package cn.iocoder.yudao.module.im.controller.admin.face;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.ImFaceUserItemRespVO;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.ImFaceUserItemSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFaceUserItemDO;
import cn.iocoder.yudao.module.im.service.face.ImFaceUserItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - IM 个人表情")
@RestController
@RequestMapping("/im/face-user-item")
@Validated
public class ImFaceUserItemController {

    @Resource
    private ImFaceUserItemService faceUserItemService;

    @GetMapping("/list")
    @Operation(summary = "获得我的个人表情列表")
    public CommonResult<List<ImFaceUserItemRespVO>> getMyFaceUserItemList() {
        List<ImFaceUserItemDO> items = faceUserItemService.getMyFaceUserItemList(getLoginUserId());
        return success(BeanUtils.toBean(items, ImFaceUserItemRespVO.class));
    }

    @PostMapping("/create")
    @Operation(summary = "添加个人表情")
    public CommonResult<Long> createFaceUserItem(@Valid @RequestBody ImFaceUserItemSaveReqVO reqVO) {
        return success(faceUserItemService.createFaceUserItem(getLoginUserId(), reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除个人表情")
    @Parameter(name = "id", description = "编号", required = true, example = "4096")
    public CommonResult<Boolean> deleteFaceUserItem(@RequestParam("id") Long id) {
        faceUserItemService.deleteFaceUserItem(getLoginUserId(), id);
        return success(true);
    }

    // TODO @AI：个人应该没批量删除把？应该就一个一个删除呢。
    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除个人表情")
    @Parameter(name = "ids", description = "编号列表", required = true)
    public CommonResult<Boolean> deleteFaceUserItemList(@RequestParam("ids") List<Long> ids) {
        faceUserItemService.deleteFaceUserItemList(getLoginUserId(), ids);
        return success(true);
    }

}
