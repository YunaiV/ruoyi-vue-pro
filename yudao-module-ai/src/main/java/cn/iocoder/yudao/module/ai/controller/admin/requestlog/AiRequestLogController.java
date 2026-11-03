package cn.iocoder.yudao.module.ai.controller.admin.requestlog;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogStatisticsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.service.requestlog.AiRequestLogService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - AI 请求日志")
@RestController
@RequestMapping("/ai/request-log")
@Validated
public class AiRequestLogController {

    @Resource
    private AiRequestLogService requestLogService;

    @Resource
    private AdminUserApi adminUserApi;

    @GetMapping("/get")
    @Operation(summary = "获得请求日志")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('ai:request-log:query')")
    public CommonResult<AiRequestLogRespVO> getRequestLog(@RequestParam("id") Long id) {
        AiRequestLogDO requestLog = requestLogService.getRequestLog(id);
        if (requestLog == null) {
            return success(null);
        }
        return success(buildRequestLogList(Collections.singletonList(requestLog)).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得请求日志分页")
    @PreAuthorize("@ss.hasPermission('ai:request-log:query')")
    public CommonResult<PageResult<AiRequestLogRespVO>> getRequestLogPage(@Valid AiRequestLogPageReqVO pageReqVO) {
        PageResult<AiRequestLogDO> pageResult = requestLogService.getRequestLogPage(pageReqVO);
        return success(new PageResult<>(buildRequestLogList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得请求日志统计", description = "统计全部匹配筛选条件的记录，不受分页参数影响")
    @PreAuthorize("@ss.hasPermission('ai:request-log:query')")
    public CommonResult<AiRequestLogStatisticsRespVO> getRequestLogStatistics(@Valid AiRequestLogPageReqVO pageReqVO) {
        return success(requestLogService.getRequestLogStatistics(pageReqVO));
    }

    /**
     * 构建请求日志列表：拼接用户昵称，用户不存在时为空，前端展示用户编号
     *
     * @param list 请求日志列表
     * @return 请求日志 Response VO 列表
     */
    private List<AiRequestLogRespVO> buildRequestLogList(List<AiRequestLogDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(list, AiRequestLogDO::getUserId));
        return BeanUtils.toBean(list, AiRequestLogRespVO.class, requestLog -> MapUtils.findAndThen(userMap,
                requestLog.getUserId(), user -> requestLog.setUserNickname(user.getNickname())));
    }

}
