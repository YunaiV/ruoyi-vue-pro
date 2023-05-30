package cn.iocoder.yudao.module.jl.controller.admin.crm;

import cn.iocoder.yudao.module.jl.convert.crm.FollowupConvert;
import cn.iocoder.yudao.module.jl.convert.crm.InstitutionConvert;
import cn.iocoder.yudao.module.jl.convert.crm.SalesleadConvert;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.*;
import cn.iocoder.yudao.module.jl.repository.CustomerRepository;
import cn.iocoder.yudao.module.jl.service.crm.FollowupService;
import cn.iocoder.yudao.module.jl.service.crm.InstitutionService;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadService;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.UserPageItemRespVO;
import cn.iocoder.yudao.module.system.convert.user.UserConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
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

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.*;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.convert.crm.CustomerConvert;
import cn.iocoder.yudao.module.jl.service.crm.CustomerService;

@Tag(name = "管理后台 - 客户")
@RestController
@RequestMapping("/jl/customer")
@Validated
public class CustomerController {

    @Resource
    private CustomerService customerService;

    @Resource
    private AdminUserService userService;

    @Resource
    private InstitutionService institutionService;

    @Resource
    private FollowupService followupService;

    @Resource
    private SalesleadService salesleadService;


    @PostMapping("/create")
    @Operation(summary = "创建客户")
    @PreAuthorize("@ss.hasPermission('jl:customer:create')")
    public CommonResult<Long> createCustomer(@Valid @RequestBody CustomerCreateReq createReqVO) {
        Long loginUserId = getLoginUserId();
        createReqVO.setSalesId(loginUserId); // 绑定当前账号作为销售人员
        return success(customerService.createCustomer(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新客户")
    @PreAuthorize("@ss.hasPermission('jl:customer:update')")
    public CommonResult<Boolean> updateCustomer(@Valid @RequestBody CustomerDto updateReqVO) {
        customerService.updateCustomer(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除客户")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:customer:delete')")
    public CommonResult<Boolean> deleteCustomer(@RequestParam("id") Long id) {
        customerService.deleteCustomer(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得客户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:customer:query')")
    public CommonResult<CustomerDto> getCustomer(@RequestParam("id") Long id) {
        CustomerDto customer = customerService.getCustomer(id);
        return success(customer);
    }

    private CustomerRespVO commonGetCustomer(CustomerDO customer) {
        if(customer == null) {
            return null;
        }

        CustomerRespVO customerRespVO = CustomerConvert.INSTANCE.convert(customer);

        // 绑定销售人员
        Long salesId = customer.getSalesId();
        AdminUserDO salesDo = userService.getUser(salesId);
        UserProfileRespVO sales = UserConvert.INSTANCE.convert03(salesDo);
        customerRespVO.setSales(sales);

//        // 绑定公司
//        Long companyId = customer.getCompanyId();
//        InstitutionDO institutionDo = institutionService.getInstitution(companyId);
//        InstitutionRespVO institution = InstitutionConvert.INSTANCE.convert(institutionDo);
//        customerRespVO.setCompany(institution);
//
//        // 绑定医院
//        Long hospitalId = customer.getHospitalId();
//        InstitutionDO hospitalDo = institutionService.getInstitution(hospitalId);
//        InstitutionRespVO hospital = InstitutionConvert.INSTANCE.convert(hospitalDo);
//        customerRespVO.setHospital(hospital);
//
//        // 绑定学校
//        Long universityId = customer.getUniversityId();
//        InstitutionDO universityDo = institutionService.getInstitution(universityId);
//        InstitutionRespVO university = InstitutionConvert.INSTANCE.convert(universityDo);
//        customerRespVO.setUniversity(university);

        // 绑定最近的跟进记录
        Long followId = customer.getLastFollowupId();
        FollowupDO followDo = followupService.getFollowup(followId);
        FollowupRespVO follow = FollowupConvert.INSTANCE.convert(followDo);
        customerRespVO.setLastFollowUp(follow);

        // 绑定最近的销售线索
        Long salesleadId = customer.getLastSalesleadId();
        SalesleadDO salesleadDo = salesleadService.getSaleslead(salesleadId);
        SalesleadRespVO saleslead = SalesleadConvert.INSTANCE.convert(salesleadDo);
        customerRespVO.setLastSalesLead(saleslead);

        return customerRespVO;
    }

    @GetMapping("/page")
    @Operation(summary = "获得客户分页")
    @PreAuthorize("@ss.hasPermission('jl:customer:query')")
    public CommonResult<PageResult<CustomerRespVO>> getCustomerPage(@Valid CustomerPageReqVO pageVO) {
//        Long loginUserId = getLoginUserId();
//        pageVO.setSalesId(loginUserId); // 制定当前账号作为销售人员，过滤掉其他人员的数据
//        // TODO 如果是管理者，就不需要过滤，需要根据岗位来区分
//        PageResult<CustomerDO> pageResult = customerService.getCustomerPage(pageVO);
//
//        PageResult<CustomerRespVO> pageData = CustomerConvert.INSTANCE.convertPage(pageResult);
//        List<CustomerRespVO> customers = new ArrayList<>();
//        for (CustomerDO customerDO : pageResult.getList()) {
//            customers.add(commonGetCustomer(customerDO));
//        }
//        pageData.setList(customers);
//
//        return success(pageData);
        return null;
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出客户 Excel")
    @PreAuthorize("@ss.hasPermission('jl:customer:export')")
    @OperateLog(type = EXPORT)
    public void exportCustomerExcel(@Valid CustomerExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
//        List<CustomerDO> list = customerService.getCustomerList(exportReqVO);
//        // 导出 Excel
//        List<CustomerExcelVO> datas = CustomerConvert.INSTANCE.convertList02(list);
//        ExcelUtils.write(response, "客户.xls", "数据", CustomerExcelVO.class, datas);
    }

}
