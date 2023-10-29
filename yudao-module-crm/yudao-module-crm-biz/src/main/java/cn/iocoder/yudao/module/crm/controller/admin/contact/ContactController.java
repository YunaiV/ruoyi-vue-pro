package cn.iocoder.yudao.module.crm.controller.admin.contact;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contact.vo.*;
import cn.iocoder.yudao.module.crm.convert.contact.ContactConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.ContactDO;
import cn.iocoder.yudao.module.crm.service.contact.ContactService;
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
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - crm联系人")
@RestController
@RequestMapping("/crm/contact")
@Validated
public class ContactController {

    @Resource
    private ContactService contactService;

    @PostMapping("/create")
    @Operation(summary = "创建crm联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody ContactCreateReqVO createReqVO) {
        return success(contactService.createContact(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新crm联系人")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody ContactUpdateReqVO updateReqVO) {
        contactService.updateContact(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除crm联系人")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contact:delete')")
    public CommonResult<Boolean> deleteContact(@RequestParam("id") Long id) {
        contactService.deleteContact(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得crm联系人")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<ContactRespVO> getContact(@RequestParam("id") Long id) {
        ContactDO contact = contactService.getContact(id);
        return success(ContactConvert.INSTANCE.convert(contact));
    }

    @GetMapping("/list")
    @Operation(summary = "获得crm联系人列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<List<ContactRespVO>> getContactList(@RequestParam("ids") Collection<Long> ids) {
        List<ContactDO> list = contactService.getContactList(ids);
        return success(ContactConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得crm联系人分页")
    @PreAuthorize("@ss.hasPermission('crm:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageVO) {
        PageResult<ContactDO> pageResult = contactService.getContactPage(pageVO);
        return success(ContactConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出crm联系人 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact:export')")
    @OperateLog(type = EXPORT)
    public void exportContactExcel(@Valid ContactExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<ContactDO> list = contactService.getContactList(exportReqVO);
        // 导出 Excel
        List<ContactExcelVO> datas = ContactConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "crm联系人.xls", "数据", ContactExcelVO.class, datas);
    }

    @PutMapping("/transfer")
    @Operation(summary = "联系人转移")
    @PreAuthorize("@ss.hasPermission('crm:contact:update')")
    public CommonResult<Boolean> transfer(@Valid @RequestBody CrmContactTransferReqVO reqVO) {
        contactService.contactTransfer(reqVO, getLoginUserId());
        return success(true);
    }

}
