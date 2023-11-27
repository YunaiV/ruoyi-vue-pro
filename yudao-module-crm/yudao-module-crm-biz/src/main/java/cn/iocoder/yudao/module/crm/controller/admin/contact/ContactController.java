package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.contact.ContactService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

// TODO @zya：crm 所有的类，dou带 Crm 前缀，因为它的名字太通用了，可能和后续的 erp 之类的冲突
@Tag(name = "管理后台 - CRM 联系人")
@RestController
@RequestMapping("/crm/contact")
@Validated
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
        // TODO @zyna：需要考虑 null 的情况；
        ContactRespVO contactRespVO = ContactConvert.INSTANCE.convert(contact);
        // TODO @zyna：可以把数据读完后，convert 统一交给 ContactConvert，让 controller 更简洁；而 convert 专门去做一些转换逻辑
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(CollUtil.removeNull(Lists.newArrayList(
                NumberUtil.parseLong(contact.getCreator()))));
        contactRespVO.setCreatorName(Optional.ofNullable(userMap.get(NumberUtil.parseLong(contact.getCreator()))).map(AdminUserRespDTO::getNickname).orElse(null));
        contactRespVO.setCustomerName(Optional.ofNullable(crmCustomerService.getCustomer(contact.getCustomerId())).map(CrmCustomerDO::getName).orElse(null));
        return success(contactRespVO);
    }

    // TODO @zyna：url 使用中划线噢；然后，单词的拼写也要注意呀，AllList 是不是更好呀；
    @GetMapping("/simpleAlllist")
    @Operation(summary = "获得联系人列表")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactSimpleRespVO>> simpleAlllist() {
        // TODO @zyna：方法名改成，getContactList；方法命名，要动名词，get 动词；all 可以去掉，因为没条件，自然是全部
        List<ContactDO> list = contactService.allContactList();
        return success(ContactConvert.INSTANCE.convertAllList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageVO) {
        PageResult<ContactDO> pageData = contactService.getContactPage(pageVO);
        PageResult<ContactRespVO> pageResult = ContactConvert.INSTANCE.convertPage(pageData);
        // TODO @zyna：需要考虑 null 的情况；
        // TODO @zyna：可以把数据读完后，convert 统一交给 ContactConvert，让 controller 更简洁；而 convert 专门去做一些转换逻辑
        //待接口实现后修改
        CrmCustomerPageReqVO reqVO = new CrmCustomerPageReqVO();
        reqVO.setPageSize(PAGE_SIZE_NONE);
        List<CrmCustomerDO> crmCustomerDOList = crmCustomerService.getCustomerPage(reqVO, getLoginUserId()).getList();
        Map<Long, CrmCustomerDO> crmCustomerDOMap = crmCustomerDOList.stream().collect(Collectors.toMap(CrmCustomerDO::getId, v -> v));
        pageResult.getList().forEach(item -> {
            item.setCustomerName(Optional.ofNullable(crmCustomerDOMap.get(item.getCustomerId())).map(CrmCustomerDO::getName).orElse(null));
        });
        return success(pageResult);
    }

    // TODO @zyna：可以看下新的导出写法，这里调整下
    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid ContactExportReqVO exportReqVO,
                                   HttpServletResponse response) throws IOException {
        List<ContactDO> list = contactService.getContactList(exportReqVO);
        // 导出 Excel
        List<ContactExcelVO> datas = ContactConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "crm联系人.xls", "数据", ContactExcelVO.class, datas);
    }

}
