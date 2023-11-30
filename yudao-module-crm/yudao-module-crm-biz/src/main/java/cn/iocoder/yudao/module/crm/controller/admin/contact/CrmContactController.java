package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertListByFlatMap;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

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
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody CrmContactCreateReqVO createReqVO) {
        return success(contactService.createContact(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody CrmContactUpdateReqVO updateReqVO) {
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
        if (contact == null) {
            throw exception(ErrorCodeConstants.CONTACT_NOT_EXISTS);
        }
        // 1. 获取用户名
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(
                NumberUtil.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 2. 获取客户信息
        List<CrmCustomerDO> customerList = customerService.getCustomerList(Collections.singletonList(contact.getCustomerId()));
        // 3. 直属上级
        List<CrmContactDO> parentContactList = contactService.getContactList(Collections.singletonList(contact.getParentId()));
        return success(ContactConvert.INSTANCE.convert(contact, userMap, customerList, parentContactList));
    }

    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<CrmContactSimpleRespVO>> getSimpleContactList() {
        List<CrmContactDO> list = contactService.getContactList();
        return success(ContactConvert.INSTANCE.convertAllList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<CrmContactRespVO>> getContactPage(@Valid CrmContactPageReqVO pageVO) {
        PageResult<CrmContactDO> pageResult = contactService.getContactPage(pageVO);
        return success(convertDetailContactPage(pageResult));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得联系人分页，基于指定客户")
    public CommonResult<PageResult<CrmContactRespVO>> getContactPageByCustomer(@Valid CrmContactPageReqVO pageVO) {
        Assert.notNull(pageVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmContactDO> pageResult = contactService.getContactPageByCustomer(pageVO);
        return success(convertDetailContactPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid CrmContactPageReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        exportReqVO.setPageNo(PageParam.PAGE_SIZE_NONE);
        PageResult<CrmContactDO> pageResult = contactService.getContactPage(exportReqVO);
        ExcelUtils.write(response, "联系人.xls", "数据", CrmContactRespVO.class,
                convertDetailContactPage(pageResult).getList());
    }

    /**
     * 转换成详细的联系人分页，即读取关联信息
     *
     * @param pageResult 联系人分页
     * @return 详细的联系人分页
     */
    private PageResult<CrmContactRespVO> convertDetailContactPage(PageResult<CrmContactDO> pageResult) {
        List<CrmContactDO> contactList = pageResult.getList();
        if (CollUtil.isEmpty(contactList)) {
            return PageResult.empty(pageResult.getTotal());
        }
        // 1. 获取客户列表
        List<CrmCustomerDO> crmCustomerDOList = customerService.getCustomerList(
                convertSet(contactList, CrmContactDO::getCustomerId));
        // 2. 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(contactList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 3. 直属上级
        List<CrmContactDO> parentContactList = contactService.getContactList(
                convertSet(contactList, CrmContactDO::getParentId));
        return ContactConvert.INSTANCE.convertPage(pageResult, userMap, crmCustomerDOList, parentContactList);
    }

}
