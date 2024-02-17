package cn.iocoder.yudao.module.crm.controller.admin.backlog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.backlog.vo.CrmTodayCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.service.message.CrmBacklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - CRM待办消息")
@RestController
@RequestMapping("/crm/backlog")
@Validated
public class CrmBacklogController {

    @Resource
    private CrmBacklogService crmMessageService;

    // TODO 芋艿：未来可能合并到 CrmCustomerController
    @GetMapping("/today-customer-page")
    @Operation(summary = "今日需联系客户")
    @PreAuthorize("@ss.hasPermission('crm:customer:query')")
    public CommonResult<PageResult<CrmCustomerRespVO>> getTodayCustomerPage(@Valid CrmTodayCustomerPageReqVO pageReqVO) {
        PageResult<CrmCustomerDO> pageResult = crmMessageService.getTodayCustomerPage(pageReqVO, getLoginUserId());
        return success(BeanUtils.toBean(pageResult, CrmCustomerRespVO.class));
    }

}