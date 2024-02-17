package cn.iocoder.yudao.module.crm.controller.admin.contract;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.number.NumberUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractSaveReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.CrmContractTransferReqVO;
import cn.iocoder.yudao.module.crm.convert.contract.CrmContractConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contact.CrmContactDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractProductDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.contact.CrmContactService;
import cn.iocoder.yudao.module.crm.service.contract.CrmContractService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.crm.service.product.CrmProductService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static java.util.Collections.singletonList;

@Tag(name = "管理后台 - CRM 合同")
@RestController
@RequestMapping("/crm/contract")
@Validated
public class CrmContractController {

    @Resource
    private CrmContractService contractService;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmContactService contactService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private CrmProductService productService;
    @Resource
    private AdminUserApi adminUserApi;

    @PostMapping("/create")
    @Operation(summary = "创建合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:create')")
    public CommonResult<Long> createContract(@Valid @RequestBody CrmContractSaveReqVO createReqVO) {
        return success(contractService.createContract(createReqVO, getLoginUserId()));
    }

    @PutMapping("/update")
    @Operation(summary = "更新合同")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> updateContract(@Valid @RequestBody CrmContractSaveReqVO updateReqVO) {
        contractService.updateContract(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除合同")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:contract:delete')")
    public CommonResult<Boolean> deleteContract(@RequestParam("id") Long id) {
        contractService.deleteContract(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得合同")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<CrmContractRespVO> getContract(@RequestParam("id") Long id) {
        // 1. 查询合同
        CrmContractDO contract = contractService.getContract(id);
        if (contract == null) {
            return success(null);
        }

        // 2. 拼接合同信息
        List<CrmContractRespVO> respVOList = buildContractDetailList(singletonList(contract));
        return success(respVOList.get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "获得合同分页")
    @PreAuthorize("@ss.hasPermission('crm:contract:query')")
    public CommonResult<PageResult<CrmContractRespVO>> getContractPage(@Valid CrmContractPageReqVO pageVO) {
        PageResult<CrmContractDO> pageResult = contractService.getContractPage(pageVO, getLoginUserId());
        return success(BeanUtils.toBean(pageResult, CrmContractRespVO.class).setList(buildContractDetailList(pageResult.getList())));
    }

    @GetMapping("/page-by-customer")
    @Operation(summary = "获得合同分页，基于指定客户")
    public CommonResult<PageResult<CrmContractRespVO>> getContractPageByCustomer(@Valid CrmContractPageReqVO pageVO) {
        Assert.notNull(pageVO.getCustomerId(), "客户编号不能为空");
        PageResult<CrmContractDO> pageResult = contractService.getContractPageByCustomerId(pageVO);
        return success(BeanUtils.toBean(pageResult, CrmContractRespVO.class).setList(buildContractDetailList(pageResult.getList())));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出合同 Excel")
    @PreAuthorize("@ss.hasPermission('crm:contract:export')")
    @OperateLog(type = EXPORT)
    public void exportContractExcel(@Valid CrmContractPageReqVO exportReqVO,
                                    HttpServletResponse response) throws IOException {
        PageResult<CrmContractDO> pageResult = contractService.getContractPage(exportReqVO, getLoginUserId());
        // 导出 Excel
        ExcelUtils.write(response, "合同.xls", "数据", CrmContractRespVO.class,
                BeanUtils.toBean(pageResult.getList(), CrmContractRespVO.class));
    }

    @PutMapping("/transfer")
    @Operation(summary = "合同转移")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> transferContract(@Valid @RequestBody CrmContractTransferReqVO reqVO) {
        contractService.transferContract(reqVO, getLoginUserId());
        return success(true);
    }

    @PutMapping("/submit")
    @Operation(summary = "提交合同审批")
    @PreAuthorize("@ss.hasPermission('crm:contract:update')")
    public CommonResult<Boolean> submitContract(@RequestParam("id") Long id) {
        contractService.submitContract(id, getLoginUserId());
        return success(true);
    }

    /**
     * 构建详细的合同结果
     *
     * @param contractList 原始合同信息
     * @return 细的合同结果
     */
    private List<CrmContractRespVO> buildContractDetailList(List<CrmContractDO> contractList) {
        if (CollUtil.isEmpty(contractList)) {
            return Collections.emptyList();
        }
        // 1. 获取客户列表
        List<CrmCustomerDO> customerList = customerService.getCustomerList(
                convertSet(contractList, CrmContractDO::getCustomerId));
        // 2. 获取创建人、负责人列表
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(convertListByFlatMap(contractList,
                contact -> Stream.of(NumberUtils.parseLong(contact.getCreator()), contact.getOwnerUserId())));
        // 3. 获取联系人
        Map<Long, CrmContactDO> contactMap = convertMap(contactService.getContactListByIds(convertSet(contractList,
                CrmContractDO::getContactId)), CrmContactDO::getId);
        // 4. 获取商机
        Map<Long, CrmBusinessDO> businessMap = convertMap(businessService.getBusinessList(convertSet(contractList,
                CrmContractDO::getBusinessId)), CrmBusinessDO::getId);
        // 5. 获取合同关联的商品
        Map<Long, CrmContractProductDO> contractProductMap = null;
        List<CrmProductDO> productList = null;
        if (contractList.size() == 1) {
            List<CrmContractProductDO> contractProductList = contractService.getContractProductListByContractId(contractList.get(0).getId());
            contractProductMap = convertMap(contractProductList, CrmContractProductDO::getProductId);
            productList = productService.getProductListByIds(convertSet(contractProductList, CrmContractProductDO::getProductId));
        }
        return CrmContractConvert.INSTANCE.convertList(contractList, userMap, customerList, contactMap, businessMap, contractProductMap, productList);
    }

}
