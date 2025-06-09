package com.somle.esb.converter;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseInDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseOrderDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseOrderItemDTO;
import cn.iocoder.yudao.module.srm.api.purchase.dto.SrmPurchaseReturnDTO;
import cn.iocoder.yudao.module.srm.api.supplier.dto.SrmSupplierDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.TmsCustomRuleDTO;
import cn.iocoder.yudao.module.tms.enums.TmsDictTypeConstants;
import com.somle.kingdee.model.*;
import com.somle.kingdee.model.supplier.KingdeeSupplierSaveVO;
import com.somle.kingdee.model.supplier.SupplierBomentity;
import com.somle.kingdee.service.KingdeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.number.LengthUtils.mmToCmAsFloat;
import static com.somle.esb.util.ConstantConvertUtils.getCountrySuffix;

/**
 * 动态转换组件，根据第三方系统返回的数据，转换为XX格式。
 */
@Slf4j
@Service
public class ErpToKingdeeConverter {

    @Autowired
    KingdeeService kingdeeService;

    @Autowired
    private DeptApi deptApi;
    @Autowired
    private DictDataApi dictDataApi;

    /**
     * 将ERP产品列表转换为完整的Kingdee产品列表。
     *
     * @param customRuleDTOs ERP产品列表
     * @return 转换后的Kingdee产品列表
     */
    public List<KingdeeProductSaveReqVO> convert(List<TmsCustomRuleDTO> customRuleDTOs) {
        log.debug("Converting ERP products to full Kingdee products");
        return customRuleDTOs.stream()
            .map(this::convert)
            .collect(Collectors.toList());
    }

    /**
     * 将ERP产品列表转换为简化的Kingdee产品列表。
     *
     * @param productDTOs ERP产品列表
     * @return 转换后的简化版Kingdee产品列表
     */
    public List<KingdeeProductSaveReqVO> toKingdeeProducts(List<ErpProductDTO> productDTOs) {
        log.debug("Converting ERP products to simple Kingdee products");
        return productDTOs.stream()
            .map(this::toKingdeeProduct)
            .collect(Collectors.toList());
    }

    /**
     * 将单个ERP产品转换为Kingdee产品。
     *
     * @param customRuleDTO ERP产品对象
     * @return 转换后的Kingdee产品对象
     */
    private KingdeeProductSaveReqVO convert(TmsCustomRuleDTO customRuleDTO) {
        ErpProductDTO productDTO = customRuleDTO.getProductDTO();
        KingdeeProductSaveReqVO reqVO = new KingdeeProductSaveReqVO();
        //普通
        reqVO.setCheckType("1");
        // 获取国家编码
        Integer countryCode = customRuleDTO.getCountryCode();
        // 获取产品名称
        String productName = productDTO.getName();
        // 获取产品条码
        String productCode = productDTO.getCode();
        // 如果国家编码不为空，且产品条码不为空，设置SKU
        if (ObjectUtil.isNotEmpty(countryCode)) {
            DictDataRespDTO dictData = dictDataApi.getDictData(TmsDictTypeConstants.COUNTRY_CODE, String.valueOf(countryCode));
            if (CharSequenceUtil.isNotBlank(productCode)) {
                String countrySuffix = getCountrySuffix(dictData.getLabel());
                reqVO.setNumber(productCode + "-" + countrySuffix);
                reqVO.setName(productName + "-" + countrySuffix);
            }
        }
        // 如果国家编码为空，且产品名称不为空，设置SKU
        else if (ObjectUtil.isNotEmpty(productName) && ObjectUtil.isNotEmpty(productCode)) {
            reqVO.setNumber(productCode);
            reqVO.setName(productName);
        }
        reqVO.setBarcode(productDTO.getCode());
        // 报关品名
        reqVO.setProducingPace(customRuleDTO.getDeclaredType());
        reqVO.setDeclaredTypeEn(customRuleDTO.getDeclaredTypeEn());
        // HS编码
        reqVO.setHelpCode(customRuleDTO.getHscode());
        reqVO.setCostMethod("2");
        //给金蝶-包装属性
        reqVO.setGrossWeight(String.valueOf(productDTO.getPackageWeight()));
        //给金蝶-净重
        reqVO.setNetWeight(String.valueOf(productDTO.getWeight()));
        Float pdNetLength = mmToCmAsFloat(productDTO.getPackageLength());
        Float pdNetWidth = mmToCmAsFloat(productDTO.getPackageWidth());
        Float pdNetHeight = mmToCmAsFloat(productDTO.getPackageHeight());
        reqVO.setLength(String.valueOf(pdNetLength));
        reqVO.setWide(String.valueOf(pdNetWidth));
        reqVO.setHigh(String.valueOf(pdNetHeight));

        if (pdNetLength != null && pdNetWidth != null && pdNetHeight != null) {
            reqVO.setVolume(String.valueOf(pdNetLength * pdNetWidth * pdNetHeight));
        }
        //部门id，映射到金蝶自定义字段中
        //在金蝶中辅助资料对应的就是erp中的部门，非树形结构，在辅助资料中，由一个辅助分类是部门/报关品名（部门公司）
        reqVO.setSaleDepartmentId(productDTO.getDeptId());
        reqVO.setDeclaredTypeZh(customRuleDTO.getDeclaredType());
        //将报关规则的id存到这里面去
        reqVO.setMaxInventoryQty(String.valueOf(customRuleDTO.getId()));
        return reqVO;
    }


    /**
     * 将单个ERP产品转换为Kingdee产品。
     *
     * @param productDTO ERP产品对象
     * @return 转换后的Kingdee产品对象
     */
    private KingdeeProductSaveReqVO toKingdeeProduct(ErpProductDTO productDTO) {
        KingdeeProductSaveReqVO reqVO = new KingdeeProductSaveReqVO();
        //普通
        reqVO.setCheckType("1");
        reqVO.setNumber(productDTO.getCode());
        reqVO.setName(productDTO.getName());
        reqVO.setBarcode(productDTO.getCode());
        reqVO.setCostMethod("2");

        reqVO.setGrossWeight(String.valueOf(productDTO.getPackageWeight()));
        //给金蝶-净重
        reqVO.setNetWeight(String.valueOf(productDTO.getWeight()));
        Float pdNetLength = mmToCmAsFloat(productDTO.getPackageLength());
        Float pdNetWidth = mmToCmAsFloat(productDTO.getPackageWidth());
        Float pdNetHeight = mmToCmAsFloat(productDTO.getPackageHeight());
        reqVO.setLength(String.valueOf(pdNetLength));
        reqVO.setWide(String.valueOf(pdNetWidth));
        reqVO.setHigh(String.valueOf(pdNetHeight));
        //部门id，映射到金蝶自定义字段中
        //在金蝶中辅助资料对应的就是erp中的部门，非树形结构，在辅助资料中，由一个辅助分类是部门/报关品名（部门公司）
        reqVO.setSaleDepartmentId(productDTO.getDeptId());
        return reqVO;
    }


    public KingdeeSupplierSaveVO toKingdee(SrmSupplierDTO erpSupplierDTO) {
        KingdeeSupplierSaveVO kingdeeSupplierSaveVO = new KingdeeSupplierSaveVO();
        kingdeeSupplierSaveVO.setName(erpSupplierDTO.getName());
        kingdeeSupplierSaveVO.setAccountOpenAddr(erpSupplierDTO.getBankAddress());
        kingdeeSupplierSaveVO.setBank(erpSupplierDTO.getBankName());
        kingdeeSupplierSaveVO.setBankAccount(erpSupplierDTO.getBankAccount());
        kingdeeSupplierSaveVO.setRemark(erpSupplierDTO.getRemark());
        kingdeeSupplierSaveVO.setRate(String.valueOf(erpSupplierDTO.getTaxRate()));
        kingdeeSupplierSaveVO.setTaxpayerNo(erpSupplierDTO.getTaxNo());
        List<SupplierBomentity> bomEntityList = new ArrayList<>();
        SupplierBomentity bomEntity = new SupplierBomentity();
        bomEntity.setContactPerson(erpSupplierDTO.getContact());
        bomEntity.setMobile(erpSupplierDTO.getMobile());
        bomEntity.setEmail(erpSupplierDTO.getEmail());
        kingdeeSupplierSaveVO.setBomEntity(bomEntityList);
        return kingdeeSupplierSaveVO;
    }

    public KingdeeAuxInfoDetail toKingdee(String deptId) {
        //这里erp的部门id对应金蝶的部门number
        //从erp中获取部门信息
        DeptRespDTO dept = deptApi.getDept(Long.valueOf(deptId));
        KingdeeAuxInfoDetail department = new KingdeeAuxInfoDetail();
        department.setName(dept.getName());
        department.setNumber(deptId);
        department.setRemark(LocalDateTime.now().toString());
        return department;
    }

    public KingdeeAuxInfoDetail toKingdee(DeptRespDTO erpDepartment) {
        // Map<String, String> departmentMap = new HashMap<>();
        // departmentMap.put("ABD美视区1组", "ABD0101");
        // departmentMap.put("ABD美视区2组", "ABD0102");
        // departmentMap.put("ABD美视区", "ABD0199");
        // departmentMap.put("ABD欧视区1组", "ABD0201");
        // departmentMap.put("ABD欧视区2组", "ABD0202");
        // departmentMap.put("ABD欧视区", "ABD0299");
        // departmentMap.put("ABD加视区1组", "ABD0301");
        // departmentMap.put("ABD加视综合区", "ABD0399");
        // departmentMap.put("ABD亚视区1组", "ABD0401");
        // departmentMap.put("ABD亚视区", "ABD0499");
        // departmentMap.put("ABD视听事业部", "ABD9999");
        // departmentMap.put("CBD美创组", "CBD0101");
        // departmentMap.put("CBD欧创1组", "CBD0102");
        // departmentMap.put("CBD欧创2组", "CBD0103");
        // departmentMap.put("CBD加创组", "CBD0105");
        // departmentMap.put("CBD亚创组", "CBD0106");
        // departmentMap.put("CBD美亚独立站", "CBD0201");
        // departmentMap.put("CBD美亚业务部", "CBD9901");
        // departmentMap.put("CBD欧洲业务部", "CBD9902");
        // departmentMap.put("CBD创意事业部", "CBD9999");
        // departmentMap.put("DCD国内品牌事业部", "DCD9999");
        // departmentMap.put("EBD美办区1组", "EBD0101");
        // departmentMap.put("EBD美办区2组", "EBD0102");
        // departmentMap.put("EBD美办区", "EBD0199");
        // departmentMap.put("EBD欧办区1组", "EBD0201");
        // departmentMap.put("EBD欧办区2组", "EBD0202");
        // departmentMap.put("EBD欧办区3组", "EBD0203");
        // departmentMap.put("EBD欧办区4组", "EBD0204");
        // departmentMap.put("EBD欧办区", "EBD0299");
        // departmentMap.put("EBD加办区1组", "EBD0301");
        // departmentMap.put("EBD加办综合区", "EBD0399");
        // departmentMap.put("EBD亚办区1组", "EBD0401");
        // departmentMap.put("EBD亚办区", "EBD0499");
        // departmentMap.put("EBD办公事业部", "EBD9999");
        // departmentMap.put("HCD美家1组", "HCD0101");
        // departmentMap.put("HCD美家二区1组", "HCD0201");
        // departmentMap.put("HCD美家二区2组", "HCD0202");
        // departmentMap.put("HCD美家3组", "HCD0301");
        // departmentMap.put("HCD美家四区1组", "HCD0401");
        // departmentMap.put("HCD美家四区2组", "HCD0402");
        // departmentMap.put("HCD欧家区1组", "HCD0501");
        // departmentMap.put("HCD欧家区2组", "HCD0502");
        // departmentMap.put("HCD欧家区", "HCD0599");
        // departmentMap.put("HCD加家区1组", "HCD0601");
        // departmentMap.put("HCD加家区2组", "HCD0602");
        // departmentMap.put("HCD加家区", "HCD0699");
        // departmentMap.put("HCD亚家区1组", "HCD0701");
        // departmentMap.put("HCD独立站组", "HCD0801");
        // departmentMap.put("HCD路易十六业务部1组", "HCD0901");
        // departmentMap.put("HCD斯嘉丽业务部", "HCD1099");
        // departmentMap.put("HCDwayfair组", "HCD1101");
        // departmentMap.put("HCD家居事业部", "HCD9999");

        KingdeeAuxInfoDetail department = new KingdeeAuxInfoDetail();
        String number = String.valueOf(erpDepartment.getId());
        String name = String.valueOf(erpDepartment.getName());

        department.setName(name);
        department.setNumber(number);
        department.setRemark(LocalDateTime.now().toString());

        return department;
    }

    /**
     * 将 SRM 采购入库单转换为金蝶采购入库单
     *
     * @param inOrder SRM采购入库单
     * @return 金蝶采购入库单
     */
    KingdeePurInboundSaveReqVO convert(SrmPurchaseInDTO inOrder) {
        return null;
    }

    /**
     * 将 SRM 采购入库单列表转换为金蝶采购入库单列表
     *
     * @param inOrders SRM采购入库单列表
     * @return 金蝶采购入库单列表
     */
    public List<KingdeePurInboundSaveReqVO> convertInDTOList(List<SrmPurchaseInDTO> inOrders) {
        return null;
    }

    /**
     * 将 SRM 采购订单转换为金蝶采购订单
     * 转换包括：
     * 1. 基本信息：订单日期、订单编号、供应商信息等
     * 2. 商品分录：物料信息、数量、价格、金额等
     *
     * @param order SRM采购订单，包含订单基本信息和商品明细
     * @return 金蝶采购订单，如果输入为null则返回null
     */
    public KingdeePurOrderSaveReqVO convertOrderDTO(SrmPurchaseOrderDTO order) {
        if (order == null) {
            return null;
        }
        KingdeePurOrderSaveReqVO vo = new KingdeePurOrderSaveReqVO();

        // 1. 转换基本信息
        convertBasicInfo(vo, order);

        // 2. 转换商品分录
        if (order.getItems() != null) {
            vo.setMaterialEntity(convertToMaterialEntity(order.getItems()));
        }

        return vo;
    }

    /**
     * 转换订单基本信息
     * 包括：单据日期、单据编号、供应商信息、备注、总金额、外部单号等
     *
     * @param target 目标对象（金蝶采购订单）
     * @param source 源对象（SRM采购订单）
     */
    public void convertBasicInfo(KingdeePurOrderSaveReqVO target, SrmPurchaseOrderDTO source) {
        // 单据日期：格式化为 yyyy-MM-dd
        if (source.getOrderDate() != null) {
            target.setBillDate(DateUtil.format(source.getOrderDate(), "yyyy-MM-dd"));
        }

        // 单据编号
        target.setBillNo(StrUtil.trimToNull(source.getOrderNo()));

        // 供应商信息：ID转为字符串，编码去除空格
        if (source.getSupplierId() != null) {
            target.setSupplierId(String.valueOf(source.getSupplierId()));
        }
        target.setSupplierNumber(StrUtil.trimToNull(source.getSupplierCode()));

        // 备注
        target.setRemark(StrUtil.trimToNull(source.getRemark()));

        // 总金额：BigDecimal转Double
        if (source.getTotalAmount() != null) {
            target.setTotalAmount(source.getTotalAmount().doubleValue());
        }

        // 外部单号：ID转为字符串
        if (source.getId() != null) {
            target.setOutsidePkId(String.valueOf(source.getId()));
        }
    }

    /**
     * 将 SRM 采购订单列表转换为金蝶采购订单列表
     * 过滤掉null值，确保返回的列表不包含null元素
     *
     * @param orders SRM采购订单列表
     * @return 金蝶采购订单列表，如果输入为null则返回空列表
     */
    public List<KingdeePurOrderSaveReqVO> convertOrderDTOList(List<SrmPurchaseOrderDTO> orders) {
        if (orders == null) {
            return Collections.emptyList();
        }
        return orders.stream()
            .filter(Objects::nonNull)
            .map(this::convertOrderDTO)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 将 SRM 采购订单项列表转换为金蝶商品分录列表
     * 过滤掉null值，确保返回的列表不包含null元素
     *
     * @param items SRM采购订单项列表
     * @return 金蝶商品分录列表，如果输入为null则返回空列表
     */
    List<KingdeePurOrderSaveReqVO.MaterialEntity> convertToMaterialEntity(List<SrmPurchaseOrderItemDTO> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
            .filter(Objects::nonNull)
            .map(this::convertToMaterialEntity)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 将 SRM 采购订单项转换为金蝶商品分录
     * 转换包括：
     * 1. 物料信息：物料ID、物料编码
     * 2. 数量信息：数量、单价、金额
     * 3. 单位信息：单位ID、单位编码
     * 4. 其他信息：备注、交货日期、仓库、税率等
     *
     * @param item SRM采购订单项
     * @return 金蝶商品分录，如果输入为null则返回null
     */
    KingdeePurOrderSaveReqVO.MaterialEntity convertToMaterialEntity(SrmPurchaseOrderItemDTO item) {
        if (item == null) {
            return null;
        }
        KingdeePurOrderSaveReqVO.MaterialEntity entity = new KingdeePurOrderSaveReqVO.MaterialEntity();

        // 1. 物料信息
        entity.setMaterialId(String.valueOf(item.getMaterialId()));
        entity.setMaterialNumber(StrUtil.trimToNull(item.getMaterialCode()));

        // 2. 数量信息
        if (item.getQuantity() != null) {
            entity.setQty(item.getQuantity().doubleValue());
        }
        if (item.getPrice() != null) {
            entity.setPrice(item.getPrice().toString());
        }
        if (item.getAmount() != null) {
            entity.setAmount(item.getAmount().toString());
        }

        // 3. 单位信息
        entity.setUnitId(StrUtil.trimToNull(item.getUnit())); // 单位ID
        entity.setUnitNumber(StrUtil.trimToNull(item.getUnit())); // 单位编码

        // 4. 其他信息
        entity.setComment(StrUtil.trimToNull(item.getRemark()));
        entity.setDeliveryDate(item.getDeliveryDate());
        if (item.getWarehouseId() != null) {
            entity.setStockId(String.valueOf(item.getWarehouseId()));
        }
        entity.setStockNumber(StrUtil.trimToNull(item.getWarehouseName()));

        // 5. 税率相关信息
        if (item.getTaxRate() != null) {
            entity.setCess(String.valueOf(item.getTaxRate()));
        }
        if (item.getTaxAmount() != null) {
            entity.setTaxAmount(String.valueOf(item.getTaxAmount()));
        }
        if (item.getGrossPrice() != null) {
            entity.setActTaxPrice(String.valueOf(item.getGrossPrice()));
        }
        if (item.getGrossTotalPrice() != null) {
            entity.setAllAmount(String.valueOf(item.getGrossTotalPrice()));
        }

        // 6. 条码信息
        entity.setBarcode(StrUtil.trimToNull(item.getProductCode()));

        return entity;
    }


    /**
     * 将 SRM 采购退货单转换为金蝶采购退货单
     *
     * @param returnOrder SRM采购退货单
     * @return 金蝶采购退货单
     */
    public KingdeePurReturnSaveReqVO convertReturnDTO(SrmPurchaseReturnDTO returnOrder) {
        return null;
    }

    /**
     * 将 SRM 采购退货单列表转换为金蝶采购退货单列表
     *
     * @param returnOrders SRM采购退货单列表
     * @return 金蝶采购退货单列表
     */
    public List<KingdeePurReturnSaveReqVO> convertReturnDTOList(List<SrmPurchaseReturnDTO> returnOrders) {
        return null;
    }

    /**
     * 将 SRM 供应商转换为金蝶供应商
     *
     * @param supplier SRM供应商
     * @return 金蝶供应商
     */
    public KingdeeSupplierSaveVO convertSupplierDTO(SrmSupplierDTO supplier) {
        if (supplier == null) {
            return null;
        }
        KingdeeSupplierSaveVO kingdeeSupplierSaveVO = new KingdeeSupplierSaveVO();
        // 基本信息
        convertBasicInfo(kingdeeSupplierSaveVO, supplier);
        // 银行信息
        convertBankInfo(kingdeeSupplierSaveVO, supplier);
        // 税务信息
        convertTaxInfo(kingdeeSupplierSaveVO, supplier);
        // 地址信息
        convertAddressInfo(kingdeeSupplierSaveVO, supplier);
        // 联系人信息
        kingdeeSupplierSaveVO.setBomEntity(convertToBomEntity(supplier));
        return kingdeeSupplierSaveVO;
    }

    /**
     * 将 SRM 供应商列表转换为金蝶供应商列表
     *
     * @param suppliers SRM供应商列表
     * @return 金蝶供应商列表
     */
    public List<KingdeeSupplierSaveVO> convertSupplierDTOList(List<SrmSupplierDTO> suppliers) {
        if (suppliers == null) {
            return Collections.emptyList();
        }
        return suppliers.stream()
            .filter(Objects::nonNull)
            .map(this::convertSupplierDTO)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * 转换基本信息
     */
    void convertBasicInfo(KingdeeSupplierSaveVO target, SrmSupplierDTO source) {
        if (source.getId() != null) {
            //把供应商ID作为金蝶的供应商编码
            target.setNumber(String.valueOf(source.getId()));
        }
        target.setName(StrUtil.trimToNull(source.getName()));
        target.setRemark(StrUtil.trimToNull(source.getRemark()));
        // 设置开票名称，默认与供应商名称一致
        target.setInvoiceName(StrUtil.trimToNull(source.getName()));
    }

    /**
     * 转换银行信息
     */
    void convertBankInfo(KingdeeSupplierSaveVO target, SrmSupplierDTO source) {
        target.setAccountOpenAddr(StrUtil.trimToNull(source.getBankAddress()));
        target.setBank(StrUtil.trimToNull(source.getBankName()));
        target.setBankAccount(StrUtil.trimToNull(source.getBankAccount()));
    }

    /**
     * 转换税务信息
     */
    void convertTaxInfo(KingdeeSupplierSaveVO target, SrmSupplierDTO source) {
        if (source.getTaxRate() != null) {
            target.setRate(String.valueOf(source.getTaxRate()));
        }
        target.setTaxpayerNo(StrUtil.trimToNull(source.getTaxNo()));
    }

    /**
     * 转换地址信息
     */
    void convertAddressInfo(KingdeeSupplierSaveVO target, SrmSupplierDTO source) {
        // 设置详细地址
        target.setAddr(StrUtil.trimToNull(source.getCompanyAddress()));
        // 设置送达地址
        if (StrUtil.isNotBlank(source.getDeliveryAddress())) {
            target.setAddr(StrUtil.trimToNull(source.getDeliveryAddress()));
        }
    }

    /**
     * 转换为联系人信息(金蝶可以是复数联系人)
     */
    List<SupplierBomentity> convertToBomEntity(SrmSupplierDTO supplier) {
        if (supplier == null) {
            return Collections.emptyList();
        }
        SupplierBomentity bomEntity = new SupplierBomentity();
        bomEntity.setContactPerson(StrUtil.trimToNull(supplier.getContact()));
        bomEntity.setMobile(StrUtil.trimToNull(supplier.getMobile()));
        bomEntity.setPhone(StrUtil.trimToNull(supplier.getTelephone()));
        bomEntity.setEmail(StrUtil.trimToNull(supplier.getEmail()));
        // 设置为首要联系人
        bomEntity.setIsDefaultLinkman(true);
        return Collections.singletonList(bomEntity);
    }
}
