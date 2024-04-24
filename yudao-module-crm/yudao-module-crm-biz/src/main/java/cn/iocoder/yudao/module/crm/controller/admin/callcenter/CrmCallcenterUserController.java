package cn.iocoder.yudao.module.crm.controller.admin.callcenter;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import cn.iocoder.yudao.module.crm.service.callcenter.CrmCallcenterUserService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import javax.validation.constraints.*;
import javax.validation.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.IOException;
import java.util.stream.Stream;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;


@Tag(name = "管理后台 - 用户与呼叫中心用户绑定关系")
@RestController
@RequestMapping("/crm/callcenter-user")
@Validated
public class CrmCallcenterUserController {

    @Resource
    private CrmCallcenterUserService callcenterUserService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建用户与呼叫中心用户绑定关系")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<Long> createCallcenterUser(@Valid @RequestBody CrmCallcenterUserSaveReqVO createReqVO) {
        return success(callcenterUserService.createCallcenterUser(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新用户与呼叫中心用户绑定关系")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<Boolean> updateCallcenterUser(@Valid @RequestBody CrmCallcenterUserSaveReqVO updateReqVO) {
        callcenterUserService.updateCallcenterUser(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除用户与呼叫中心用户绑定关系")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<Boolean> deleteCallcenterUser(@RequestParam("id") Long id) {
        callcenterUserService.deleteCallcenterUser(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得用户与呼叫中心用户绑定关系")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<CrmCallcenterUserRespVO> getCallcenterUser(@RequestParam("id") Long id) {
        CrmCallcenterUserDO callcenterUser = callcenterUserService.getCallcenterUser(id);
        return success(BeanUtils.toBean(callcenterUser, CrmCallcenterUserRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得用户与呼叫中心用户绑定关系分页")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<PageResult<CrmCallcenterUserRespVO>> getCallcenterUserPage(@Valid CrmCallcenterUserPageReqVO pageReqVO) {
        PageResult<CrmCallcenterUserDO> pageResult = callcenterUserService.getCallcenterUserPage(pageReqVO);
        return success(new PageResult<>(buildCallcenterUserList(pageResult.getList()), pageResult.getTotal()));
    }

    private List<CrmCallcenterUserRespVO> buildCallcenterUserList(List<CrmCallcenterUserDO> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        // 1 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(list, contact -> Stream.of(contact.getUserId())));
        return BeanUtils.toBean(list, CrmCallcenterUserRespVO.class, userRespVO -> {
            // 2 设置用户信息
            MapUtils.findAndThen(userMap, userRespVO.getUserId(), user -> userRespVO.setNickName(user.getNickname()));
        });
    }

    @GetMapping("/bingding-user")
    @Operation(summary = "获得用户与呼叫中心用户绑定关系分页")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user')")
    public CommonResult<PageResult<CrmCallcenterUserRespVO>> getListCallcenterUser() {
        PageResult<AdminUserRespDTO> userPage = adminUserApi.getUserPage();
        List<CrmCallcenterUserDO> list = callcenterUserService.getList();
        Map<String,CrmCallcenterUserDO> map = new HashMap<>();
        for (CrmCallcenterUserDO crmCallcenterUserDO : list) {
            map.put(crmCallcenterUserDO.getUserId().toString(),crmCallcenterUserDO);
        }
        PageResult<CrmCallcenterUserRespVO> pageResult = new PageResult<>();
        pageResult.setList(new ArrayList<>());
        //构建返回结果
        userPage.getList().forEach(adminUserRespDTO -> {
            CrmCallcenterUserRespVO crmCallcenterUserRespVO = new CrmCallcenterUserRespVO();
            crmCallcenterUserRespVO.setUserId(adminUserRespDTO.getId());
            crmCallcenterUserRespVO.setNickName(adminUserRespDTO.getNickname());
            CrmCallcenterUserDO crmCallcenterUserDO = map.get(adminUserRespDTO.getId().toString());
            if (crmCallcenterUserDO != null) {
                crmCallcenterUserRespVO.setId(crmCallcenterUserDO.getId());
                crmCallcenterUserRespVO.setYunkeCallcenterUserId(crmCallcenterUserDO.getYunkeCallcenterUserId());
                crmCallcenterUserRespVO.setYunkeCallcenterPhone(crmCallcenterUserDO.getYunkeCallcenterPhone());
                crmCallcenterUserRespVO.setCreateTime(crmCallcenterUserDO.getCreateTime());
                pageResult.getList().add(crmCallcenterUserRespVO);
            } else {
                CrmCallcenterUserSaveReqVO newCrmCallcenterUserDo = new CrmCallcenterUserSaveReqVO();
                newCrmCallcenterUserDo.setUserId(adminUserRespDTO.getId());
                Long userid = callcenterUserService.createCallcenterUser(newCrmCallcenterUserDo);
                CrmCallcenterUserDO ccu = callcenterUserService.getCallcenterUser(userid);
                if (ccu != null){
                    crmCallcenterUserRespVO.setId(ccu.getId());
                    crmCallcenterUserRespVO.setUserId(ccu.getUserId());
                    pageResult.getList().add(crmCallcenterUserRespVO);
                }
            }
        });
        return success(pageResult);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出用户与呼叫中心用户绑定关系 Excel")
    @PreAuthorize("@ss.hasPermission('crm:callcenter:user-export')")
    @OperateLog(type = EXPORT)
    public void exportCallcenterUserExcel(@Valid CrmCallcenterUserPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CrmCallcenterUserDO> list = callcenterUserService.getCallcenterUserPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "用户与呼叫中心用户绑定关系.xls", "数据", CrmCallcenterUserRespVO.class,
                        BeanUtils.toBean(list, CrmCallcenterUserRespVO.class));
    }

}