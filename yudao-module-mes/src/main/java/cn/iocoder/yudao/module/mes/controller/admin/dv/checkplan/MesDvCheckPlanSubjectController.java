package cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject.MesDvCheckPlanSubjectRespVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkplan.vo.subject.MesDvCheckPlanSubjectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkplan.MesDvCheckPlanSubjectDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.subject.MesDvSubjectDO;
import cn.iocoder.yudao.module.mes.service.dv.checkplan.MesDvCheckPlanSubjectService;
import cn.iocoder.yudao.module.mes.service.dv.subject.MesDvSubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - MES 点检保养方案项目")
@RestController
@RequestMapping("/mes/dv/check-plan-subject")
@Validated
public class MesDvCheckPlanSubjectController {

    @Resource
    private MesDvCheckPlanSubjectService checkPlanSubjectService;
    @Resource
    private MesDvSubjectService subjectService;

    @PostMapping("/create")
    @Operation(summary = "创建方案项目关联")
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:update')")
    public CommonResult<Long> createCheckPlanSubject(@Valid @RequestBody MesDvCheckPlanSubjectSaveReqVO createReqVO) {
        return success(checkPlanSubjectService.createCheckPlanSubject(createReqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除方案项目关联")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:update')")
    public CommonResult<Boolean> deleteCheckPlanSubject(@RequestParam("id") Long id) {
        checkPlanSubjectService.deleteCheckPlanSubject(id);
        return success(true);
    }

    @GetMapping("/list-by-plan")
    @Operation(summary = "获得指定方案的项目列表")
    @Parameter(name = "planId", description = "方案编号", required = true)
    @PreAuthorize("@ss.hasPermission('mes:dv-check-plan:query')")
    public CommonResult<List<MesDvCheckPlanSubjectRespVO>> getCheckPlanSubjectListByPlan(
            @RequestParam("planId") Long planId) {
        List<MesDvCheckPlanSubjectDO> list = checkPlanSubjectService.getCheckPlanSubjectListByPlanId(planId);
        List<MesDvCheckPlanSubjectRespVO> respList = BeanUtils.toBean(list, MesDvCheckPlanSubjectRespVO.class);
        // 拼装项目编码/名称/类型/内容/标准
        // MesDvCheckPlanMachineryController.java 参考下里面的 todo；
        if (CollUtil.isNotEmpty(respList)) {
            List<Long> subjectIds = CollectionUtils.convertList(respList, MesDvCheckPlanSubjectRespVO::getSubjectId);
            Map<Long, MesDvSubjectDO> subjectMap = subjectService.getSubjectMap(subjectIds);
            respList.forEach(resp -> {
                MesDvSubjectDO subject = subjectMap.get(resp.getSubjectId());
                if (subject != null) {
                    resp.setSubjectCode(subject.getCode());
                    resp.setSubjectName(subject.getName());
                    resp.setSubjectType(subject.getType());
                    resp.setSubjectContent(subject.getContent());
                    resp.setSubjectStandard(subject.getStandard());
                }
            });
        }
        return success(respList);
    }

}
