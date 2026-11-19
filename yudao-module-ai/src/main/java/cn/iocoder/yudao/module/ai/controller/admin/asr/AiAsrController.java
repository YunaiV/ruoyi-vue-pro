package cn.iocoder.yudao.module.ai.controller.admin.asr;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrRecognizeReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.asr.AiAsrDO;
import cn.iocoder.yudao.module.ai.service.asr.AiAsrService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - AI 语音识别")
@RestController
@RequestMapping("/ai/asr")
public class AiAsrController {

    @Resource
    private AiAsrService asrService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/recognize")
    @Operation(summary = "识别音频中的语音", description = "音频先上传到文件服务；同步识别，成功、失败都会新增一条记录")
    public CommonResult<AiAsrRespVO> recognizeAsr(@Valid @RequestBody AiAsrRecognizeReqVO reqVO) {
        AiAsrDO asr = asrService.recognizeAsr(getLoginUserId(), reqVO);
        return success(BeanUtils.toBean(asr, AiAsrRespVO.class));
    }

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】识别记录分页")
    public CommonResult<PageResult<AiAsrRespVO>> getAsrMyPage(@Valid PageParam pageReqVO) {
        PageResult<AiAsrDO> pageResult = asrService.getAsrMyPage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, AiAsrRespVO.class));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】识别记录")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<AiAsrRespVO> getAsrMy(@RequestParam("id") Long id) {
        AiAsrDO asr = asrService.getAsrMy(id, getLoginUserId());
        return success(BeanUtils.toBean(asr, AiAsrRespVO.class));
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】识别记录")
    @Parameter(name = "id", required = true, description = "编号", example = "1024")
    public CommonResult<Boolean> deleteAsrMy(@RequestParam("id") Long id) {
        asrService.deleteAsrMy(id, getLoginUserId());
        return success(true);
    }

    // ================ 语音识别管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得识别记录分页")
    @PreAuthorize("@ss.hasPermission('ai:asr:query')")
    public CommonResult<PageResult<AiAsrRespVO>> getAsrPage(@Valid AiAsrPageReqVO pageReqVO) {
        PageResult<AiAsrDO> pageResult = asrService.getAsrPage(pageReqVO);
        return success(new PageResult<>(buildAsrList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得识别记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:asr:query')")
    public CommonResult<AiAsrRespVO> getAsr(@RequestParam("id") Long id) {
        AiAsrDO asr = asrService.getAsr(id);
        if (asr == null) {
            return success(null);
        }
        return success(buildAsrList(Collections.singletonList(asr)).get(0));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除识别记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('ai:asr:delete')")
    public CommonResult<Boolean> deleteAsr(@RequestParam("id") Long id) {
        asrService.deleteAsr(id);
        return success(true);
    }

    /**
     * 构建识别记录列表：管理端拼接用户昵称
     */
    private List<AiAsrRespVO> buildAsrList(List<AiAsrDO> list) {
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiAsrDO::getUserId));
        return BeanUtils.toBean(list, AiAsrRespVO.class, asr ->
                MapUtils.findAndThen(userMap, asr.getUserId(), user -> asr.setUserNickname(user.getNickname())));
    }

}
