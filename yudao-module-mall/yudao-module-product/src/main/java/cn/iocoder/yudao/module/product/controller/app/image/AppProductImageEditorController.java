package cn.iocoder.yudao.module.product.controller.app.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.product.controller.app.image.vo.AppProductImageEditReqVO;
import cn.iocoder.yudao.module.product.controller.app.image.vo.AppProductImageEditRespVO;
import cn.iocoder.yudao.module.product.service.image.ProductImageEditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 智能服装图片编辑
 * <p>
 * 对应 Python /api/v1/clothing/edit-image + /api/v1/clothing/virtual-tryon +
 * IntelligentImageEditor + AugmentedRealityFitting + Interactive3DViewer +
 * BatchImageProcessor
 *
 * @author deepay
 */
@Tag(name = "用户 App - 智能服装图片编辑")
@RestController
@RequestMapping("/product/image")
@Validated
public class AppProductImageEditorController {

    @Resource
    private ProductImageEditService productImageEditService;

    @PostMapping("/edit")
    @Operation(summary = "生成图片编辑变换参数（颜色/图案/版型/背景）")
    @PermitAll
    public CommonResult<AppProductImageEditRespVO> editImage(@Valid @RequestBody AppProductImageEditReqVO reqVO) {
        ProductImageEditService.ImageEditResultDTO dto =
                productImageEditService.buildEditTask(reqVO.getSpuId(),
                        reqVO.getEdits() != null ? reqVO.getEdits() : Collections.emptyList());
        AppProductImageEditRespVO vo = new AppProductImageEditRespVO();
        vo.setSpuId(dto.spuId);
        vo.setOriginalPicUrl(dto.originalPicUrl);
        vo.setTransformParams(dto.transformParams);
        vo.setEditHistory(dto.editHistory);
        vo.setPreviewNote(dto.previewNote);
        return success(vo);
    }

    @PostMapping("/virtual-tryon")
    @Operation(summary = "生成虚拟试衣配置（AR + 3D，对应 AugmentedRealityFitting）")
    @PermitAll
    public CommonResult<AppProductImageEditRespVO> virtualTryOn(@Valid @RequestBody AppProductImageEditReqVO reqVO) {
        ProductImageEditService.VirtualTryOnConfigDTO tryOnDto =
                productImageEditService.buildVirtualTryOnConfig(reqVO.getSpuId(), reqVO.getBodyMeasurements());
        AppProductImageEditRespVO vo = new AppProductImageEditRespVO();
        vo.setSpuId(reqVO.getSpuId());
        vo.setVirtualTryOnConfig(tryOnDto);
        return success(vo);
    }

    @GetMapping("/3d-config")
    @Operation(summary = "获取 3D 交互展示配置（Three.js / Babylon.js）")
    @Parameter(name = "spuId", description = "商品 SPU 编号", required = true)
    @PermitAll
    public CommonResult<AppProductImageEditRespVO> get3DConfig(@RequestParam("spuId") Long spuId) {
        Map<String, Object> config = productImageEditService.build3DViewerConfig(spuId);
        AppProductImageEditRespVO vo = new AppProductImageEditRespVO();
        vo.setSpuId(spuId);
        vo.setViewer3DConfig(config);
        return success(vo);
    }

    @PostMapping("/batch-edit")
    @Operation(summary = "提交批量图片编辑任务（BatchImageProcessor）")
    public CommonResult<AppProductImageEditRespVO> batchEdit(@Valid @RequestBody AppProductImageEditReqVO reqVO) {
        String jobId = productImageEditService.submitBatchEditJob(
                reqVO.getBatchSpuIds() != null ? reqVO.getBatchSpuIds() : Collections.emptyList(),
                reqVO.getEdits() != null ? reqVO.getEdits() : Collections.emptyList());
        AppProductImageEditRespVO vo = new AppProductImageEditRespVO();
        vo.setBatchJobId(jobId);
        vo.setPreviewNote("批量任务已提交，jobId=" + jobId);
        return success(vo);
    }

}
