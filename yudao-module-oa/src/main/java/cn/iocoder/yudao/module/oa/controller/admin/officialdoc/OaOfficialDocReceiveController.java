package cn.iocoder.yudao.module.oa.controller.admin.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceivePageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceiveRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.receive.OaOfficialDocReceiveSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocReceiveDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO;
import cn.iocoder.yudao.module.oa.service.officialdoc.OaOfficialDocReceiveService;
import cn.iocoder.yudao.module.oa.service.officialdoc.OaOfficialDocSendService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSetByFlatMap;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 公文收文")
@RestController
@RequestMapping("/oa/officialdoc-receive")
@Validated
public class OaOfficialDocReceiveController {

    @Resource
    private OaOfficialDocReceiveService officialDocReceiveService;
    @Resource
    private OaOfficialDocSendService officialDocSendService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建公文收文")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:create')")
    public CommonResult<Long> createOfficialDocReceive(@Valid @RequestBody OaOfficialDocReceiveSaveReqVO reqVO) {
        return success(officialDocReceiveService.createOfficialDocReceive(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新公文收文")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:update')")
    public CommonResult<Boolean> updateOfficialDocReceive(@Valid @RequestBody OaOfficialDocReceiveSaveReqVO reqVO) {
        officialDocReceiveService.updateOfficialDocReceive(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除公文收文")
    @Parameter(name = "id", description = "收文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:delete')")
    public CommonResult<Boolean> deleteOfficialDocReceive(@RequestParam("id") Long id) {
        officialDocReceiveService.deleteOfficialDocReceive(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交公文收文审批")
    @Parameter(name = "id", description = "收文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:update')")
    public CommonResult<Boolean> submitOfficialDocReceive(@RequestParam("id") Long id) {
        officialDocReceiveService.submitOfficialDocReceive(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤销公文收文审批")
    @Parameter(name = "id", description = "收文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:update')")
    public CommonResult<Boolean> cancelOfficialDocReceive(@RequestParam("id") Long id) {
        officialDocReceiveService.cancelOfficialDocReceive(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/claim")
    @Operation(summary = "签收公文收文")
    @Parameter(name = "id", description = "收文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:update')")
    public CommonResult<Boolean> claimOfficialDocReceive(@RequestParam("id") Long id) {
        officialDocReceiveService.claimOfficialDocReceive(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得公文收文分页列表")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:query')")
    public CommonResult<PageResult<OaOfficialDocReceiveRespVO>> getOfficialDocReceivePage(
            @Valid OaOfficialDocReceivePageReqVO pageReqVO) {
        PageResult<OaOfficialDocReceiveDO> pageResult = officialDocReceiveService.getOfficialDocReceivePage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildOfficialDocReceiveRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得公文收文详情")
    @Parameter(name = "id", description = "收文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-receive:query')")
    public CommonResult<OaOfficialDocReceiveRespVO> getOfficialDocReceive(@RequestParam("id") Long id) {
        OaOfficialDocReceiveDO receive = officialDocReceiveService.getOfficialDocReceive(id);
        return success(buildOfficialDocReceiveRespVO(receive));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接收文详情及关联发文信息
     *
     * @param receive 收文
     * @return 收文详情
     */
    private OaOfficialDocReceiveRespVO buildOfficialDocReceiveRespVO(OaOfficialDocReceiveDO receive) {
        if (receive == null) {
            return null;
        }
        return CollUtil.getFirst(buildOfficialDocReceiveRespVOList(Collections.singletonList(receive)));
    }

    /**
     * 拼接公文收文的用户、部门名称及关联发文信息
     *
     * @param receives 公文收文列表
     * @return 公文收文响应列表
     */
    private List<OaOfficialDocReceiveRespVO> buildOfficialDocReceiveRespVOList(List<OaOfficialDocReceiveDO> receives) {
        if (CollUtil.isEmpty(receives)) {
            return Collections.emptyList();
        }
        // 1.1 批量查询关联发文，手工收文无需查询
        Map<Long, OaOfficialDocSendDO> sendMap = officialDocSendService.getOfficialDocSendMap(
                convertSet(receives, OaOfficialDocReceiveDO::getSendId));
        // 1.2 批量查询主办人及签发人
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSetByFlatMap(receives, receive -> {
            OaOfficialDocSendDO send = sendMap.get(receive.getSendId());
            return Stream.of(receive.getHandlerUserId(), send != null ? send.getSignerUserId() : null);
        }));
        // 1.3 批量查询收发文部门
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSetByFlatMap(receives, receive -> {
            OaOfficialDocSendDO send = sendMap.get(receive.getSendId());
            return Stream.of(receive.getReceiveDeptId(), send != null ? send.getSendDeptId() : null);
        }));
        // TODO DONE @AI：移除重复解释实现方式的注释，OA 同类说明同步精简。
        // 2. 转换并补充展示字段
        return convertList(receives, receive -> {
            OaOfficialDocReceiveRespVO respVO = BeanUtils.toBean(receive, OaOfficialDocReceiveRespVO.class);
            MapUtils.findAndThen(userMap, receive.getHandlerUserId(), user -> respVO.setHandlerName(user.getNickname()));
            MapUtils.findAndThen(deptMap, receive.getReceiveDeptId(), dept -> respVO.setReceiveDeptName(dept.getName()));
            MapUtils.findAndThen(sendMap, receive.getSendId(), send -> {
                MapUtils.findAndThen(deptMap, send.getSendDeptId(), dept -> respVO.setSendDeptName(dept.getName()));
                MapUtils.findAndThen(userMap, send.getSignerUserId(), user -> respVO.setSignerName(user.getNickname()));
                respVO.setIssueTime(send.getIssueTime()).setDisclosureType(send.getDisclosureType());
            });
            return respVO;
        });
    }

}
