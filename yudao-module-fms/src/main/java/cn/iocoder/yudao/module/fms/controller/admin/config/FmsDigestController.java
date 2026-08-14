package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest.FmsDigestRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.digest.FmsDigestSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsDigestDO;
import cn.iocoder.yudao.module.fms.service.config.FmsDigestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * FMS 常用摘要 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - FMS 常用摘要")
@RestController
@RequestMapping("/fms/config/digest")
@Validated
public class FmsDigestController {

    @Resource
    private FmsDigestService digestService;

    @PostMapping("/create")
    @Operation(summary = "创建常用摘要")
    @PreAuthorize("@ss.hasPermission('fms:config:digest:create')")
    public CommonResult<Long> createDigest(@Valid @RequestBody FmsDigestSaveReqVO createReqVO) {
        return success(digestService.createDigest(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改常用摘要")
    @PreAuthorize("@ss.hasPermission('fms:config:digest:update')")
    public CommonResult<Boolean> updateDigest(@Valid @RequestBody FmsDigestSaveReqVO updateReqVO) {
        digestService.updateDigest(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除常用摘要")
    @Parameter(name = "id", description = "常用摘要编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:digest:delete')")
    public CommonResult<Boolean> deleteDigest(@RequestParam("accountSetId") @NotNull Long accountSetId,
                                              @RequestParam("id") @NotNull Long id) {
        digestService.deleteDigest(accountSetId, id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得常用摘要管理列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:digest:query')")
    public CommonResult<List<FmsDigestRespVO>> getDigestList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsDigestDO> list = digestService.getDigestList(accountSetId, getLoginUserId());
        return success(BeanUtils.toBean(list, FmsDigestRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得常用摘要精简列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    public CommonResult<List<FmsDigestRespVO>> getDigestSimpleList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsDigestDO> list = digestService.getDigestList(accountSetId, getLoginUserId());
        return success(convertList(list, digest -> new FmsDigestRespVO()
                .setId(digest.getId()).setAccountSetId(digest.getAccountSetId()).setContent(digest.getContent())));
    }

}
