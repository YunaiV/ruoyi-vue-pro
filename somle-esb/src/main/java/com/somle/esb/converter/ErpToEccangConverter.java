package com.somle.esb.converter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.somle.eccang.model.*;
import com.somle.eccang.service.EccangService;
import com.somle.esb.enums.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.somle.esb.enums.ErrorCodeConstants.DEPT_LEVEL_ERROR;
import static com.somle.esb.util.ConstantConvertUtils.getCountrySuffix;

@Service
public class ErpToEccangConverter {


    @Autowired
    EccangService eccangService;

    @Autowired
    private DeptApi deptApi;
    @Autowired
    private AdminUserApi userApi;
    @Autowired
    private DictDataApi dictDataApi;

//    public EccangProduct toEccang(ErpCountrySku erpCountrySku) {
//        ErpStyleSku erpStyleSku = erpCountrySku.getStyleSku();
//        Long deptId = erpStyleSku.getSaleDepartmentId();
//        EccangProduct product = new EccangProduct();
//        List<ErpDepartment> path = erpDepartmentService.getDepartmentParents(deptId).toList();
//        Collections.reverse(path);
//        // Integer level = path.size() - 1;
//        EccangOrganization organization = eccangService.getOrganizationByNameEn(path.get(2).getId().toString());
//
//        product.setProductSku(erpCountrySku.getCountrySku());
//        product.setProductTitle(erpStyleSku.getNameZh());
//        product.setProductTitleEn(erpStyleSku.getNameEn());
//
//        product.setProductImgUrlList(erpStyleSku.getImageUrlList());
//
//        product.setPdNetWeight(erpStyleSku.getWeight());
//        product.setPdNetLength(erpStyleSku.getLength());
//        product.setPdNetWeight(erpStyleSku.getWidth());
//        product.setPdNetHeight(erpStyleSku.getHeight());
//        product.setProductWeight(erpStyleSku.getPackageWeight());
//        product.setProductLength(erpStyleSku.getPackageLength());
//        product.setProductWidth(erpStyleSku.getPackageWidth());
//        product.setProductHeight(erpStyleSku.getPackageHeight());
//        product.setProductMaterial(erpStyleSku.getMaterialZh());
//        product.setMaterialEn(erpStyleSku.getMaterialEn());
//
//
//        product.setProductPurchaseValue(erpStyleSku.getPurchasePrice());
//        product.setCurrencyCode(erpStyleSku.getPurchasePriceCurrencyCode());
//        product.setDefaultSupplierCode(erpStyleSku.getDefaultSupplierCode());
//
//        product.setSaleStatus(erpStyleSku.getSaleStatus());
//
//        product.setLogisticAttribute(erpCountrySku.getLogisticAttribute());
//        product.setHsCode(erpCountrySku.getHscode());
//        product.setProductDeclaredValue(erpCountrySku.getDeclaredValue());
//        product.setPdDeclareCurrencyCode(erpCountrySku.getDeclaredValueCurrencyCode());
//        product.setPdOverseaTypeCn(erpCountrySku.getDeclaredTypeZh());
//        product.setPdOverseaTypeEn(erpCountrySku.getDeclaredTypeEn());
//        product.setFboTaxRate(erpCountrySku.getExportCustomTaxRate());
//        product.setPdDeclarationStatement(erpCountrySku.getImportCustomTaxRate().toString());
//
//
//
//        try {
//            EccangCategory category1 = eccangService.getCategoryByNameEn(path.get(1).getId().toString());
//            product.setProductCategoryId1(category1.getPcId());
//            EccangCategory category2 = eccangService.getCategoryByNameEn(path.get(2).getId().toString());
//            product.setProductCategoryId2(category2.getPcId());
//            EccangCategory category3 = eccangService.getCategoryByNameEn(deptId.toString());
//            product.setProductCategoryId3(category3.getPcId());
//        } catch (Exception e) {
//        }
//
//        product.setUserOrganizationId(organization.getId());
//        return product;
//    }
    /**
     * 将ERP产品列表转换为完整的Eccang产品列表。
     *
     * @param customRuleDTOs ERP产品列表
     * @return 转换后的Eccang产品列表
     */
    public List<EccangProduct> customRuleDTOToProduct(List<ErpCustomRuleDTO> customRuleDTOs) {
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(customRuleDTOs, product -> Long.parseLong(product.getProductCreatorId())));
        return customRuleDTOs.stream()
            .map(product -> customRuleToProduct(product, userMap))
            .collect(Collectors.toList());
    }

    /**
     * 将ERP产品列表转换为简化的Eccang产品列表。
     *
     * @param productDTOs ERP产品列表
     * @return 转换后的简化版Eccang产品列表
     */
    public List<EccangProduct> productDTOToProduct(List<ErpProductDTO> productDTOs) {
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(productDTOs, product -> Long.parseLong(product.getCreator())));
        return productDTOs.stream()
            .map(product -> productToProduct(product, userMap))
            .collect(Collectors.toList());
    }

    /**
     * 将单个ERP产品转换为Eccang产品。
     *
     * @param customRuleDTO ERP产品对象
     * @param userMap 用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct customRuleToProduct(ErpCustomRuleDTO customRuleDTO, Map<Long, AdminUserRespDTO> userMap) {
        EccangProduct eccangProduct = new EccangProduct();
        eccangProduct.setPdDeclarationStatement(customRuleDTO.getId());
        // 设置SKU和标题
        Integer countryCode = customRuleDTO.getCountryCode();
        if (ObjUtil.isNotEmpty(countryCode)) {
            DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.COUNTRY_CODE, String.valueOf(countryCode));
            if (StrUtil.isNotBlank(customRuleDTO.getBarCode())) {
                eccangProduct.setProductTitle(customRuleDTO.getProductName() + "-" + getCountrySuffix(dictData.getLabel()));
                eccangProduct.setProductTitleEn(customRuleDTO.getBarCode() + "-" + getCountrySuffix(dictData.getLabel()));
                eccangProduct.setProductSku(customRuleDTO.getBarCode() + "-" + getCountrySuffix(dictData.getLabel()));
            }
        }
        // 设置货币代码
        Integer declaredValueCurrencyCode = customRuleDTO.getDeclaredValueCurrencyCode();
        if (ObjUtil.isNotEmpty(declaredValueCurrencyCode)) {
            DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, String.valueOf(declaredValueCurrencyCode));
            eccangProduct.setPdDeclareCurrencyCode(dictData.getLabel());
        }
        Integer purchasePriceCurrencyCode = customRuleDTO.getPurchasePriceCurrencyCode();
        if (ObjUtil.isNotEmpty(purchasePriceCurrencyCode)) {
            DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, String.valueOf(purchasePriceCurrencyCode));
            eccangProduct.setCurrencyCode(dictData.getLabel());
        }
        eccangProduct.setProductDeclaredValue(customRuleDTO.getDeclaredValue());
        eccangProduct.setPdOverseaTypeEn(customRuleDTO.getDeclaredTypeEn());

        // 设置产品尺寸和重量
        eccangProduct.setProductWeight(customRuleDTO.getPackageWeight());
        eccangProduct.setProductWidth(customRuleDTO.getPackageWidth());
        eccangProduct.setProductLength(customRuleDTO.getPackageLength());
        eccangProduct.setProductHeight(customRuleDTO.getPackageHeight());
        eccangProduct.setProductMaterial(customRuleDTO.getProductMaterial());
        eccangProduct.setPdNetWeight(customRuleDTO.getProductWeight());
        eccangProduct.setPdNetLength(customRuleDTO.getProductLength() / 100);
        eccangProduct.setPdNetWidth(customRuleDTO.getProductWidth() / 100);
        eccangProduct.setPdNetHeight(customRuleDTO.getProductHeight() / 100);

        // 设置其他产品属性
        eccangProduct.setProductPurchaseValue(customRuleDTO.getProductPurchaseValue());
        eccangProduct.setFboTaxRate(customRuleDTO.getTaxRate());
        eccangProduct.setPdOverseaTypeCn(customRuleDTO.getDeclaredType());
        eccangProduct.setProductImgUrlList(Collections.singletonList(customRuleDTO.getProductImageUrl()));
//        eccangProduct.setHsCode(customRuleDTO.getHscode());//暂停向eccang同步该属性
        eccangProduct.setDefaultSupplierCode("默认供应商");
        // 设置物流属性
        Integer logisticAttribute = customRuleDTO.getLogisticAttribute();
        if (ObjUtil.isNotEmpty(logisticAttribute)) {
            eccangProduct.setLogisticAttribute(String.valueOf(logisticAttribute));
        }

        // 设置销售状态和声明价值
        eccangProduct.setSaleStatus(2);
        // 设置产品创建人部门名称
        MapUtils.findAndThen(userMap, Long.parseLong(customRuleDTO.getProductCreatorId()),
            user -> eccangProduct.setUserOrganizationId(eccangService.getOrganizationByNameEn(String.valueOf(customRuleDTO.getProductDeptId())).getId()));

        // 设置品类
        TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(customRuleDTO.getProductDeptId());
        if (CollectionUtil.isEmpty(deptTreeLevel) || deptTreeLevel.size() > 3) {
            throw new RuntimeException("品类部门信息异常，请联系管理员，检查erp中产品资料库中的部门信息");
        }
        deptTreeLevel.pollFirst();

        int index = 1;
        for (DeptLevelRespDTO deptLevelRespDTO : deptTreeLevel) {
            Field categoryNameField = ReflectUtil.getField(EccangProduct.class, "procutCategoryName" + index);
            ReflectUtil.setFieldValue(eccangProduct, categoryNameField, deptLevelRespDTO.getDeptName());
            Field categoryNameEnField = ReflectUtil.getField(EccangProduct.class, "procutCategoryNameEn" + index);
            ReflectUtil.setFieldValue(eccangProduct, categoryNameEnField, deptLevelRespDTO.getDeptName());
            index += 1;
        }

        return eccangProduct;
    }

    /**
     * 将单个ERP产品转换为Eccang产品。
     *
     * @param product ERP产品对象
     * @param userMap 用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct productToProduct(ErpProductDTO product, Map<Long, AdminUserRespDTO> userMap) {
        EccangProduct eccangProduct = new EccangProduct();
        // 设置SKU和标题
        eccangProduct.setProductTitle(product.getName());
        eccangProduct.setProductTitleEn(product.getBarCode());
        eccangProduct.setProductSku(product.getBarCode());
        eccangProduct.setPdDeclareCurrencyCode("1");
        eccangProduct.setCurrencyCode("1");
        eccangProduct.setProductDeclaredValue(1.0f);
        eccangProduct.setPdOverseaTypeEn("无");

        // 设置产品尺寸和重量
        eccangProduct.setProductMaterial(product.getMaterial());
        eccangProduct.setPdNetWeight(Float.valueOf(product.getWeight()));
        eccangProduct.setPdNetLength(Float.valueOf(product.getLength()) / 100);
        eccangProduct.setPdNetWidth(Float.valueOf(product.getWidth()) / 100);
        eccangProduct.setPdNetHeight(Float.valueOf(product.getHeight()) / 100);
        eccangProduct.setProductImgUrlList(Collections.singletonList(product.getPrimaryImageUrl()));
        eccangProduct.setDefaultSupplierCode("默认供应商");
        // 设置销售状态和声明价值
        eccangProduct.setSaleStatus(2);
        // 设置产品创建人部门名称
        MapUtils.findAndThen(userMap, Long.parseLong(product.getCreator()),
            user -> eccangProduct.setUserOrganizationId(eccangService.getOrganizationByNameEn(String.valueOf(product.getDeptId())).getId()));

        // 设置品类
        TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(product.getDeptId());
        if (CollectionUtil.isEmpty(deptTreeLevel) || deptTreeLevel.size() > 3) {
            throw new RuntimeException("品类部门信息异常，请联系管理员，检查erp中产品资料库中的部门信息");
        }
        deptTreeLevel.pollFirst();

        int index = 1;
        for (DeptLevelRespDTO deptLevelRespDTO : deptTreeLevel) {
            Field categoryNameField = ReflectUtil.getField(EccangProduct.class, "procutCategoryName" + index);
            ReflectUtil.setFieldValue(eccangProduct, categoryNameField, deptLevelRespDTO.getDeptName());
            Field categoryNameEnField = ReflectUtil.getField(EccangProduct.class, "procutCategoryNameEn" + index);
            ReflectUtil.setFieldValue(eccangProduct, categoryNameEnField, deptLevelRespDTO.getDeptName());
            index += 1;
        }

        return eccangProduct;
    }




    public EccangCategory toEccang(String deptId) {
        //从erp中获取部门信息
        DeptRespDTO dept = deptApi.getDept(Long.valueOf(deptId));
        //获取部门名称
        String deptName = dept.getName();
        TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(Long.valueOf(deptId));
        int len = deptTreeLevel.size();
        //因为erp的层级比eccang高一级，所以减1
        int deptLevel = len - 1;
        //如果层级为0，则忽略并抛出异常通知操作人员
        ThrowUtil.ifThrow(deptLevel <= 0, DEPT_LEVEL_ERROR);
        String actionType = "ADD";
        Integer id = null;
        Integer pid = 0;
        //判断该品类名在易仓中是否已经存在，存在则去修改，不存在则去新增
        EccangCategory categoryByName = eccangService.getCategoryByErpDeptId(deptId);
        if (categoryByName != null){
            id = categoryByName.getPcId();
            actionType = "EDIT";
        }
        DeptLevelRespDTO parentDept = getParentName(deptLevel, deptTreeLevel);
        Long deptParentId = parentDept.getDeptId();
        if (!Objects.equals(deptParentId, TenantId.DEFAULT.getId())) {
            EccangCategory category = eccangService.getCategoryByErpDeptId(String.valueOf(deptParentId));
            if (category == null) {
                throw new RuntimeException("父类【" + parentDept.getDeptName() + "】在易仓中不存在，请检查erp中产品资料库中的部门信息");
            }
            //获取父id
            pid = category.getPcId();
        }
        return EccangCategory.builder()
                .actionType(actionType)
                .pcId(id)
                .pcName(deptName)
                .pcNameEn(deptId)
                .pcPid(pid)
                .pcLevel(Math.min(deptLevel, 3))
                .build();
    }
    /**
    * @Author Wqh
    * @Description 获取父级名称
    * @Date  2024/11/25
    * @Param [deptTreeLevel]
    **/
        private DeptLevelRespDTO getParentName(Integer deptLevel, TreeSet<DeptLevelRespDTO> deptTreeLevel) {
        if (deptLevel > 3) {
            //向上找父类
            deptTreeLevel.pollLast();
            getParentName(--deptLevel, deptTreeLevel);
        } else {
            //因为erp的层级比eccang高一级，所以移除一个
            deptTreeLevel.pollLast();
        }
        return deptTreeLevel.last();
    }
}
