package cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink;

import cn.iocoder.yudao.module.crm.controller.admin.business.vo.business.CrmBusinessRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import org.springframework.transaction.annotation.Transactional;
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

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;

import cn.iocoder.yudao.module.crm.controller.admin.contactbusinesslink.vo.*;
import cn.iocoder.yudao.module.crm.dal.dataobject.contactbusinesslink.CrmContactBusinessLinkDO;
import cn.iocoder.yudao.module.crm.service.contactbusinesslink.CrmContactBusinessLinkService;

@Tag(name = "管理后台 - 联系人商机关联")
@RestController
@RequestMapping("/crm/contact-business-link")
@Validated
public class CrmContactBusinessLinkController {

    @Resource
    private CrmContactBusinessLinkService contactBusinessLinkService;
    @Resource
    private CrmBusinessService crmBusinessService;

    @PostMapping("/create")
    @Operation(summary = "创建联系人商机关联")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:create')")
    public CommonResult<Long> createContactBusinessLink(@Valid @RequestBody CrmContactBusinessLinkSaveReqVO createReqVO) {
        return success(contactBusinessLinkService.createContactBusinessLink(createReqVO));
    }
    @PostMapping("/create-batch")
    @Operation(summary = "创建联系人商机关联")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:create')")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Boolean> createContactBusinessLinkBatch(@Valid @RequestBody List<CrmContactBusinessLinkSaveReqVO> createReqVO) {
        createReqVO.stream().forEach(item -> {
            CrmBusinessDO crmBusinessDO = crmBusinessService.getBusiness(item.getBusinessId());
            if(crmBusinessDO == null){
                throw exception(BUSINESS_NOT_EXISTS);
            }
        });
        contactBusinessLinkService.createContactBusinessLinkBatch(createReqVO);
        return success(true);
    }
    @PutMapping("/update")
    @Operation(summary = "更新联系人商机关联")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:update')")
    public CommonResult<Boolean> updateContactBusinessLink(@Valid @RequestBody CrmContactBusinessLinkSaveReqVO updateReqVO) {
        contactBusinessLinkService.updateContactBusinessLink(updateReqVO);
        return success(true);
    }
    @DeleteMapping("/delete-batch")
    @Operation(summary = "批量删除联系人商机关联")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:delete')")
    public CommonResult<Boolean> deleteContactBusinessLinkBatch(@Valid @RequestBody List<CrmContactBusinessLinkSaveReqVO> deleteList) {
        contactBusinessLinkService.deleteContactBusinessLink(deleteList);
        return success(true);
    }
    @GetMapping("/get")
    @Operation(summary = "获得联系人商机关联")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:query')")
    public CommonResult<CrmContactBusinessLinkRespVO> getContactBusinessLink(@RequestParam("id") Long id) {
        CrmContactBusinessLinkDO contactBusinessLink = contactBusinessLinkService.getContactBusinessLink(id);
        return success(BeanUtils.toBean(contactBusinessLink, CrmContactBusinessLinkRespVO.class));
    }
    @GetMapping("/page-by-contact")
    @Operation(summary = "获得联系人商机关联")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:query')")
    public CommonResult<PageResult<CrmBusinessRespVO>> getContactBusinessLinkByContact(@Valid CrmContactBusinessLinkPageReqVO pageReqVO) {
        PageResult<CrmBusinessRespVO> contactBusinessLink = contactBusinessLinkService.getContactBusinessLinkPageByContact(pageReqVO);
        return success(contactBusinessLink);
    }
    @GetMapping("/page")
    @Operation(summary = "获得联系人商机关联分页")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:query')")
    public CommonResult<PageResult<CrmContactBusinessLinkRespVO>> getContactBusinessLinkPage(@Valid CrmContactBusinessLinkPageReqVO pageReqVO) {
        PageResult<CrmContactBusinessLinkDO> pageResult = contactBusinessLinkService.getContactBusinessLinkPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CrmContactBusinessLinkRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人商机关联 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contact-business-link:export')")
    @OperateLog(type = EXPORT)
    public void exportContactBusinessLinkExcel(@Valid CrmContactBusinessLinkPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CrmContactBusinessLinkDO> list = contactBusinessLinkService.getContactBusinessLinkPage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "联系人商机关联.xls", "数据", CrmContactBusinessLinkRespVO.class,
                        BeanUtils.toBean(list, CrmContactBusinessLinkRespVO.class));
    }

}