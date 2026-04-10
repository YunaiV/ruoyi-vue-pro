package cn.iocoder.yudao.module.mes.controller.admin.md.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.worker.MesMdWorkstationWorkerRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.md.workstation.vo.worker.MesMdWorkstationWorkerSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationWorkerDO;
import cn.iocoder.yudao.module.mes.service.md.workstation.MesMdWorkstationWorkerService;
import cn.iocoder.yudao.module.system.api.dept.PostApi;
import cn.iocoder.yudao.module.system.api.dept.dto.PostRespDTO;
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

@Tag(name = "管理后台 - MES 人力资源")
@RestController
@RequestMapping("/mes/md-workstation-worker")
@Validated
public class MesMdWorkstationWorkerController {

    @Resource
    private MesMdWorkstationWorkerService workstationWorkerService;

    @Resource
    private PostApi postApi;

    @PostMapping("/create")
    @Operation(summary = "创建人力资源")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Long> createWorkstationWorker(@Valid @RequestBody MesMdWorkstationWorkerSaveReqVO createReqVO) {
        return success(workstationWorkerService.createWorkstationWorker(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新人力资源")
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> updateWorkstationWorker(@Valid @RequestBody MesMdWorkstationWorkerSaveReqVO updateReqVO) {
        workstationWorkerService.updateWorkstationWorker(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除人力资源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:update')")
    public CommonResult<Boolean> deleteWorkstationWorker(@RequestParam("id") Long id) {
        workstationWorkerService.deleteWorkstationWorker(id);
        return success(true);
    }

    @GetMapping("/list-by-workstation")
    @Operation(summary = "获得人力资源列表")
    @Parameter(name = "workstationId", description = "工作站编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:md-workstation:query')")
    public CommonResult<List<MesMdWorkstationWorkerRespVO>> getWorkstationWorkerList(
            @RequestParam("workstationId") Long workstationId) {
        List<MesMdWorkstationWorkerDO> list = workstationWorkerService.getWorkstationWorkerListByWorkstationId(workstationId);
        return success(buildWorkstationWorkerRespVOList(list));
    }

    // ==================== 拼接 VO ====================

    private List<MesMdWorkstationWorkerRespVO> buildWorkstationWorkerRespVOList(List<MesMdWorkstationWorkerDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1. 批量获取岗位信息
        Map<Long, PostRespDTO> postMap = postApi.getPostMap(
                convertSet(list, MesMdWorkstationWorkerDO::getPostId));
        // 2. 拼接 VO
        return BeanUtils.toBean(list, MesMdWorkstationWorkerRespVO.class, vo -> {
            MapUtils.findAndThen(postMap, vo.getPostId(),
                    post -> vo.setPostName(post.getName()));
        });
    }

}
