package cn.iocoder.yudao.module.fms.controller.admin.config;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword.FmsVoucherWordRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.voucherword.FmsVoucherWordSaveReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsVoucherWordDO;
import cn.iocoder.yudao.module.fms.service.config.FmsVoucherWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
 * FMS 凭证字 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - FMS 凭证字")
@RestController
@RequestMapping("/fms/config/voucher-word")
@Validated
public class FmsVoucherWordController {

    @Resource
    private FmsVoucherWordService voucherWordService;

    @PostMapping("/create")
    @Operation(summary = "创建凭证字")
    @PreAuthorize("@ss.hasPermission('fms:config:voucher-word:create')")
    public CommonResult<Long> createVoucherWord(@Valid @RequestBody FmsVoucherWordSaveReqVO createReqVO) {
        return success(voucherWordService.createVoucherWord(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "修改凭证字")
    @PreAuthorize("@ss.hasPermission('fms:config:voucher-word:update')")
    public CommonResult<Boolean> updateVoucherWord(@Valid @RequestBody FmsVoucherWordSaveReqVO updateReqVO) {
        voucherWordService.updateVoucherWord(updateReqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除凭证字")
    @Parameter(name = "id", description = "凭证字编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:voucher-word:delete')")
    public CommonResult<Boolean> deleteVoucherWord(
            @RequestParam("accountSetId") @NotNull Long accountSetId,
            @RequestParam("id") @NotNull Long id) {
        voucherWordService.deleteVoucherWord(accountSetId, id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/list")
    @Operation(summary = "获得凭证字管理列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('fms:config:voucher-word:query')")
    public CommonResult<List<FmsVoucherWordRespVO>> getVoucherWordList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        List<FmsVoucherWordDO> list = voucherWordService.getVoucherWordList(accountSetId, getLoginUserId());
        return success(BeanUtils.toBean(list, FmsVoucherWordRespVO.class));
    }

    @GetMapping("/simple-list")
    @Operation(summary = "获得凭证字精简列表")
    @Parameter(name = "accountSetId", description = "账套编号", required = true, example = "1024")
    public CommonResult<List<FmsVoucherWordRespVO>> getVoucherWordSimpleList(
            @RequestParam("accountSetId") @NotNull Long accountSetId) {
        return success(convertList(voucherWordService.getVoucherWordList(
                accountSetId, getLoginUserId()), voucherWord -> new FmsVoucherWordRespVO()
                .setId(voucherWord.getId()).setName(voucherWord.getName())
                .setDefaultStatus(voucherWord.getDefaultStatus())));
    }

}
