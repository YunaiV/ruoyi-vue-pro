package cn.iocoder.yudao.module.im.controller.admin.manager.channel;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialRespVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.channel.vo.material.ImChannelMaterialSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelDO;
import cn.iocoder.yudao.module.im.dal.dataobject.channel.ImChannelMaterialDO;
import cn.iocoder.yudao.module.im.service.channel.ImChannelMaterialService;
import cn.iocoder.yudao.module.im.service.channel.ImChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IM 频道素材")
@RestController
@RequestMapping("/im/manager/channel-material")
@Validated
public class ImChannelMaterialManagerController {

    @Resource
    private ImChannelMaterialService channelMaterialService;
    @Resource
    private ImChannelService channelService;

    @PostMapping("/create")
    @Operation(summary = "新增素材")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-material:create')")
    public CommonResult<Long> createMaterial(@Valid @RequestBody ImChannelMaterialSaveReqVO reqVO) {
        return success(channelMaterialService.createMaterial(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改素材")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-material:update')")
    public CommonResult<Boolean> updateMaterial(@Valid @RequestBody ImChannelMaterialSaveReqVO reqVO) {
        channelMaterialService.updateMaterial(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除素材")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-material:delete')")
    public CommonResult<Boolean> deleteMaterial(@RequestParam("id") Long id) {
        channelMaterialService.deleteMaterial(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得素材分页；含频道名回填")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-material:query')")
    public CommonResult<PageResult<ImChannelMaterialRespVO>> getMaterialPage(@Valid ImChannelMaterialPageReqVO pageReqVO) {
        PageResult<ImChannelMaterialDO> pageResult = channelMaterialService.getMaterialPage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        // 回填频道名
        List<ImChannelDO> channels = channelService.getChannelList(
                convertSet(pageResult.getList(), ImChannelMaterialDO::getChannelId));
        Map<Long, ImChannelDO> channelMap = convertMap(channels, ImChannelDO::getId);
        return success(BeanUtils.toBean(pageResult, ImChannelMaterialRespVO.class, vo ->
                MapUtils.findAndThen(channelMap, vo.getChannelId(), c -> vo.setChannelName(c.getName()))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得素材详情（含富文本正文）")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('im:manager:channel-material:query')")
    public CommonResult<ImChannelMaterialRespVO> getMaterial(@RequestParam("id") Long id) {
        ImChannelMaterialDO material = channelMaterialService.getMaterial(id);
        return success(BeanUtils.toBean(material, ImChannelMaterialRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得指定频道下的素材精简列表；用于推送弹窗的素材下拉")
    @Parameter(name = "channelId", description = "频道编号", required = true, example = "1")
    public CommonResult<List<ImChannelMaterialRespVO>> getSimpleMaterialList(@RequestParam("channelId") Long channelId) {
        List<ImChannelMaterialDO> list = channelMaterialService.getMaterialListByChannelId(channelId);
        return success(BeanUtils.toBean(list, ImChannelMaterialRespVO.class));
    }

}
