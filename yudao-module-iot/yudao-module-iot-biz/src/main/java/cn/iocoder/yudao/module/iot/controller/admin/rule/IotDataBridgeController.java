package cn.iocoder.yudao.module.iot.controller.admin.rule;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.rule.vo.databridge.IotDataBridgeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotDataRuleSinkDO;
import cn.iocoder.yudao.module.iot.service.rule.IotDataBridgeService;
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

@Tag(name = "管理后台 - IoT 数据桥梁")
@RestController
@RequestMapping("/iot/data-bridge")
@Validated
public class IotDataBridgeController {

    @Resource
    private IotDataBridgeService dataBridgeService;

    @PostMapping("/create")
    @Operation(summary = "创建数据桥梁")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:create')")
    public CommonResult<Long> createDataBridge(@Valid @RequestBody IotDataBridgeSaveReqVO createReqVO) {
        return success(dataBridgeService.createDataBridge(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新数据桥梁")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:update')")
    public CommonResult<Boolean> updateDataBridge(@Valid @RequestBody IotDataBridgeSaveReqVO updateReqVO) {
        dataBridgeService.updateDataBridge(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除数据桥梁")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:delete')")
    public CommonResult<Boolean> deleteDataBridge(@RequestParam("id") Long id) {
        dataBridgeService.deleteDataBridge(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得数据桥梁")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:query')")
    public CommonResult<IotDataBridgeRespVO> getDataBridge(@RequestParam("id") Long id) {
        IotDataRuleSinkDO dataBridge = dataBridgeService.getDataBridge(id);
        return success(BeanUtils.toBean(dataBridge, IotDataBridgeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得数据桥梁分页")
    @PreAuthorize("@ss.hasPermission('iot:data-bridge:query')")
    public CommonResult<PageResult<IotDataBridgeRespVO>> getDataBridgePage(@Valid IotDataBridgePageReqVO pageReqVO) {
        PageResult<IotDataRuleSinkDO> pageResult = dataBridgeService.getDataBridgePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotDataBridgeRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获取数据桥梁的精简信息列表", description = "主要用于前端的下拉选项")
    public CommonResult<List<IotDataBridgeRespVO>> getSimpleDataBridgeList() {
        List<IotDataRuleSinkDO> list = dataBridgeService.getDataBridgeList(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, dataBridge -> // 只返回 id、name 字段
                new IotDataBridgeRespVO().setId(dataBridge.getId()).setName(dataBridge.getName())));
    }

}
