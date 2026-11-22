package cn.iocoder.yudao.module.iot.controller.admin.opcuaserver;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.opcuaserver.vo.IotOpcUaServerConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.opcuaserver.IotOpcUaServerConfigDO;
import cn.iocoder.yudao.module.iot.service.opcuaserver.IotOpcUaServerConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - IoT OPC UA Server 配置")
@RestController
@RequestMapping("/iot/opc-ua-server-config")
@Validated
public class IotOpcUaServerConfigController {

    @Resource
    private IotOpcUaServerConfigService opcUaServerConfigService;

    @PostMapping("/create")
    @Operation(summary = "创建 OPC UA Server 配置")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:create')")
    public CommonResult<Long> createOpcUaServerConfig(@Valid @RequestBody IotOpcUaServerConfigSaveReqVO createReqVO) {
        return success(opcUaServerConfigService.createOpcUaServerConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新 OPC UA Server 配置")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:update')")
    public CommonResult<Boolean> updateOpcUaServerConfig(@Valid @RequestBody IotOpcUaServerConfigSaveReqVO updateReqVO) {
        opcUaServerConfigService.updateOpcUaServerConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 OPC UA Server 配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:delete')")
    public CommonResult<Boolean> deleteOpcUaServerConfig(@RequestParam("id") Long id) {
        opcUaServerConfigService.deleteOpcUaServerConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得 OPC UA Server 配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:query')")
    public CommonResult<IotOpcUaServerConfigRespVO> getOpcUaServerConfig(@RequestParam("id") Long id) {
        IotOpcUaServerConfigDO config = opcUaServerConfigService.getOpcUaServerConfig(id);
        return success(BeanUtils.toBean(config, IotOpcUaServerConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得 OPC UA Server 配置分页")
    @PreAuthorize("@ss.hasPermission('iot:opc-ua-server:query')")
    public CommonResult<PageResult<IotOpcUaServerConfigRespVO>> getOpcUaServerConfigPage(
            @Valid IotOpcUaServerConfigPageReqVO pageReqVO) {
        PageResult<IotOpcUaServerConfigDO> pageResult = opcUaServerConfigService.getOpcUaServerConfigPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, IotOpcUaServerConfigRespVO.class));
    }

}
