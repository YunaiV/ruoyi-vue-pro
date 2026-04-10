package cn.iocoder.yudao.module.mes.controller.admin.pro.card;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.card.vo.process.MesProCardProcessSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.card.MesProCardProcessDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.process.MesProProcessDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationService;
import cn.iocoder.yudao.module.mes.service.pro.card.MesProCardProcessService;
import cn.iocoder.yudao.module.mes.service.pro.process.MesProProcessService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - MES 流转卡工序记录")
@RestController
@RequestMapping("/mes/pro/card-process")
@Validated
public class MesProCardProcessController {

    @Resource
    private MesProCardProcessService cardProcessService;

    @Resource
    private MesProProcessService processService;

    @Resource
    private MesMdWorkstationService workstationService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建流转卡工序记录")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:create')")
    public CommonResult<Long> createCardProcess(@Valid @RequestBody MesProCardProcessSaveReqVO createReqVO) {
        return success(cardProcessService.createCardProcess(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新流转卡工序记录")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:update')")
    public CommonResult<Boolean> updateCardProcess(@Valid @RequestBody MesProCardProcessSaveReqVO updateReqVO) {
        cardProcessService.updateCardProcess(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除流转卡工序记录")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:pro-card:delete')")
    public CommonResult<Boolean> deleteCardProcess(@RequestParam("id") Long id) {
        cardProcessService.deleteCardProcess(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得流转卡工序记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:query')")
    public CommonResult<MesProCardProcessRespVO> getCardProcess(@RequestParam("id") Long id) {
        MesProCardProcessDO cardProcess = cardProcessService.getCardProcess(id);
        if (cardProcess == null) {
            return success(null);
        }
        return success(buildCardProcessRespVOList(ListUtil.of(cardProcess)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得流转卡工序记录分页")
    @PreAuthorize("@ss.hasPermission('mes:pro-card:query')")
    public CommonResult<PageResult<MesProCardProcessRespVO>> getCardProcessPage(@Valid MesProCardProcessPageReqVO pageReqVO) {
        PageResult<MesProCardProcessDO> pageResult = cardProcessService.getCardProcessPage(pageReqVO);
        return success(new PageResult<>(buildCardProcessRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    // ==================== 拼接 VO ====================

    private List<MesProCardProcessRespVO> buildCardProcessRespVOList(List<MesProCardProcessDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取关联数据
        Map<Long, MesProProcessDO> processMap = CollectionUtils.convertMap(
                processService.getProcessList(new ArrayList<>(convertSet(list, MesProCardProcessDO::getProcessId))),
                MesProProcessDO::getId);
        Map<Long, MesMdWorkstationDO> workstationMap = workstationService.getWorkstationMap(
                convertSet(list, MesProCardProcessDO::getWorkstationId));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(list, MesProCardProcessDO::getUserId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesProCardProcessRespVO.class, vo -> {
            MapUtils.findAndThen(processMap, vo.getProcessId(), process ->
                    vo.setProcessCode(process.getCode()).setProcessName(process.getName()));
            MapUtils.findAndThen(workstationMap, vo.getWorkstationId(), ws ->
                    vo.setWorkstationCode(ws.getCode()).setWorkstationName(ws.getName()));
            MapUtils.findAndThen(userMap, vo.getUserId(),
                    user -> vo.setNickname(user.getNickname()));
        });
    }

}
