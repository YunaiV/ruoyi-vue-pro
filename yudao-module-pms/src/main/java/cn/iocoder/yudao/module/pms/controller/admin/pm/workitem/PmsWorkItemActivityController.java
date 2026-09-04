package cn.iocoder.yudao.module.pms.controller.admin.pm.workitem;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.pms.controller.admin.pm.workitem.vo.activity.PmsWorkItemActivityRespVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemActivityService;
import cn.iocoder.yudao.module.pms.service.pm.workitem.PmsWorkItemService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - PMS 工作项动态")
@RestController
@RequestMapping("/pms/pm/work-item-activity")
@Validated
public class PmsWorkItemActivityController {

    @Resource
    private PmsWorkItemActivityService activityService;
    @Resource
    private PmsWorkItemService workItemService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/list")
    @Operation(summary = "获得工作项动态列表")
    @Parameter(name = "workItemId", description = "工作项编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pms:pm:work-item:query')")
    public CommonResult<List<PmsWorkItemActivityRespVO>> getWorkItemActivityList(
            @RequestParam("workItemId") Long workItemId) {
        workItemService.getWorkItem(workItemId, getLoginUserId());
        List<PmsWorkItemActivityDO> activities = activityService.getWorkItemActivityList(workItemId);
        // 拼接 VO：批量查询操作人，并补充用户展示字段
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                convertSet(activities, PmsWorkItemActivityDO::getOperatorUserId));
        List<PmsWorkItemActivityRespVO> activityVOList = BeanUtils.toBean(
                activities, PmsWorkItemActivityRespVO.class, activityVO ->
                findAndThen(userMap, activityVO.getOperatorUserId(), user -> activityVO
                        .setOperatorUserName(user.getNickname()).setOperatorUserAvatar(user.getAvatar())));
        return success(activityVOList);
    }

}
