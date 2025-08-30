package cn.iocoder.yudao.module.iot.controller.admin.alert;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config.IotAlertConfigPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config.IotAlertConfigRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.alert.vo.config.IotAlertConfigSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.alert.IotAlertConfigDO;
import cn.iocoder.yudao.module.iot.service.alert.IotAlertConfigService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
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
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - IoT 告警配置")
@RestController
@RequestMapping("/iot/alert-config")
@Validated
public class IotAlertConfigController {

    @Resource
    private IotAlertConfigService alertConfigService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建告警配置")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:create')")
    public CommonResult<Long> createAlertConfig(@Valid @RequestBody IotAlertConfigSaveReqVO createReqVO) {
        return success(alertConfigService.createAlertConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新告警配置")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:update')")
    public CommonResult<Boolean> updateAlertConfig(@Valid @RequestBody IotAlertConfigSaveReqVO updateReqVO) {
        alertConfigService.updateAlertConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除告警配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:alert-config:delete')")
    public CommonResult<Boolean> deleteAlertConfig(@RequestParam("id") Long id) {
        alertConfigService.deleteAlertConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得告警配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:query')")
    public CommonResult<IotAlertConfigRespVO> getAlertConfig(@RequestParam("id") Long id) {
        IotAlertConfigDO alertConfig = alertConfigService.getAlertConfig(id);
        return success(BeanUtils.toBean(alertConfig, IotAlertConfigRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得告警配置分页")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:query')")
    public CommonResult<PageResult<IotAlertConfigRespVO>> getAlertConfigPage(@Valid IotAlertConfigPageReqVO pageReqVO) {
        PageResult<IotAlertConfigDO> pageResult = alertConfigService.getAlertConfigPage(pageReqVO);

        // 转换返回
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSetByFlatMap(pageResult.getList(), config -> config.getReceiveUserIds().stream()));
        return success(BeanUtils.toBean(pageResult, IotAlertConfigRespVO.class, vo -> {
            vo.setReceiveUserNames(vo.getReceiveUserIds().stream()
                    .map(userMap::get)
                    .filter(Objects::nonNull)
                    .map(AdminUserRespDTO::getNickname)
                    .collect(Collectors.toList()));
        }));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得告警配置简单列表", description = "只包含被开启的告警配置，主要用于前端的下拉选项")
    @PreAuthorize("@ss.hasPermission('iot:alert-config:query')")
    public CommonResult<List<IotAlertConfigRespVO>> getAlertConfigSimpleList() {
        List<IotAlertConfigDO> list = alertConfigService.getAlertConfigListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, config -> // 只返回 id、name 字段
                new IotAlertConfigRespVO().setId(config.getId()).setName(config.getName())));
    }

}