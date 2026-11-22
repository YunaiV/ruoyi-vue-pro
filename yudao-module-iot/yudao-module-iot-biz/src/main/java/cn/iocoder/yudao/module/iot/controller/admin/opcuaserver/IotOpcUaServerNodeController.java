package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerNodeSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerNodeDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerNodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - IoT OPC UA Server 节点")
@RestController
@RequestMapping("/iot/opc-ua-server-node")
@Validated
public class IotOpcUaServerNodeController {

    @Resource
    private IotOpcUaServerNodeService opcUaServerNodeService;
    @Resource
    private IotDeviceService deviceService;

    @PostMapping("/create")
    @Operation(summary = "创建 OPC UA Server 节点")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:update')")
    public CommonResult<Long> createOpcUaServerNode(@Valid @RequestBody IotOpcUaServerNodeSaveReqVO createReqVO) {
        return success(opcUaServerNodeService.createOpcUaServerNode(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 OPC UA Server 节点")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:update')")
    public CommonResult<Boolean> updateOpcUaServerNode(@Valid @RequestBody IotOpcUaServerNodeSaveReqVO updateReqVO) {
        opcUaServerNodeService.updateOpcUaServerNode(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 OPC UA Server 节点")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:update')")
    public CommonResult<Boolean> deleteOpcUaServerNode(@RequestParam("id") Long id) {
        opcUaServerNodeService.deleteOpcUaServerNode(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OPC UA Server 节点")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:query')")
    public CommonResult<IotOpcUaServerNodeRespVO> getOpcUaServerNode(@RequestParam("id") Long id) {
        IotOpcUaServerNodeDO node = opcUaServerNodeService.getOpcUaServerNode(id);
        if (node == null) {
            return success(null);
        }
        return success(buildNodeList(Collections.singletonList(node)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OPC UA Server 节点分页")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:query')")
    public CommonResult<PageResult<IotOpcUaServerNodeRespVO>> getOpcUaServerNodePage(
            @Valid IotOpcUaServerNodePageReqVO pageReqVO) {
        PageResult<IotOpcUaServerNodeDO> pageResult = opcUaServerNodeService.getOpcUaServerNodePage(pageReqVO);
        return success(new PageResult<>(buildNodeList(pageResult.getList()), pageResult.getTotal()));
    }

    /**
     * 拼接设备名称、物模型属性：节点不冗余标识符和名称；关联不到时标为失效
     */
    private List<IotOpcUaServerNodeRespVO> buildNodeList(List<IotOpcUaServerNodeDO> nodes) {
        if (CollUtil.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        Map<Long, IotDeviceDO> deviceMap = deviceService.getDeviceMap(convertSet(nodes, IotOpcUaServerNodeDO::getDeviceId));
        Map<Long, IotThingModelDO> thingModelMap = opcUaServerNodeService.getPropertyThingModelMap(
                convertSet(deviceMap.values(), IotDeviceDO::getProductId));
        return BeanUtils.toBean(nodes, IotOpcUaServerNodeRespVO.class, nodeVO -> {
            IotDeviceDO device = deviceMap.get(nodeVO.getDeviceId());
            if (device != null) {
                nodeVO.setDeviceName(device.getDeviceName());
            }
            IotThingModelDO thingModel = thingModelMap.get(nodeVO.getThingModelId());
            if (device == null || thingModel == null || ObjUtil.notEqual(device.getProductId(), thingModel.getProductId())) {
                nodeVO.setThingModelValid(false);
                return;
            }
            nodeVO.setThingModelValid(true).setIdentifier(thingModel.getIdentifier()).setName(thingModel.getName());
        });
    }

}
