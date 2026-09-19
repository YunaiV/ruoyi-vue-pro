package cn.iocoder.yudao.module.oa.controller.admin.officialdoc;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.OaOfficialDocSendPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.OaOfficialDocSendRespVO;
import cn.iocoder.yudao.module.oa.controller.admin.officialdoc.vo.send.OaOfficialDocSendSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc.OaOfficialDocSendDO;
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
import java.util.Set;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 公文发文")
@RestController
@RequestMapping("/oa/officialdoc-send")
@Validated
public class OaOfficialDocSendController {

    @Resource
    private OaOfficialDocSendService officialDocSendService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建公文发文")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:create')")
    public CommonResult<Long> createOfficialDocSend(@Valid @RequestBody OaOfficialDocSendSaveReqVO reqVO) {
        return success(officialDocSendService.createOfficialDocSend(reqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新公文发文")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:update')")
    public CommonResult<Boolean> updateOfficialDocSend(@Valid @RequestBody OaOfficialDocSendSaveReqVO reqVO) {
        officialDocSendService.updateOfficialDocSend(reqVO, getLoginUserId());
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除公文发文")
    @Parameter(name = "id", description = "发文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:delete')")
    public CommonResult<Boolean> deleteOfficialDocSend(@RequestParam("id") Long id) {
        officialDocSendService.deleteOfficialDocSend(id, getLoginUserId());
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交公文发文审批")
    @Parameter(name = "id", description = "发文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:update')")
    public CommonResult<Boolean> submitOfficialDocSend(@RequestParam("id") Long id) {
        officialDocSendService.submitOfficialDocSend(id, getLoginUserId());
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "撤销公文发文审批")
    @Parameter(name = "id", description = "发文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:update')")
    public CommonResult<Boolean> cancelOfficialDocSend(@RequestParam("id") Long id) {
        officialDocSendService.cancelOfficialDocSend(id, getLoginUserId());
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得公文发文分页列表")
    @PreAuthorize("@ss.hasPermission('oa:officialdoc-send:query')")
    public CommonResult<PageResult<OaOfficialDocSendRespVO>> getOfficialDocSendPage(
            @Valid OaOfficialDocSendPageReqVO pageReqVO) {
        PageResult<OaOfficialDocSendDO> pageResult = officialDocSendService.getOfficialDocSendPage(pageReqVO, getLoginUserId());
        return success(new PageResult<>(buildOfficialDocSendRespVOList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/get")
    @Operation(summary = "获得公文发文详情")
    @Parameter(name = "id", description = "发文编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasAnyPermissions('oa:officialdoc-send:query', 'oa:officialdoc-receive:query')")
    public CommonResult<OaOfficialDocSendRespVO> getOfficialDocSend(@RequestParam("id") Long id) {
        OaOfficialDocSendDO send = officialDocSendService.getOfficialDocSend(id);
        return success(buildOfficialDocSendRespVO(send));
    }

    // ==================== 拼接 VO ====================

    /**
     * 拼接公文发文详情
     *
     * @param send 公文发文
     * @return 公文发文响应
     */
    private OaOfficialDocSendRespVO buildOfficialDocSendRespVO(OaOfficialDocSendDO send) {
        if (send == null) {
            return null;
        }
        return CollUtil.getFirst(buildOfficialDocSendRespVOList(Collections.singletonList(send)));
    }

    /**
     * 拼接公文发文的用户、部门名称
     *
     * @param sends 公文发文列表
     * @return 公文发文响应列表
     */
    private List<OaOfficialDocSendRespVO> buildOfficialDocSendRespVOList(List<OaOfficialDocSendDO> sends) {
        if (CollUtil.isEmpty(sends)) {
            return Collections.emptyList();
        }
        // 1.1 收集发文、主送和抄送部门编号
        Set<Long> deptIds = convertSet(sends, OaOfficialDocSendDO::getSendDeptId);
        sends.forEach(send -> {
            if (CollUtil.isNotEmpty(send.getMainDeptIds())) {
                deptIds.addAll(send.getMainDeptIds());
            }
            if (CollUtil.isNotEmpty(send.getCopyDeptIds())) {
                deptIds.addAll(send.getCopyDeptIds());
            }
        });
        // 1.2 批量查询签发人和部门
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertSet(sends, OaOfficialDocSendDO::getSignerUserId));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(deptIds);

        // 2. 转换并补充展示字段
        return convertList(sends, send -> {
            OaOfficialDocSendRespVO respVO = BeanUtils.toBean(send, OaOfficialDocSendRespVO.class);
            MapUtils.findAndThen(userMap, send.getSignerUserId(), user -> respVO.setSignerName(user.getNickname()));
            MapUtils.findAndThen(deptMap, send.getSendDeptId(), dept -> respVO.setSendDeptName(dept.getName()));
            respVO.setMainDeptNames(convertList(send.getMainDeptIds(), id -> {
                DeptRespDTO dept = deptMap.get(id);
                return dept != null ? dept.getName() : null;
            }));
            respVO.setCopyDeptNames(convertList(send.getCopyDeptIds(), id -> {
                DeptRespDTO dept = deptMap.get(id);
                return dept != null ? dept.getName() : null;
            }));
            return respVO;
        });
    }

}
