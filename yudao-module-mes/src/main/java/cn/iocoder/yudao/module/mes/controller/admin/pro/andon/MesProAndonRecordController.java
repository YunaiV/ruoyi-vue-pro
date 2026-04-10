package cn.iocoder.yudao.module.mes.controller.admin.pro.andon;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record.MesProAndonRecordUpdateReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record.MesProAndonRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record.MesProAndonRecordRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.record.MesProAndonRecordCreateReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon.MesProAndonRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workorder.MesProWorkOrderDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.andon.MesProAndonRecordService;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import cn.iocoder.yudao.module.mes.service.pro.workorder.MesProWorkOrderService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;

@Tag(name = "管理后台 - MES 安灯呼叫记录")
@RestController
@RequestMapping("/mes/pro/andon-record")
@Validated
public class MesProAndonRecordController {

    @Resource
    private MesProAndonRecordService andonRecordService;

    @Resource
    private MesMdWorkstationService workstationService;

    @Resource
    private MesProWorkOrderService workOrderService;

    @Resource
    private MesProProcessService processService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建安灯呼叫记录")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:create')")
    public CommonResult<Long> createAndonRecord(@Valid @RequestBody MesProAndonRecordCreateReqVO createReqVO) {
        return success(andonRecordService.createAndonRecord(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除安灯呼叫记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:delete')")
    public CommonResult<Boolean> deleteAndonRecord(@RequestParam("id") Long id) {
        andonRecordService.deleteAndonRecord(id);
        return success(true);
    }

    @PutMapping("/update")
    @Operation(summary = "更新安灯呼叫记录")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:update')")
    public CommonResult<Boolean> updateAndonRecord(@Valid @RequestBody MesProAndonRecordUpdateReqVO updateReqVO) {
        andonRecordService.updateAndonRecord(updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得安灯呼叫记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:query')")
    public CommonResult<MesProAndonRecordRespVO> getAndonRecord(@RequestParam("id") Long id) {
        MesProAndonRecordDO record = andonRecordService.getAndonRecord(id);
        if (record == null) {
            return success(null);
        }
        return success(buildRecordRespVOList(ListUtil.of(record)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得安灯呼叫记录分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:query')")
    public CommonResult<PageResult<MesProAndonRecordRespVO>> getAndonRecordPage(@Valid MesProAndonRecordPageReqVO pageReqVO) {
        PageResult<MesProAndonRecordDO> pageResult = andonRecordService.getAndonRecordPage(pageReqVO);
        return success(new PageResult<>(buildRecordRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出安灯呼叫记录 Excel")
    @PreAuthorize("@ss.hasPermission('mes:pro-andon-record:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAndonRecordExcel(@Valid MesProAndonRecordPageReqVO pageReqVO,
                                       HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<MesProAndonRecordDO> list = andonRecordService.getAndonRecordPage(pageReqVO).getList();
        List<MesProAndonRecordRespVO> voList = buildRecordRespVOList(list);
        ExcelUtils.write(response, "安灯呼叫记录.xls", "数据", MesProAndonRecordRespVO.class, voList);
    }

    // ==================== 拼接 VO ====================

    private List<MesProAndonRecordRespVO> buildRecordRespVOList(List<MesProAndonRecordDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesProAndonRecordDO::getWorkstationId));
        Map<Long, MesProWorkOrderDO> workOrderMap = workOrderService.getWorkOrderMap(
                convertSet(list, MesProAndonRecordDO::getWorkOrderId));
        Map<Long, MesProProcessDO> processMap = processService.getProcessMap(
                new ArrayList<>(convertSet(list, MesProAndonRecordDO::getProcessId)));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(list,
                record -> Stream.of(record.getUserId(), record.getHandlerUserId())));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProAndonRecordRespVO.class, vo -> {
            MapUtils.findAndThen(workstationMap, vo.getWorkstationId(), ws ->
                    vo.setWorkstationCode(ws.getCode()).setWorkstationName(ws.getName()));
            MapUtils.findAndThen(workOrderMap, vo.getWorkOrderId(),
                    wo -> vo.setWorkOrderCode(wo.getCode()));
            MapUtils.findAndThen(processMap, vo.getProcessId(),
                    process -> vo.setProcessName(process.getName()));
            MapUtils.findAndThen(userMap, vo.getUserId(),
                    user -> vo.setUserNickname(user.getNickname()));
            MapUtils.findAndThen(userMap, vo.getHandlerUserId(),
                    user -> vo.setHandlerUserNickname(user.getNickname()));
        });
    }

}
