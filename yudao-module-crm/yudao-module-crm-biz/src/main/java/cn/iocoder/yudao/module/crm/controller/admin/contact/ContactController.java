package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.crm.service.contact.ContactService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
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
import java.util.*;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO @zya：crm 所有的类，dou带 Crm 前缀，因为它的名字太通用了，可能和后续的 erp 之类的冲突
@Tag(name = "管理后台 - CRM 联系人")
@RestController
@RequestMapping("/crm/contact")
@Validated
@Slf4j
public class ContactController {

    @Resource
    private ContactService contactService;
    @Resource
    private CrmCustomerService crmCustomerService;

    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody ContactCreateReqVO createReqVO) {
        return success(contactService.createContact(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody ContactUpdateReqVO updateReqVO) {
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
    public CommonResult<ContactRespVO> getContact(@RequestParam("id") Long id) {
        ContactDO contact = contactService.getContact(id);
        if (contact == null) {
            throw exception(ErrorCodeConstants.CONTACT_NOT_EXISTS);
        }
        // 1. 获取用户名
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(
                NumberUtil.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 2. 获取客户信息
        List<CrmCustomerDO> customerList = crmCustomerService.getCustomerList(Collections.singletonList(contact.getCustomerId()));
        // 3. 直属上级
        List<ContactDO> parentContactList = contactService.getContactList(Collections.singletonList(contact.getParentId()));
        return success(ContactConvert.INSTANCE.convert(contact, userMap, customerList, parentContactList));
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactSimpleRespVO>> getContactList() {
        List<ContactDO> list = contactService.getContactList();
        return success(ContactConvert.INSTANCE.convertAllList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageVO) {
        PageResult<ContactDO> pageResult = contactService.getContactPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }
        return success(convertFieldValue2Name(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid ContactPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageNo(PageParam.PAGE_SIZE_NONE);
        PageResult<ContactDO> pageResult = contactService.getContactPage(exportReqVO);
        PageResult<ContactRespVO> exportPage = convertFieldValue2Name(pageResult);
        ExcelUtils.write(response, "crm 联系人.xls", "数据", ContactRespVO.class, exportPage.getList());
    }

    // TODO 芋艿：后续会合并下，

    /**
     * 翻译字段名称
     *
     * @param pageResult 联系人分页参数
     * @return List<ContactRespVO>
     */
    private PageResult<ContactRespVO> convertFieldValue2Name(PageResult<ContactDO> pageResult) {
        List<ContactDO> contactDOList = pageResult.getList();
        // 1. 获取客户列表
        List<CrmCustomerDO> crmCustomerDOList = crmCustomerService.getCustomerList(convertSet(contactDOList, ContactDO::getCustomerId));
        // 2. 获取创建人、责任人列表
        List<Long> userIdsList = convertListByFlatMap(contactDOList, item -> Stream.of(NumberUtils.parseLong(item.getCreator()), item.getOwnerUserId())
                .filter(Objects::nonNull));
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIdsList);
        // 3. 直属上级
        Set<Long> contactIdsList = convertSet(contactDOList, ContactDO::getParentId);
        List<ContactDO> contactList = contactService.getContactList(contactIdsList);
        return ContactConvert.INSTANCE.convertPage(pageResult, userMap, crmCustomerDOList, contactList);
    }

}
