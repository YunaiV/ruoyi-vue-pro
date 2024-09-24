package com.somle.esb.converter;

import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangOrganization;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.erp.model.product.ErpCountrySku;
import com.somle.erp.model.ErpDepartment;
import com.somle.erp.model.product.ErpStyleSku;
import com.somle.erp.service.ErpDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ErpToEccangConverter {
    @Autowired
    ErpDepartmentService erpDepartmentService;

    @Autowired
    EccangService eccangService;

    public EccangProduct toEccang(ErpCountrySku erpCountrySku) {
        ErpStyleSku erpStyleSku = erpCountrySku.getStyleSku();
        Long deptId = erpStyleSku.getSaleDepartmentId();
        EccangProduct product = new EccangProduct();
        List<ErpDepartment> path = erpDepartmentService.getDepartmentParents(deptId).toList().reversed();
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

    public EccangCategory toEccang(ErpDepartment erpDepartment) {
        String actionType = "ADD";
        Integer id = null;
        Integer pid = null;
        Integer level = erpDepartment.getLevel();
        level = level > 3 ? 3 : level;
        try {
            id = eccangService.getCategoryByNameEn(erpDepartment.getId().toString()).getPcId();
            actionType = "EDIT";
        } catch (Exception e) {
        }
        try {
            pid = eccangService.getCategoryByNameEn(erpDepartmentService.getParent(erpDepartment, level - 1).getId().toString()).getPcId();
        } catch (Exception e) {
            if (level != 1) {
                throw new RuntimeException("pid not found for " + erpDepartment.toString() + " Detail: " + e.toString());
            }
        }
        return EccangCategory.builder()
            .actionType(actionType)
            .pcId(id)
            .pcName(erpDepartment.getNameZh())
            .pcNameEn(String.valueOf(erpDepartment.getId()))
            .pcPid(pid)
            .pcLevel(level)
            .build();
    }
}
