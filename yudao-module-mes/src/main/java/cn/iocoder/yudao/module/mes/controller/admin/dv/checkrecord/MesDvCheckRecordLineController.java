package cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLineRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordLineDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.service.dv.checkrecord.MesDvCheckRecordLineService;
import cn.iocoder.yudao.module.mes.service.dv.subject.MesDvSubjectService;
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

@Tag(name = "管理后台 - MES 设备点检记录明细")
@RestController
@RequestMapping("/mes/dv/check-record-line")
@Validated
public class MesDvCheckRecordLineController {

    @Resource
    private MesDvCheckRecordLineService checkRecordLineService;

    @Resource
    private MesDvSubjectService subjectService;

    @PostMapping("/create")
    @Operation(summary = "创建设备点检记录明细")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-record:create')")
    public CommonResult<Long> createCheckRecordLine(@Valid @RequestBody MesDvCheckRecordLineSaveReqVO createReqVO) {
        return success(checkRecordLineService.createCheckRecordLine(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新设备点检记录明细")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-record:update')")
    public CommonResult<Boolean> updateCheckRecordLine(@Valid @RequestBody MesDvCheckRecordLineSaveReqVO updateReqVO) {
        checkRecordLineService.updateCheckRecordLine(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除设备点检记录明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-record:delete')")
    public CommonResult<Boolean> deleteCheckRecordLine(@RequestParam("id") Long id) {
        checkRecordLineService.deleteCheckRecordLine(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得设备点检记录明细")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-record:query')")
    public CommonResult<MesDvCheckRecordLineRespVO> getCheckRecordLine(@RequestParam("id") Long id) {
        MesDvCheckRecordLineDO line = checkRecordLineService.getCheckRecordLine(id);
        if (line == null) {
            return success(null);
        }
        return success(buildCheckRecordLineRespVOList(Collections.singletonList(line)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得设备点检记录明细分页")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-record:query')")
    public CommonResult<PageResult<MesDvCheckRecordLineRespVO>> getCheckRecordLinePage(@Valid MesDvCheckRecordLinePageReqVO pageReqVO) {
        PageResult<MesDvCheckRecordLineDO> pageResult = checkRecordLineService.getCheckRecordLinePage(pageReqVO);
        return success(new PageResult<>(buildCheckRecordLineRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesDvCheckRecordLineRespVO> buildCheckRecordLineRespVOList(List<MesDvCheckRecordLineDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesDvSubjectDO> subjectMap = subjectService.getSubjectMap(
                convertSet(list, MesDvCheckRecordLineDO::getSubjectId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesDvCheckRecordLineRespVO.class, vo ->
                MapUtils.findAndThen(subjectMap, vo.getSubjectId(), subject -> vo
                        .setSubjectCode(subject.getCode()).setSubjectName(subject.getName())
                        .setSubjectType(subject.getType()).setSubjectContent(subject.getContent()).setSubjectStandard(subject.getStandard())));
    }

}
