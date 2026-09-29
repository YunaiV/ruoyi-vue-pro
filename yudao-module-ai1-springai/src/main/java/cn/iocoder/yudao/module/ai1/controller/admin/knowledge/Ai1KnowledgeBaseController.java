package cn.iocoder.yudao.module.ai1.controller.admin.knowledge;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBasePageReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBaseRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeBaseSaveReqVO;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.base.Ai1KnowledgeSearchRespVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeDocumentDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeBaseService;
import cn.iocoder.yudao.module.ai1.service.knowledge.Ai1KnowledgeDocumentService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ModelService;
import cn.iocoder.yudao.module.ai1.service.model.Ai1ProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

@Tag(name = "管理后台 - AI1 知识库")
@RestController
@RequestMapping("/ai1/knowledge")
@Validated
public class Ai1KnowledgeBaseController {

    @Resource
    private Ai1KnowledgeBaseService knowledgeBaseService;
    @Resource
    private Ai1KnowledgeDocumentService knowledgeDocumentService;
    @Resource
    private Ai1ProviderService providerService;
    @Resource
    private Ai1ModelService modelService;

    @PostMapping("/create")
    @Operation(summary = "创建知识库")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:create')")
    public CommonResult<Long> createKnowledgeBase(@Valid @RequestBody Ai1KnowledgeBaseSaveReqVO createReqVO) {
        return success(knowledgeBaseService.createKnowledgeBase(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新知识库")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:update')")
    public CommonResult<Boolean> updateKnowledgeBase(@Valid @RequestBody Ai1KnowledgeBaseSaveReqVO updateReqVO) {
        knowledgeBaseService.updateKnowledgeBase(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除知识库", description = "连带删除文档与向量集合")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:delete')")
    public CommonResult<Boolean> deleteKnowledgeBase(@RequestParam("id") Long id) {
        knowledgeBaseService.deleteKnowledgeBase(id);
        return success(true);
    }

    @DeleteMapping("/delete-list")
    @Operation(summary = "批量删除知识库", description = "连带删除文档与向量集合")
    @Parameter(name = "ids", description = "编号列表", required = true)
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:delete')")
    public CommonResult<Boolean> deleteKnowledgeBaseList(@RequestParam("ids") List<Long> ids) {
        knowledgeBaseService.deleteKnowledgeBaseListByIds(ids);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得知识库")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:query')")
    public CommonResult<Ai1KnowledgeBaseRespVO> getKnowledgeBase(@RequestParam("id") Long id) {
        Ai1KnowledgeBaseDO knowledgeBase = knowledgeBaseService.getKnowledgeBase(id);
        return success(buildKnowledgeBaseRespVO(knowledgeBase));
    }

    @GetMapping("/page")
    @Operation(summary = "获得知识库分页")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:query')")
    public CommonResult<PageResult<Ai1KnowledgeBaseRespVO>> getKnowledgeBasePage(@Valid Ai1KnowledgeBasePageReqVO pageReqVO) {
        PageResult<Ai1KnowledgeBaseDO> pageResult = knowledgeBaseService.getKnowledgeBasePage(pageReqVO);
        return success(new PageResult<>(buildKnowledgeBaseRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得知识库精简列表", description = "只包含开启状态，用于 Agent 绑定知识库的下拉选择")
    public CommonResult<List<Ai1KnowledgeBaseRespVO>> getKnowledgeBaseSimpleList() {
        List<Ai1KnowledgeBaseDO> list = knowledgeBaseService.getKnowledgeBaseListByStatus(CommonStatusEnum.ENABLE.getStatus());
        return success(convertList(list, knowledgeBase -> new Ai1KnowledgeBaseRespVO()
                .setId(knowledgeBase.getId()).setName(knowledgeBase.getName())));
    }

    @GetMapping("/search")
    @Operation(summary = "知识库检索测试")
    @Parameter(name = "id", description = "知识库编号", required = true, example = "1024")
    @Parameter(name = "query", description = "查询文本", required = true, example = "三体舰队")
    @Parameter(name = "topK", description = "检索数量，为空时使用知识库配置", example = "5")
    @PreAuthorize("@ss.hasPermission('ai1:knowledge:search')")
    public CommonResult<List<Ai1KnowledgeSearchRespVO>> searchKnowledgeBase(
            @RequestParam("id") Long id,
            @RequestParam("query") @NotEmpty(message = "检索内容不能为空") String query,
            @RequestParam(value = "topK", required = false) Integer topK) {
        List<Ai1KnowledgeSearchRespVO> list = knowledgeBaseService.searchKnowledgeBase(id, query, topK);
        return success(buildKnowledgeSearchRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private Ai1KnowledgeBaseRespVO buildKnowledgeBaseRespVO(Ai1KnowledgeBaseDO knowledgeBase) {
        if (knowledgeBase == null) {
            return null;
        }
        return CollUtil.getFirst(buildKnowledgeBaseRespVOList(Collections.singletonList(knowledgeBase)));
    }

    private List<Ai1KnowledgeBaseRespVO> buildKnowledgeBaseRespVOList(List<Ai1KnowledgeBaseDO> list) {
        Map<Long, Ai1ProviderDO> providerMap = providerService.getProviderMap(
                convertSet(list, Ai1KnowledgeBaseDO::getEmbeddingProviderId));
        Map<Long, Ai1ModelDO> modelMap = modelService.getModelMap(
                convertSet(list, Ai1KnowledgeBaseDO::getEmbeddingModelId));
        return BeanUtils.toBean(list, Ai1KnowledgeBaseRespVO.class, respVO -> {
            MapUtils.findAndThen(providerMap, respVO.getEmbeddingProviderId(),
                    provider -> respVO.setEmbeddingProviderName(provider.getName()));
            MapUtils.findAndThen(modelMap, respVO.getEmbeddingModelId(),
                    model -> respVO.setEmbeddingModelName(model.getName()));
        });
    }

    private List<Ai1KnowledgeSearchRespVO> buildKnowledgeSearchRespVOList(List<Ai1KnowledgeSearchRespVO> list) {
        Map<Long, Ai1KnowledgeDocumentDO> documentMap = knowledgeDocumentService.getKnowledgeDocumentMap(
                convertSet(list, Ai1KnowledgeSearchRespVO::getDocumentId));
        list.forEach(respVO -> MapUtils.findAndThen(documentMap, respVO.getDocumentId(),
                document -> respVO.setDocumentName(document.getName())));
        return list;
    }

}
