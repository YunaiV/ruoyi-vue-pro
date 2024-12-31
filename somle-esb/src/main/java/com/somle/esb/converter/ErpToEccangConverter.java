package com.somle.esb.converter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.somle.eccang.model.*;
import com.somle.eccang.service.EccangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.somle.esb.enums.ErrorCodeConstants.DEPT_LEVEL_ERROR;
import static com.somle.esb.job.SyncDepartmentsJob.TENANT_ID_DEFAULT;
import static com.somle.esb.util.ConstantConvertUtils.getProductStatus;

@Service
public class ErpToEccangConverter {
//    @Autowired
//    ErpDepartmentService erpDepartmentService;

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

    public List<EccangProduct> toEccang(List<ErpCustomRuleDTO> allProducts){
        //获取创建人用户信息
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(allProducts, i -> Long.parseLong(i.getProductCreatorId())));
        List<EccangProduct> allEccangProducts = new ArrayList<>();
        for (ErpCustomRuleDTO product : allProducts){
            /*//编辑箱规
            EccangProductBoxes box = new EccangProductBoxes();
            //箱规的中文名称和英文名称都根据sku来
            box.setBoxName(product.getProductSku()+"-"+ LocalDateTime.now());
            box.setBoxNameEn(product.getProductSku());
            //箱规的长宽重，就是产品包装长宽高重
            box.setBoxLength(String.valueOf(product.getPdNetLength()));
            box.setBoxWidth(String.valueOf(product.getPdNetWidth()));
            box.setBoxHeight(String.valueOf(product.getPdNetHeight()));
            box.setBoxWeight("0.001");
            //以及均为默认值
            box.setSmCode("0");
            box.setBoxStatus(1);
            //新增
            EccangResponse.BizContent bizContent = eccangService.addProductBoxes(box);
            JsonNode data = bizContent.getData();
            //获取boxId
            String boxId = data.get("box_id").asText();
            Map<String,Object> boxArr = new HashMap<>();
            boxArr.put("box_id",3);
            boxArr.put("box_quantity",1);
            boxArr.put("warehouse_id",0);*/
            //boxArr.put("box_id",boxId);
            //EccangOrganization organization = eccangService.getOrganizationByNameEn(product.getUserOrganizationId());
            EccangProduct eccangProduct = new EccangProduct();
            eccangProduct.setPdDeclarationStatement(product.getId());
            eccangProduct.setProductTitle(product.getProductName());
            //如果有供应商产品编码和国家代码都不为空的时候才去设置SKU
            //获取国家代码字典value
            Integer countryCode = product.getCountryCode();
            if (ObjUtil.isNotEmpty(countryCode)){
                //将字典value转换为label
                DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.COUNTRY_CODE, String.valueOf(countryCode));
                if (StrUtil.isNotBlank(product.getSupplierProductCode())){
                    eccangProduct.setProductTitleEn(product.getSupplierProductCode() + "-" + getProductStatus(dictData.getLabel()));
                    eccangProduct.setProductSku(product.getSupplierProductCode() + "-" + getProductStatus(dictData.getLabel()));
                }
            }
            eccangProduct.setProductWeight(product.getPackageWeight());
            eccangProduct.setProductWidth(product.getPackageWidth());
            eccangProduct.setProductLength(product.getPackageLength());
            eccangProduct.setProductHeight(product.getPackageHeight());
            eccangProduct.setProductMaterial(product.getProductMaterial());
            eccangProduct.setPdNetWeight(product.getProductWeight());
            //erp是毫米，这里需要除100变换成厘米
            eccangProduct.setPdNetLength(product.getProductLength()/100);
            eccangProduct.setPdNetWidth(product.getProductWidth()/100);
            eccangProduct.setPdNetHeight(product.getProductHeight()/100);
            //字典数据转换
            Integer declaredValueCurrencyCode = product.getDeclaredValueCurrencyCode();
            Integer purchasePriceCurrencyCode = product.getPurchasePriceCurrencyCode();
            if (ObjUtil.isNotEmpty(declaredValueCurrencyCode)){
                DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, String.valueOf(declaredValueCurrencyCode));
                eccangProduct.setPdDeclareCurrencyCode(dictData.getLabel());
            }
            if (ObjUtil.isNotEmpty(purchasePriceCurrencyCode)){
                DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, String.valueOf(purchasePriceCurrencyCode));
                eccangProduct.setCurrencyCode(dictData.getLabel());
            }
            eccangProduct.setProductPurchaseValue(product.getProductPurchaseValue());
            eccangProduct.setFboTaxRate(product.getTaxRate());
            eccangProduct.setPdOverseaTypeCn(product.getDeclaredType());

            eccangProduct.setProductImgUrlList(Collections.singletonList(product.getProductImageUrl()));
            eccangProduct.setHsCode(product.getHscode());
            eccangProduct.setDefaultSupplierCode("默认供应商");
            //物流属性
            Integer logisticAttribute = product.getLogisticAttribute();
            if (ObjUtil.isNotEmpty(logisticAttribute)){
                eccangProduct.setLogisticAttribute(String.valueOf(logisticAttribute));
            }
            //销售状态默认是在线产品,对应的no是2
            eccangProduct.setSaleStatus(2);
            eccangProduct.setProductDeclaredValue(product.getDeclaredValue());
            eccangProduct.setPdOverseaTypeEn(product.getDeclaredTypeEn());
            //设置产品创建人部门名称
            MapUtils.findAndThen(userMap, Long.parseLong(product.getProductCreatorId()),
                    user -> eccangProduct.setUserOrganizationId(eccangService.getOrganizationByNameEn(String.valueOf(product.getProductDeptId())).getId()));
            //获取产品部门的源关系
            TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(product.getProductDeptId());
            //判断集合是否为空、0、大于3
            if (CollectionUtil.isEmpty(deptTreeLevel) || deptTreeLevel.size() >3){
                throw new RuntimeException("品类部门信息异常，请联系管理员，检查erp中产品资料库中的部门信息");
            }
            //移除第一个元素
            deptTreeLevel.pollFirst();
            //设置品类
            int index = 1;
            for (DeptLevelRespDTO deptLevelRespDTO : deptTreeLevel){
                //根据循环分别设置到procutCategoryNameEn和procutCategoryName
                //通过反射获取属性名，并设置值
                Field procutCategoryNameField = ReflectUtil.getField(EccangProduct.class, "procutCategoryName"+ index);
                ReflectUtil.setFieldValue(eccangProduct,procutCategoryNameField, deptLevelRespDTO.getDeptName());
                Field procutCategoryNameEnField = ReflectUtil.getField(EccangProduct.class, "procutCategoryNameEn"+ index);
                ReflectUtil.setFieldValue(eccangProduct,procutCategoryNameEnField, deptLevelRespDTO.getDeptName());
                index += 1;
            }

            allEccangProducts.add(eccangProduct);
        }
        return allEccangProducts;
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
        if (!Objects.equals(deptParentId, TENANT_ID_DEFAULT)) {
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
