package com.somle.esb.converter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.supplier.dto.ErpSupplierDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.model.product.ErpStyleSku;
import com.somle.kingdee.model.KingdeeProduct;
import com.somle.kingdee.model.supplier.KingdeeSupplier;
import com.somle.kingdee.model.supplier.SupplierBomentity;
import com.somle.kingdee.service.KingdeeService;
import com.somle.kingdee.model.KingdeeAuxInfoDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import lombok.extern.slf4j.Slf4j;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.somle.esb.util.ConstantConvertUtils.getProductStatus;

@Slf4j
@Service
public class ErpToKingdeeConverter {

    @Autowired
    KingdeeService kingdeeService;

    @Autowired
    private DeptApi deptApi;

    public KingdeeProduct toKingdee(ErpCountrySku erpCountrySku) {
        ErpStyleSku erpStyleSku = erpCountrySku.getStyleSku();
        KingdeeProduct product = new KingdeeProduct();
//        try {
//            String id = kingdeeService.getMaterial(esbCountrySku.getCountrySku()).getData().getString("id");
//            product.setId(id);
//        } catch (Exception e) {
//            log.debug("id not found for " + esbCountrySku.getCountrySku() + "adding new");
//        }
        product.setCheckType("1"); //普通
        product.setName(erpStyleSku.getNameZh());
        product.setNumber(erpCountrySku.getCountrySku());
        product.setBarcode(erpStyleSku.getBarcode());
        product.setProducingPace(erpCountrySku.getDeclaredTypeZh()); //报关品名
        product.setHelpCode(erpCountrySku.getHscode()); //HS编码
        product.setCostMethod("2"); //加权平均

        product.setGrossWeight(String.valueOf(erpStyleSku.getPackageWeight()));
        product.setLength(String.valueOf(erpStyleSku.getLength()));
        product.setWide(String.valueOf(erpStyleSku.getWidth()));
        product.setHigh(String.valueOf(erpStyleSku.getHeight()));

        //product.setSaleDepartmentId(erpStyleSku.getSaleDepartmentId());
        //product.setDeclaredTypeZh(erpCountrySku.getDeclaredTypeZh());

        // product.setIsFloat(true);
        // product.setGrossWeight(product.getPackageWeight());
        // product.setLength(product.getLength());
        // product.setWide(product.getWidth());
        // product.setHigh(product.getHeight());

//        product.setVolumeUnitId(kingdeeService.getMeasureUnitByNumber("立方厘米").getId());
//        product.setWeightUnitId(kingdeeService.getMeasureUnitByNumber("kg").getId());
//        product.setBaseUnitId(kingdeeService.getMeasureUnitByNumber("套").getId());
//        product.setCustomField(
//            kingdeeService.getCustomFieldByDisplayName("bd_material", "部门"),
//            kingdeeService.getAuxInfoByNumber(esbStyleSku.getSaleDepartmentId().toString()).getId()
//        );
//        try {
//            product.setCustomField(kingdeeService.getCustomFieldByDisplayName("bd_material", "报关品名"), esbCountrySku.getDeclaredTypeZh());
//        } catch (Exception e) {
//            log.debug("custom field 报关品名 skipped for " + kingdeeService.token.getAccountName());
//        }

        return product;

    }

    public List<KingdeeProduct> toKingdee(List<ErpCustomRuleDTO> allProducts) {
        List<KingdeeProduct> kingdeeProducts = new ArrayList<>();
        for (ErpCustomRuleDTO product : allProducts){
            KingdeeProduct kingdeeProduct = new KingdeeProduct();
            //普通
            kingdeeProduct.setCheckType("1");
            kingdeeProduct.setName(product.getProductName());
            //如果有供应商产品编码和国家代码都不为空的时候才去设置SKU
            if (StrUtil.isNotBlank(product.getSupplierProductCode()) && StrUtil.isNotBlank(product.getCountryCode())) {
                kingdeeProduct.setNumber(product.getSupplierProductCode() + "-" + getProductStatus(product.getCountryCode()));
            }
            kingdeeProduct.setBarcode(product.getBarCode());
            //报关品名
            kingdeeProduct.setProducingPace(product.getDeclaredType());
            //HS编码
            kingdeeProduct.setHelpCode(product.getHscode());
            //加权平均
            kingdeeProduct.setCostMethod("2");
            kingdeeProduct.setGrossWeight(String.valueOf(product.getPackageWeight()));
            Float pdNetLength = product.getPackageLength();
            Float pdNetWidth = product.getPackageWidth();
            Float pdNetHeight = product.getPackageHeight();
            kingdeeProduct.setLength(String.valueOf(pdNetLength));
            kingdeeProduct.setWide(String.valueOf(pdNetWidth));
            kingdeeProduct.setHigh(String.valueOf(pdNetHeight));
            if (pdNetLength != null && pdNetWidth != null && pdNetHeight != null){
                kingdeeProduct.setVolume(String.valueOf(pdNetLength * pdNetWidth * pdNetHeight));
            }
            //部门id，映射到金蝶自定义字段中
            //在金蝶中辅助资料对应的就是erp中的部门，非树形结构，在辅助资料中，由一个辅助分类是部门/报关品名（部门公司）
            kingdeeProduct.setSaleDepartmentId(product.getProductDeptId());
            kingdeeProduct.setDeclaredTypeZh(product.getDeclaredType());
            //将报关规则的id存到这里面去
            kingdeeProduct.setMaxInventoryQty(product.getId());
            kingdeeProducts.add(kingdeeProduct);
        }

        return kingdeeProducts;

    }

    public KingdeeSupplier toKingdee(ErpSupplierDTO erpSupplierDTO) {
        KingdeeSupplier kingdeeSupplier = new KingdeeSupplier();
        kingdeeSupplier.setName(erpSupplierDTO.getName());
        kingdeeSupplier.setAccountOpenAddr(erpSupplierDTO.getBankAddress());
        kingdeeSupplier.setBank(erpSupplierDTO.getBankName());
        kingdeeSupplier.setBankAccount(erpSupplierDTO.getBankAccount());
        kingdeeSupplier.setRemark(erpSupplierDTO.getRemark());
        kingdeeSupplier.setRate(String.valueOf(erpSupplierDTO.getTaxPercent()));
        kingdeeSupplier.setTaxpayerNo(erpSupplierDTO.getTaxNo());
        List<SupplierBomentity> bomEntityList = new ArrayList<>();
        SupplierBomentity bomEntity = new SupplierBomentity();
        bomEntity.setContactPerson(erpSupplierDTO.getContact());
        bomEntity.setMobile(erpSupplierDTO.getMobile());
        bomEntity.setEmail(erpSupplierDTO.getEmail());
        kingdeeSupplier.setBomEntity(bomEntityList);
        return kingdeeSupplier;
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
}
