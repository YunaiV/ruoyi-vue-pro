package cn.iocoder.yudao.module.mes.controller.admin.pro.andon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon.MesProAndonConfigDO;
import cn.iocoder.yudao.module.mes.service.pro.andon.MesProAndonConfigService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 安灯呼叫配置")
@RestController
@RequestMapping("/mes/pro/andon-config")
@Validated
public class MesProAndonConfigController {

    @Resource
    private MesProAndonConfigService andonConfigService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建安灯呼叫配置")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:create')")
    public CommonResult<Long> createAndonConfig(@Valid @RequestBody MesProAndonConfigSaveReqVO createReqVO) {
        return success(andonConfigService.createAndonConfig(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新安灯呼叫配置")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:update')")
    public CommonResult<Boolean> updateAndonConfig(@Valid @RequestBody MesProAndonConfigSaveReqVO updateReqVO) {
        andonConfigService.updateAndonConfig(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安灯呼叫配置")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:delete')")
    public CommonResult<Boolean> deleteAndonConfig(@RequestParam("id") Long id) {
        andonConfigService.deleteAndonConfig(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得安灯呼叫配置")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:query')")
    public CommonResult<MesProAndonConfigRespVO> getAndonConfig(@RequestParam("id") Long id) {
        MesProAndonConfigDO config = andonConfigService.getAndonConfig(id);
        if (config == null) {
            return success(null);
        }
        return success(buildConfigRespVOList(ListUtil.of(config)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得安灯呼叫配置分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:query')")
    public CommonResult<PageResult<MesProAndonConfigRespVO>> getAndonConfigPage(@Valid MesProAndonConfigPageReqVO pageReqVO) {
        PageResult<MesProAndonConfigDO> pageResult = andonConfigService.getAndonConfigPage(pageReqVO);
        return success(new PageResult<>(buildConfigRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/list")
    @Operation(summary = "获得安灯呼叫配置列表")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-config:query')")
    public CommonResult<List<MesProAndonConfigRespVO>> getAndonConfigList() {
        List<MesProAndonConfigDO> list = andonConfigService.getAndonConfigList();
        return success(buildConfigRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesProAndonConfigRespVO> buildConfigRespVOList(List<MesProAndonConfigDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesProAndonConfigDO::getHandlerUserId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProAndonConfigRespVO.class, vo ->
                MapUtils.findAndThen(userMap, vo.getHandlerUserId(),
                        user -> vo.setHandlerUserNickname(user.getNickname())));
    }

}
