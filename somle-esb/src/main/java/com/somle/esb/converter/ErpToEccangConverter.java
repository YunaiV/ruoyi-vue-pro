package com.somle.esb.converter;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.DeptServiceImpl;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangOrganization;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.erp.model.ErpProduct;
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.product.ErpStyleSku;
import com.somle.erp.repository.ErpProductRepository;
import com.somle.erp.service.ErpDepartmentService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.somle.esb.enums.ErrorCodeConstants.DEPT_LEVEL_ERROR;

@Service
public class ErpToEccangConverter {
    @Autowired
    ErpDepartmentService erpDepartmentService;

    @Autowired
    EccangService eccangService;
    @Autowired
    private ErpProductRepository erpProductRepository;

    @Autowired
    DeptService deptService;

    public EccangProduct toEccang(ErpCountrySku erpCountrySku) {
        ErpStyleSku erpStyleSku = erpCountrySku.getStyleSku();
        Long deptId = erpStyleSku.getSaleDepartmentId();
        EccangProduct product = new EccangProduct();
        List<ErpDepartment> path = erpDepartmentService.getDepartmentParents(deptId).toList();
        Collections.reverse(path);
        // Integer level = path.size() - 1;
        EccangOrganization organization = eccangService.getOrganizationByNameEn(path.get(2).getId().toString());

        product.setProductSku(erpCountrySku.getCountrySku());
        product.setProductTitle(erpStyleSku.getNameZh());
        product.setProductTitleEn(erpStyleSku.getNameEn());

        product.setProductImgUrlList(erpStyleSku.getImageUrlList());

        product.setPdNetWeight(erpStyleSku.getWeight());
        product.setPdNetLength(erpStyleSku.getLength());
        product.setPdNetWeight(erpStyleSku.getWidth());
        product.setPdNetHeight(erpStyleSku.getHeight());
        product.setProductWeight(erpStyleSku.getPackageWeight());
        product.setProductLength(erpStyleSku.getPackageLength());
        product.setProductWidth(erpStyleSku.getPackageWidth());
        product.setProductHeight(erpStyleSku.getPackageHeight());
        product.setProductMaterial(erpStyleSku.getMaterialZh());
        product.setMaterialEn(erpStyleSku.getMaterialEn());


        product.setProductPurchaseValue(erpStyleSku.getPurchasePrice());
        product.setCurrencyCode(erpStyleSku.getPurchasePriceCurrencyCode());
        product.setDefaultSupplierCode(erpStyleSku.getDefaultSupplierCode());

        product.setSaleStatus(erpStyleSku.getSaleStatus());

        product.setLogisticAttribute(erpCountrySku.getLogisticAttribute());
        product.setHsCode(erpCountrySku.getHscode());
        product.setProductDeclaredValue(erpCountrySku.getDeclaredValue());
        product.setPdDeclareCurrencyCode(erpCountrySku.getDeclaredValueCurrencyCode());
        product.setPdOverseaTypeCn(erpCountrySku.getDeclaredTypeZh());
        product.setPdOverseaTypeEn(erpCountrySku.getDeclaredTypeEn());
        product.setFboTaxRate(erpCountrySku.getExportCustomTaxRate());
        product.setPdDeclarationStatement(erpCountrySku.getImportCustomTaxRate().toString());



        try {
            EccangCategory category1 = eccangService.getCategoryByNameEn(path.get(1).getId().toString());
            product.setProductCategoryId1(category1.getPcId());
            EccangCategory category2 = eccangService.getCategoryByNameEn(path.get(2).getId().toString());
            product.setProductCategoryId2(category2.getPcId());
            EccangCategory category3 = eccangService.getCategoryByNameEn(deptId.toString());
            product.setProductCategoryId3(category3.getPcId());
        } catch (Exception e) {
        }

        product.setUserOrganizationId(organization.getId());
        return product;
    }

    public List<EccangProduct> toEccang(){
        List<ErpProduct> allProducts = erpProductRepository.findAllProducts();
        List<EccangProduct> allEccangProducts = new ArrayList<>();
        for (ErpProduct product : allProducts){
            EccangOrganization organization = eccangService.getOrganizationByNameEn("HCD家居事业部");
            EccangProduct eccangProduct = new EccangProduct();
            BeanUtils.copyProperties(product,eccangProduct);
            eccangProduct.setPdDeclarationStatement(product.getRuleId());
            eccangProduct.setProductTitleEn(product.getProductSku());
            eccangProduct.setPdDeclareCurrencyCode("USD");
            eccangProduct.setProductDeclaredValue(99999.9F);
            eccangProduct.setLogisticAttribute("1");
            eccangProduct.setProductImgUrlList(Collections.singletonList(product.getImageUrl()));

           /* try {
                EccangCategory category1 = eccangService.getCategoryByNameEn(path.get(1).getId().toString());
                product.setProductCategoryId1(category1.getPcId());
                EccangCategory category2 = eccangService.getCategoryByNameEn(path.get(2).getId().toString());
                product.setProductCategoryId2(category2.getPcId());
                EccangCategory category3 = eccangService.getCategoryByNameEn(deptId.toString());
                product.setProductCategoryId3(category3.getPcId());
            } catch (Exception e) {
            }*/


            eccangProduct.setUserOrganizationId(organization.getId());
            allEccangProducts.add(eccangProduct);
        }
        return allEccangProducts;
    }

    public EccangCategory toEccang(String deptId) {
        //从erp中获取部门信息
        DeptDO dept = deptService.getDept(Long.valueOf(deptId));
        //获取部门名称
        String deptName = dept.getName();
        String actionType = "ADD";
        Integer id = null;
        Integer pid;
        try {
            EccangCategory category = eccangService.getCategoryByName(deptName);
            id = category.getPcId();
            actionType = "EDIT";
        } catch (Exception e) {
        }
        try {
            //获取父级名称
            String parentName = deptService.getParentNameById(Long.valueOf(deptId));
            pid = eccangService.getCategoryByName(parentName).getPcId();
        } catch (Exception e) {
            //由于未找到父id，它自己就是顶级层级
            pid = 0;
        }
        Integer deptLevel = deptService.getDeptLevel(Long.valueOf(deptId));
        --deptLevel;
        //如果层级为0，则忽略并抛出异常通知操作人员
        ThrowUtil.ifThrow(Objects.equals(deptLevel, 0), DEPT_LEVEL_ERROR);
        //当层级大于3，则自动修正为3
        deptLevel = deptLevel > 3 ? 3 : deptLevel;
        return EccangCategory.builder()
            .actionType(actionType)
            .pcId(id)
            .pcName(deptName)
            .pcNameEn(deptId)
            .pcPid(pid)
            .pcLevel(deptLevel)
            .build();
    }
}
