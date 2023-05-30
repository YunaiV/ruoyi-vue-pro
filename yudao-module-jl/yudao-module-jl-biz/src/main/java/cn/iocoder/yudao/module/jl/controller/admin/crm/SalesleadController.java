package cn.iocoder.yudao.module.jl.controller.admin.crm;

import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2competitorRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2customerplanRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2managerRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.JoinSaleslead2reportRespVO;
import cn.iocoder.yudao.module.jl.controller.admin.project.vo.ProjectBaseCreateReqVO;
import cn.iocoder.yudao.module.jl.convert.crm.CustomerConvert;
import cn.iocoder.yudao.module.jl.convert.crm.FollowupConvert;
import cn.iocoder.yudao.module.jl.convert.crm.InstitutionConvert;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2competitorConvert;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2customerplanConvert;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2managerConvert;
import cn.iocoder.yudao.module.jl.convert.join.JoinSaleslead2reportConvert;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.CustomerDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.FollowupDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2competitorDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2customerplanDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2managerDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinSaleslead2reportDO;
import cn.iocoder.yudao.module.jl.dal.dataobject.project.ProjectBaseDO;
import cn.iocoder.yudao.module.jl.service.crm.CustomerService;
import cn.iocoder.yudao.module.jl.service.crm.FollowupService;
import cn.iocoder.yudao.module.jl.service.crm.InstitutionService;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2competitorService;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2customerplanService;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2managerService;
import cn.iocoder.yudao.module.jl.service.join.JoinSaleslead2reportService;
import cn.iocoder.yudao.module.jl.service.project.ProjectBaseService;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileRespVO;
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
import java.time.LocalDateTime;
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
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.SalesleadDO;
import cn.iocoder.yudao.module.jl.convert.crm.SalesleadConvert;
import cn.iocoder.yudao.module.jl.service.crm.SalesleadService;

@Tag(name = "管理后台 - 销售线索")
@RestController
@RequestMapping("/jl/saleslead")
@Validated
public class SalesleadController {

    @Resource
    private SalesleadService salesleadService;

    @Resource
    private InstitutionService institutionService;

    @Resource
    private JoinSaleslead2reportService joinSaleslead2reportService;

    @Resource
    private JoinSaleslead2customerplanService joinSaleslead2customerplanService;

    @Resource
    private JoinSaleslead2competitorService joinSaleslead2competitorService;

    @Resource
    private JoinSaleslead2managerService joinSaleslead2managerService;

    @Resource
    private CustomerService customerService;

    @Resource
    private FollowupService followupService;

    @Resource
    private AdminUserService userService;

    @Resource
    private ProjectBaseService projectBaseService;

    @PostMapping("/create")
    @Operation(summary = "创建销售线索")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:create')")
    public CommonResult<Long> createSaleslead(@Valid @RequestBody SalesleadCreateReqVO createReqVO) {
        Long id = salesleadService.createSaleslead(createReqVO);

        // 为客户绑定最近的线索记录
        Long customerId = createReqVO.getCustomerId();
        CustomerUpdateSalesLeadVO customerUpdateReqVO = new CustomerUpdateSalesLeadVO();
        customerUpdateReqVO.setId(customerId);
        customerUpdateReqVO.setLastSalesleadId(id);
//        customerService.updateCustomerSalesLead(customerUpdateReqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "更新销售线索")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:update')")
    public CommonResult<Boolean> updateSaleslead(@Valid @RequestBody SalesleadUpdateReqVO updateReqVO) {
        Long loginUserId = getLoginUserId();
        salesleadService.updateSaleslead(updateReqVO);

        if(updateReqVO.getStatus() == 4) {
            // 创建合同

            // 创建项目
            ProjectBaseCreateReqVO project = new ProjectBaseCreateReqVO();
            project.setName(updateReqVO.getContractName());
            project.setStage("0"); // TODO 默认阶段，预开展阶段
            project.setSalesleadId(updateReqVO.getId());
            project.setSalesId(loginUserId);
            project.setType("0"); // TODO 项目类型
            project.setCustomerId(updateReqVO.getCustomerId());
            projectBaseService.createProjectBase(project);
        }
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除销售线索")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('jl:saleslead:delete')")
    public CommonResult<Boolean> deleteSaleslead(@RequestParam("id") Long id) {
        salesleadService.deleteSaleslead(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得销售线索")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<SalesleadRespVO> getSaleslead(@RequestParam("id") Long id) {
        SalesleadDO saleslead = salesleadService.getSaleslead(id);

        return success(commonGetSaleslead(saleslead));
    }

    private SalesleadRespVO commonGetSaleslead(SalesleadDO saleslead ) {
        if(saleslead == null) {
            return null;
        }

        SalesleadRespVO salesleadResp = SalesleadConvert.INSTANCE.convert(saleslead);

        // 查询报告数据
        List<JoinSaleslead2reportDO> joinSaleslead2reports = joinSaleslead2reportService.getReportBySalesleadId(saleslead.getId());
        if(joinSaleslead2reports != null) {
            List<JoinSaleslead2reportRespVO> saleslead2reports = JoinSaleslead2reportConvert.INSTANCE.convertList(joinSaleslead2reports);
            salesleadResp.setReports(saleslead2reports);
        }

        // 查询方案数据
        List<JoinSaleslead2customerplanDO> joinSaleslead2plans = joinSaleslead2customerplanService.getCustomerPlanBySalesleadId(saleslead.getId());
        if(joinSaleslead2plans != null) {
            List<JoinSaleslead2customerplanRespVO> saleslead2plans = JoinSaleslead2customerplanConvert.INSTANCE.convertList(joinSaleslead2plans);
            salesleadResp.setCustomerPlans(saleslead2plans);
        }

        // 查询项目人员数据
        List<JoinSaleslead2managerDO> joinSaleslead2managers = joinSaleslead2managerService.getBySalesleadId(saleslead.getId());
        if(joinSaleslead2managers != null) {
            List<JoinSaleslead2managerRespVO> saleslead2managers = JoinSaleslead2managerConvert.INSTANCE.convertList(joinSaleslead2managers);
            salesleadResp.setManagers(saleslead2managers);
        }

        // 查询竞争对手的报价
        List<JoinSaleslead2competitorDO> competitors = joinSaleslead2competitorService.getCompetitorBySalesleadId(saleslead.getId());
        if(competitors != null) {
            List<JoinSaleslead2competitorRespVO> competitorQuotations = JoinSaleslead2competitorConvert.INSTANCE.convertList(competitors);
            salesleadResp.setCompetitorQuotations(competitorQuotations);
        }

        // 添加客户信息
        Long customerId = saleslead.getCustomerId();
        if(customerId != null) {
//            CustomerDO customer = customerService.getCustomer(customerId);
//            if(customer != null) {
//                salesleadResp.setCustomer(commonGetCustomer(customer));
//            }
        }

        // 添加销售信息
//        Long salesId = Long.valueOf(saleslead.getCreator());
//        if(customerId != null) {
//            AdminUserDO salesDo = userService.getUser(salesId);
//            commonGetCustomer(salesDo)
//            UserProfileRespVO sales = UserConvert.INSTANCE.convert03(salesDo);
//            salesleadResp.setSales(sales);
//        }

        // 跟进记录
        Long salesleadId = saleslead.getId();
        FollowupDO followupDo = followupService.selectLatestOneBySealsLeadId(salesleadId);
        FollowupRespVO followup = FollowupConvert.INSTANCE.convert(followupDo);
        salesleadResp.setLatestFollowup(followup);

        return salesleadResp;
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

        // 绑定公司
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

        return customerRespVO;
    }

    @GetMapping("/list")
    @Operation(summary = "获得销售线索列表")
    @Parameter(name = "ids", description = "编号列表", required = true, example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<List<SalesleadRespVO>> getSalesleadList(@RequestParam("ids") Collection<Long> ids) {
        List<SalesleadDO> list = salesleadService.getSalesleadList(ids);
        return success(SalesleadConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/page")
    @Operation(summary = "获得销售线索分页")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:query')")
    public CommonResult<PageResult<SalesleadRespVO>> getSalesleadPage(@Valid SalesleadPageReqVO pageVO) {
        Long loginUserId = getLoginUserId();
        pageVO.setCreator(loginUserId); // 当前账号作为拥有者，过滤其他数据

        PageResult<SalesleadDO> pageResult = salesleadService.getSalesleadPage(pageVO);
        PageResult<SalesleadRespVO> pageData = SalesleadConvert.INSTANCE.convertPage(pageResult);
        List<SalesleadRespVO> salesleads = new ArrayList<>();
        for (SalesleadDO saleslead : pageResult.getList()) {
            salesleads.add(commonGetSaleslead(saleslead));
        }
        pageData.setList(salesleads);
        return success(pageData);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出销售线索 Excel")
    @PreAuthorize("@ss.hasPermission('jl:saleslead:export')")
    @OperateLog(type = EXPORT)
    public void exportSalesleadExcel(@Valid SalesleadExportReqVO exportReqVO,
              HttpServletResponse response) throws IOException {
        List<SalesleadDO> list = salesleadService.getSalesleadList(exportReqVO);
        // 导出 Excel
        List<SalesleadExcelVO> datas = SalesleadConvert.INSTANCE.convertList02(list);
        ExcelUtils.write(response, "销售线索.xls", "数据", SalesleadExcelVO.class, datas);
    }

}
