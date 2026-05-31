package cn.iocoder.yudao.module.im.controller.admin.face;

import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.face.vo.pack.ImFacePackUserRespVO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackDO;
import cn.iocoder.yudao.module.im.dal.dataobject.face.ImFacePackItemDO;
import cn.iocoder.yudao.module.im.service.face.ImFacePackItemService;
import cn.iocoder.yudao.module.im.service.face.ImFacePackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

@Tag(name = "管理后台 - IM 表情包")
@RestController
@RequestMapping("/im/face-pack")
@Validated
public class ImFacePackController {

    @Resource
    private ImFacePackService facePackService;
    @Resource
    private ImFacePackItemService facePackItemService;

    @GetMapping("/list")
    @Operation(summary = "获得启用的表情包列表（含表情）")
    public CommonResult<List<ImFacePackUserRespVO>> getFacePackList() {
        // 1.1 拉所有启用表情包
        List<ImFacePackDO> packs = facePackService.getEnabledFacePackList();
        if (packs.isEmpty()) {
            return success(ListUtil.of());
        }
        // 1.2 拉这些包下所有启用表情，按 packId 分组
        List<ImFacePackItemDO> items = facePackItemService.getEnabledItemListByPackIds(
                convertList(packs, ImFacePackDO::getId));
        Map<Long, List<ImFacePackItemDO>> itemsByPackId = convertMultiMap(items, ImFacePackItemDO::getPackId);

        // 2. 拼装：BeanUtils 把 pack 字段映射 + 自己塞 items
        List<ImFacePackUserRespVO> result = convertList(packs, pack -> {
            ImFacePackUserRespVO vo = BeanUtils.toBean(pack, ImFacePackUserRespVO.class);
            vo.setItems(BeanUtils.toBean(itemsByPackId.getOrDefault(pack.getId(), ListUtil.of()), ImFacePackUserRespVO.Item.class));
            return vo;
        });
        return success(result);
    }

}
