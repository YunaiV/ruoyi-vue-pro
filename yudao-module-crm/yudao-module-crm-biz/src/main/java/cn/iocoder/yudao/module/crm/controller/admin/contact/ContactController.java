package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
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
    // TODO @zyna：模块内，注入的变量，不用带 crm 前缀哈
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
        if(contact == null){
            throw exception(ErrorCodeConstants.CONTACT_NOT_EXISTS);
        }
        //1.获取用户名
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(
                NumberUtil.parseLong(contact.getCreator()),contact.getOwnerUserId())));
        //2.获取客户信息
        List<CrmCustomerDO> crmCustomerDOList = crmCustomerService.getCustomerList(Collections.singletonList(contact.getCustomerId()));
        //3.直属上级
        List<ContactDO> contactList = contactService.getContactList(Collections.singletonList(contact.getParentId()));
        ContactRespVO contactRespVO  = ContactConvert.INSTANCE.convert(contact,userMap,crmCustomerDOList,contactList);
        return success(contactRespVO);
    }
    @GetMapping("/simple-all-list")
    @Operation(summary = "获得联系人列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactSimpleRespVO>> simpleAlllist() {
        // TODO @zyna：方法名改成，getContactList；方法命名，要动名词，get 动词；all 可以去掉，因为没条件，自然是全部
        List<ContactDO> list = contactService.getContactList();
        return success(ContactConvert.INSTANCE.convertAllList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageVO) {
        PageResult<ContactDO> pageData = contactService.getContactPage(pageVO);
        List<ContactRespVO>  contactRespVOList = convertFieldValue2Name(pageData.getList());
        PageResult<ContactRespVO> pageDataReturn = ContactConvert.INSTANCE.convertPage(pageData);
        pageDataReturn.setList(contactRespVOList);
        return success(pageDataReturn);
    }

    // TODO @zyna：可以看下新的导出写法，这里调整下
    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid ContactPageReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ContactDO> list = contactService.getContactList(exportReqVO);
        // 导出 Excel
        List<ContactRespVO>  contactRespVOList = convertFieldValue2Name(list);
        ExcelUtils.write(response, "crm联系人.xls", "数据", ContactRespVO.class, contactRespVOList);
    }

    /**
     * 翻译字段名称
     * @param contactDOList 联系人List
     * @return List<ContactRespVO>
     */
    private List<ContactRespVO> convertFieldValue2Name(List<ContactDO> contactDOList){
        //1.获取客户列表
        List<Long> customerIdList = contactDOList.stream().map(ContactDO::getCustomerId).distinct().collect(Collectors.toList());
        List<CrmCustomerDO> crmCustomerDOList = crmCustomerService.getCustomerList(customerIdList);
        //2.获取创建人、责任人列表
        List<Long> userIdsList =  contactDOList.stream().flatMap(item-> Stream.of(Long.parseLong(item.getCreator()),item.getOwnerUserId()).distinct()).collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIdsList);
        //3.直属上级
        List<Long> contactIdsList =  contactDOList.stream().map(ContactDO::getParentId).distinct().collect(Collectors.toList());
        List<ContactDO> contactList = contactService.getContactList(contactIdsList);
        List<ContactRespVO> pageResult =ContactConvert.INSTANCE.converList(contactDOList,userMap,crmCustomerDOList,contactList);
        return pageResult;
    }
}
