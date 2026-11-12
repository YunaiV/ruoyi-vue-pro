package cn.iocoder.yudao.module.ai.controller.admin.ppt;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.*;
import cn.iocoder.yudao.module.ai.dal.dataobject.ppt.AiPptDO;
import cn.iocoder.yudao.module.ai.service.ppt.AiPptService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static java.util.Collections.singletonList;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台 - AI PPT
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - AI PPT")
@RestController
@RequestMapping("/ai/ppt")
public class AiPptController {

    @Resource
    private AiPptService pptService;

    @Resource
    private AdminUserApi adminUserApi;

    // ================ 创作 ================

    @PostMapping("/generate")
    @Operation(summary = "生成 PPT", description = "异步生成分镜、逐页生图，前端轮询进度")
    public CommonResult<Long> generatePpt(@Valid @RequestBody AiPptGenerateReqVO reqVO) {
        return success(pptService.generatePpt(getLoginUserId(), reqVO));
    }

    @PostMapping("/resume-my")
    @Operation(summary = "继续生成【我的】PPT", description = "仅已失败；没有分镜时重新生成分镜，否则只补没有图的页")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    public CommonResult<Boolean> resumePptMy(@RequestParam("id") Long id) {
        pptService.resumePptMy(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/redraw-slide-my")
    @Operation(summary = "重绘【我的】PPT 的一页", description = "使用修改后的完整提示词异步文生图，成功后追加为新版本")
    public CommonResult<Boolean> redrawSlideMy(@Valid @RequestBody AiPptRedrawReqVO reqVO) {
        pptService.redrawSlideMy(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/update-slide-image-my")
    @Operation(summary = "切换【我的】PPT 一页的图片版本")
    public CommonResult<Boolean> updateSlideImageMy(@Valid @RequestBody AiPptSlideImageReqVO reqVO) {
        pptService.updateSlideImageMy(reqVO, getLoginUserId());
        return success(true);
    }

    // ================ 我的 PPT ================

    @GetMapping("/my-page")
    @Operation(summary = "获得【我的】PPT 分页")
    public CommonResult<PageResult<AiPptRespVO>> getPptMyPage(@Valid PageParam pageReqVO) {
        PageResult<AiPptDO> pageResult = pptService.getPptMyPage(getLoginUserId(), pageReqVO);
        return success(new PageResult<>(buildPptDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get-my")
    @Operation(summary = "获得【我的】PPT")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    public CommonResult<AiPptRespVO> getPptMy(@RequestParam("id") Long id) {
        AiPptDO ppt = pptService.getPptMy(id, getLoginUserId());
        return success(buildPptDetail(ppt));
    }

    @GetMapping("/export-my")
    @Operation(summary = "导出【我的】PPT", description = "仅已完成；pptx、pdf 每页一张整页图")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    @Parameter(name = "format", required = true, description = "格式：pptx、pdf", example = "pptx")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPptMy(@RequestParam("id") Long id, @RequestParam("format") String format,
                            HttpServletResponse response) throws IOException {
        AiPptDO ppt = pptService.getPptMy(id, getLoginUserId());
        writePpt(ppt, format, response);
    }

    @DeleteMapping("/delete-my")
    @Operation(summary = "删除【我的】PPT", description = "仅已完成、已失败")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    public CommonResult<Boolean> deletePptMy(@RequestParam("id") Long id) {
        pptService.deletePptMy(id, getLoginUserId());
        return success(true);
    }

    // ================ PPT 管理 ================

    @GetMapping("/page")
    @Operation(summary = "获得 PPT 分页")
    @PreAuthorize("@ss.hasPermission('ai:ppt:query')")
    public CommonResult<PageResult<AiPptRespVO>> getPptPage(@Valid AiPptPageReqVO pageReqVO) {
        PageResult<AiPptDO> pageResult = pptService.getPptPage(pageReqVO);
        return success(new PageResult<>(buildPptDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得 PPT")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:ppt:query')")
    public CommonResult<AiPptRespVO> getPpt(@RequestParam("id") Long id) {
        AiPptDO ppt = pptService.getPpt(id);
        return success(buildPptDetail(ppt));
    }

    @GetMapping("/export")
    @Operation(summary = "导出 PPT", description = "仅已完成；pptx、pdf 每页一张整页图")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    @Parameter(name = "format", required = true, description = "格式：pptx、pdf", example = "pptx")
    @PreAuthorize("@ss.hasPermission('ai:ppt:query')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportPpt(@RequestParam("id") Long id, @RequestParam("format") String format,
                          HttpServletResponse response) throws IOException {
        AiPptDO ppt = pptService.validatePptExists(id);
        writePpt(ppt, format, response);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除 PPT", description = "仅已完成、已失败")
    @Parameter(name = "id", required = true, description = "PPT 编号", example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:ppt:delete')")
    public CommonResult<Boolean> deletePpt(@RequestParam("id") Long id) {
        pptService.deletePpt(id);
        return success(true);
    }

    /**
     * 导出文件：拼接完成后再写响应，失败时按普通的错误结果返回
     */
    private void writePpt(AiPptDO ppt, String format, HttpServletResponse response) throws IOException {
        byte[] content = pptService.exportPpt(ppt, format);
        ServletUtils.writeAttachment(response, ppt.getTitle() + "." + format, content);
    }

    private AiPptRespVO buildPptDetail(AiPptDO ppt) {
        if (ppt == null) {
            return null;
        }
        return buildPptDetailList(singletonList(ppt)).get(0);
    }

    private List<AiPptRespVO> buildPptDetailList(List<AiPptDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiPptDO::getUserId));
        return BeanUtils.toBean(list, AiPptRespVO.class, ppt -> MapUtils.findAndThen(userMap, ppt.getUserId(),
                user -> ppt.setUserNickname(user.getNickname())));
    }

}
