package cn.iocoder.yudao.module.ai.controller.admin.image;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dConvertReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashion3dResultRespVO;
import cn.iocoder.yudao.module.ai.service.image.AiFashion3dService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI 服装 3D 模型
 *
 * @author deepay
 */
@Tag(name = "管理后台 - AI 服装3D模型")
@RestController
@RequestMapping("/ai/fashion/3d")
@Validated
@Slf4j
public class AiFashion3dController {

    @Resource
    private AiFashion3dService fashion3dService;

    @PostMapping("/convert")
    @Operation(summary = "2D 转 3D（异步）", description = "立即返回资产ID，使用 GET /get 轮询处理结果")
    @PreAuthorize("@ss.hasPermission('ai:image:fashion:create')")
    public CommonResult<AiFashion3dResultRespVO> convert(@RequestBody AiFashion3dConvertReqVO reqVO) {
        AiFashion3dResultRespVO resp = fashion3dService.convert(getLoginUserId(), reqVO);
        return success(resp);
    }

    @GetMapping("/get")
    @Operation(summary = "查询 3D 资产结果")
    @Parameter(name = "id", description = "资产编号", required = true, example = "1001")
    @PreAuthorize("@ss.hasPermission('ai:image:fashion:create')")
    public CommonResult<AiFashion3dResultRespVO> getResult(@RequestParam("id") Long id) {
        return success(fashion3dService.getResult(id));
    }

    @PostMapping("/color")
    @Operation(summary = "重新应用颜色变换")
    @Parameter(name = "id", description = "资产编号", required = true, example = "1001")
    @Parameter(name = "colorHex", description = "目标颜色 Hex", required = true, example = "#FF0000")
    @PreAuthorize("@ss.hasPermission('ai:image:fashion:create')")
    public CommonResult<AiFashion3dResultRespVO> changeColor(
            @RequestParam("id") Long id,
            @RequestParam("colorHex") String colorHex) {
        return success(fashion3dService.changeColor(id, colorHex));
    }

}
