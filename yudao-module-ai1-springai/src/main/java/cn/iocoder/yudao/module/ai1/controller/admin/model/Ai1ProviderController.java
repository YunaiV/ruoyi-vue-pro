package cn.iocoder.yudao.module.ai1.controller.admin.model;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderConnectRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderSaveReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ProviderService;
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

@Tag(name = "管理后台 - AI1 供应商")
@RestController
@RequestMapping("/ai1/provider")
@Validated
public class Ai1ProviderController {

    @Resource
    private Ai1ProviderService providerService;

    @PostMapping("/create")
    @Operation(summary = "创建供应商")
    @PreAuthorize("@ss.hasPermission('ai1:provider:create')")
    public CommonResult<Long> createProvider(@Valid @RequestBody Ai1ProviderSaveReqVO createReqVO) {
        return success(providerService.createProvider(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新供应商")
    @PreAuthorize("@ss.hasPermission('ai1:provider:update')")
    public CommonResult<Boolean> updateProvider(@Valid @RequestBody Ai1ProviderSaveReqVO updateReqVO) {
        providerService.updateProvider(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除供应商")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:provider:delete')")
    public CommonResult<Boolean> deleteProvider(@RequestParam("id") Long id) {
        providerService.deleteProvider(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除供应商")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:provider:delete')")
    public CommonResult<Boolean> deleteProviderList(@RequestParam("ids") List<Long> ids) {
        providerService.deleteProviderListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得供应商")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:provider:query')")
    public CommonResult<Ai1ProviderRespVO> getProvider(@RequestParam("id") Long id) {
        Ai1ProviderDO provider = providerService.getProvider(id);
        return success(buildProviderRespVO(provider));
    }

    @GetMapping("/page")
    @Operation(summary = "获得供应商分页")
    @PreAuthorize("@ss.hasPermission('ai1:provider:query')")
    public CommonResult<PageResult<Ai1ProviderRespVO>> getProviderPage(@Valid Ai1ProviderPageReqVO pageReqVO) {
        PageResult<Ai1ProviderDO> pageResult = providerService.getProviderPage(pageReqVO);
        return success(new PageResult<>(convertList(pageResult.getList(), this::buildProviderRespVO), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得供应商精简列表", description = "只包含开启状态，用于 Agent 模型、知识库向量化模型的下拉选择")
    public CommonResult<List<Ai1ProviderRespVO>> getProviderSimpleList() {
        List<Ai1ProviderDO> list = providerService.getProviderListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, provider -> new Ai1ProviderRespVO().setId(provider.getId()).setName(provider.getName())));
    }

    @PostMapping("/test")
    @Operation(summary = "供应商连通测试", description = "GET /models 优先，失败时回退 POST /chat/completions")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:provider:test')")
    public CommonResult<Ai1ProviderConnectRespVO> testProvider(@RequestParam("id") Long id) {
        return success(providerService.testProviderConnect(id));
    }

    // ==================== 拼接 VO ====================

    /**
     * 构建供应商响应：API 密钥脱敏返回，避免查询权限直接获取完整密钥
     */
    private Ai1ProviderRespVO buildProviderRespVO(Ai1ProviderDO provider) {
        if (provider == null) {
            return null;
        }
        Ai1ProviderRespVO respVO = BeanUtils.toBean(provider, Ai1ProviderRespVO.class);
        String apiKey = provider.getApiKey();
        if (StrUtil.contains(apiKey, "${")) {
            // 环境变量占位符不是密钥本身，原样返回，便于确认引用了哪个变量
            respVO.setApiKey(apiKey);
        } else if (StrUtil.isNotEmpty(apiKey)) {
            respVO.setApiKey(apiKey.length() <= 8 ? "****"
                    : StrUtil.hide(apiKey, 3, apiKey.length() - 4));
        }
        return respVO;
    }

}
