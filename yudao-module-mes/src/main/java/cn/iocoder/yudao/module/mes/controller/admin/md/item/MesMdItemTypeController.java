package cn.iocoder.yudao.module.mes.controller.admin.md.item;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.item.vo.type.MesMdItemTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.item.MesMdItemTypeDO;
import cn.iocoder.yudao.module.mes.service.md.item.MesMdItemTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;

@Tag(name = "管理后台 - MES 物料产品分类")
@RestController
@RequestMapping("/mes/md/item-type")
@Validated
public class MesMdItemTypeController {

    @Resource
    private MesMdItemTypeService itemTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建物料产品分类")
    @PreAuthorize("@ss.hasPermission('mes:md-item-type:create')")
    public CommonResult<Long> createItemType(@Valid @RequestBody MesMdItemTypeSaveReqVO createReqVO) {
        return success(itemTypeService.createItemType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料产品分类")
    @PreAuthorize("@ss.hasPermission('mes:md-item-type:update')")
    public CommonResult<Boolean> updateItemType(@Valid @RequestBody MesMdItemTypeSaveReqVO updateReqVO) {
        itemTypeService.updateItemType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料产品分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-item-type:delete')")
    public CommonResult<Boolean> deleteItemType(@RequestParam("id") Long id) {
        itemTypeService.deleteItemType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料产品分类")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:md-item-type:query')")
    public CommonResult<MesMdItemTypeRespVO> getItemType(@RequestParam("id") Long id) {
        MesMdItemTypeDO itemType = itemTypeService.getItemType(id);
        return success(BeanUtils.toBean(itemType, MesMdItemTypeRespVO.class));
    }

    @GetMapping("/list")
    @Operation(summary = "获得物料产品分类列表")
    @PreAuthorize("@ss.hasPermission('mes:md-item-type:query')")
    public CommonResult<List<MesMdItemTypeRespVO>> getItemTypeList(@Valid MesMdItemTypeListReqVO listReqVO) {
        List<MesMdItemTypeDO> list = itemTypeService.getItemTypeList(listReqVO);
        return success(BeanUtils.toBean(list, MesMdItemTypeRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得物料产品分类精简列表", description = "只包含被开启的分类，主要用于前端的下拉选项")
    public CommonResult<List<MesMdItemTypeRespVO>> getItemTypeSimpleList() {
        List<MesMdItemTypeDO> list = itemTypeService.getItemTypeList(
                new MesMdItemTypeListReqVO().setStatus(CommonStatusEnum.ENABLE.getStatus()));
        return success(convertList(list, itemType -> new MesMdItemTypeRespVO()
                .setId(itemType.getId()).setName(itemType.getName()).setParentId(itemType.getParentId())
                .setCode(itemType.getCode()).setRemark(itemType.getRemark())
                .setItemOrProduct(itemType.getItemOrProduct())));
    }

}
