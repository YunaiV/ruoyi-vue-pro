package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.apilog.core.annotation.ApiAccessLog;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - CRM 联系人")
@RestController
@RequestMapping("/crm/contact")
@Validated
@Slf4j
public class CrmContactController {

    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmContactBusinessService contactBusinessLinkService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @PostMapping("/create")
    @Operation(summary = "创建联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody CrmContactSaveReqVO createReqVO) {
        return success(contactService.createContact(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody CrmContactSaveReqVO updateReqVO) {
        contactService.updateContact(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除联系人")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contact:delete')")
    public CommonResult<Boolean> deleteContact(@RequestParam("id") Long id) {
        contactService.deleteContact(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得联系人")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<CrmContactRespVO> getContact(@RequestParam("id") Long id) {
        CrmContactDO contact = contactService.getContact(id);
        return success(buildContactDetail(contact));
    }

    private CrmContactRespVO buildContactDetail(CrmContactDO contact) {
        if (contact == null) {
            return null;
        }
        return buildContactDetailList(singletonList(contact)).get(0);
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人的精简列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<CrmContactRespVO>> getSimpleContactList() {
        List<CrmContactDO> list = contactService.getContactList(getLoginUserId());
        return success(convertList(list, contact -> // 只返回 id、name 字段
                new CrmContactRespVO().setId(contact.getId()).setName(contact.getName())
                        .setCustomerId(contact.getCustomerId())));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<CrmContactRespVO>> getContactPage(@Valid CrmContactPageReqVO pageVO) {
        PageResult<CrmContactDO> pageResult = contactService.getContactPage(pageVO, getLoginUserId());
        return success(new PageResult<>(buildContactDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得联系人分页，基于指定客户")
    public CommonResult<PageResult<CrmContactRespVO>> getContactPageByCustomer(@Valid CrmContactPageReqVO pageVO) {
        Assert.notNull(pageVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmContactDO> pageResult = contactService.getContactPageByCustomerId(pageVO);
        return success(new PageResult<>(buildContactDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/page-by-business")
    @Operation(summary = "获得联系人分页，基于指定商机")
    public CommonResult<PageResult<CrmContactRespVO>> getContactPageByBusiness(@Valid CrmContactPageReqVO pageVO) {
        Assert.notNull(pageVO.getBusinessId(), "商机编号不能为空");
        PageResult<CrmContactDO> pageResult = contactService.getContactPageByBusinessId(pageVO);
        return success(new PageResult<>(buildContactDetailList(pageResult.getList()), pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportContactExcel(@Valid CrmContactPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageNo(PAGE_SIZE_NONE);
        List<CrmContactDO> list = contactService.getContactPage(exportReqVO, getLoginUserId()).getList();
        ExcelUtils.write(response, "联系人.xls", "数据", CrmContactRespVO.class, buildContactDetailList(list));
    }

    private List<CrmContactRespVO> buildContactDetailList(List<CrmContactDO> contactList) {
        if (CollUtil.isEmpty(contactList)) {
            return Collections.emptyList();
        }
        // 1.1 获取客户列表
        Map<Long, CrmCustomerDO> customerMap = customerService.getCustomerMap(
                convertSet(contactList, CrmContactDO::getCustomerId));
        // 1.2 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(contactList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(convertSet(userMap.values(), AdminUserRespDTO::getDeptId));
        // 1.3 直属上级 Map
        Map<Long, CrmContactDO> parentContactMap = contactService.getContactMap(
                convertSet(contactList, CrmContactDO::getParentId));
        // 2. 转换成 VO
        return BeanUtils.toBean(contactList, CrmContactRespVO.class, contactVO -> {
            contactVO.setAreaName(AreaUtils.format(contactVO.getAreaId()));
            // 2.1 设置客户名称
            MapUtils.findAndThen(customerMap, contactVO.getCustomerId(), customer -> contactVO.setCustomerName(customer.getName()));
            // 2.2 设置创建人、负责人名称
            MapUtils.findAndThen(userMap, NumberUtils.parseLong(contactVO.getCreator()),
                    user -> contactVO.setCreatorName(user.getNickname()));
            MapUtils.findAndThen(userMap, contactVO.getOwnerUserId(), user -> {
                contactVO.setOwnerUserName(user.getNickname());
                MapUtils.findAndThen(deptMap, user.getDeptId(), dept -> contactVO.setOwnerUserDeptName(dept.getName()));
            });
            // 2.3 设置直属上级名称
            findAndThen(parentContactMap, contactVO.getParentId(), contact -> contactVO.setParentName(contact.getName()));
        });
    }

    @PutMapping("/transfer")
    @Operation(summary = "联系人转移")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> transferContact(@Valid @RequestBody CrmContactTransferReqVO reqVO) {
        contactService.transferContact(reqVO, getLoginUserId());
        return success(true);
    }

    // ================== 关联/取关商机 ===================

    @PostMapping("/create-business-list")
    @Operation(summary = "创建联系人与商机的关联")
    @PreAuthorize("@ss.hasPermission('crm:contact:create-business')")
    public CommonResult<Boolean> createContactBusinessList(@Valid @RequestBody CrmContactBusinessReqVO createReqVO) {
        contactBusinessLinkService.createContactBusinessList(createReqVO);
        return success(true);
    }


    @PostMapping("/create-business-list2")
    @Operation(summary = "创建联系人与商机的关联")
    @PreAuthorize("@ss.hasPermission('crm:contact:create-business')")
    public CommonResult<Boolean> createContactBusinessList2(@Valid @RequestBody CrmContactBusiness2ReqVO createReqVO) {
        contactBusinessLinkService.createContactBusinessList2(createReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-business-list")
    @Operation(summary = "删除联系人与联系人的关联")
    @PreAuthorize("@ss.hasPermission('crm:contact:delete-business')")
    public CommonResult<Boolean> deleteContactBusinessList(@Valid @RequestBody CrmContactBusinessReqVO deleteReqVO) {
        contactBusinessLinkService.deleteContactBusinessList(deleteReqVO);
        return success(true);
    }

    @DeleteMapping("/delete-business-list2")
    @Operation(summary = "删除联系人与联系人的关联")
    @PreAuthorize("@ss.hasPermission('crm:contact:delete-business')")
    public CommonResult<Boolean> deleteContactBusinessList(@Valid @RequestBody CrmContactBusiness2ReqVO deleteReqVO) {
        contactBusinessLinkService.deleteContactBusinessList2(deleteReqVO);
        return success(true);
    }

}
