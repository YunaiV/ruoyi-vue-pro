package com.somle.esb.converter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.collection.MapUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.erp.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.controller.admin.purchase.vo.ErpSupplierProductPageReqVO;
import cn.iocoder.yudao.module.erp.service.purchase.ErpSupplierProductService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.esb.enums.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.somle.esb.enums.ErrorCodeConstants.DEPT_LEVEL_ERROR;
import static com.somle.esb.util.ConstantConvertUtils.getCountrySuffix;
import static com.somle.framework.common.util.number.LengthUtils.mmToCmAsFloat;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpToEccangConverter {
    private final EccangService eccangService;
    private final DeptApi deptApi;
    private final AdminUserApi userApi;
    private final DictDataApi dictDataApi;
    private final ErpSupplierProductService erpSupplierProductService;

    /**
     * 将ERP产品列表转换为完整的Eccang产品列表。
     *
     * @param customRuleDTOs ERP产品列表
     * @return 转换后的Eccang产品列表
     */
    public List<EccangProduct> convertByErpCustomDTOs(List<ErpCustomRuleDTO> customRuleDTOs) {
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(customRuleDTOs, erpCustomRuleDTO -> Long.parseLong(erpCustomRuleDTO.getProductDTO().getCreator())));
        return customRuleDTOs.stream()
            .map(product -> customRuleToProduct(product, userMap))
            .toList();
    }

    /**
     * 将ERP产品列表转换为简化的Eccang产品列表。
     *
     * @param productDTOs ERP产品列表
     * @return 转换后的简化版Eccang产品列表
     */
    public List<EccangProduct> convertByErpProducts(List<ErpProductDTO> productDTOs) {
        Map<Long, AdminUserRespDTO> userMap = userApi.getUserMap(convertSet(productDTOs, product -> Long.parseLong(product.getCreator())));
        return productDTOs.stream()
            .map(product -> convertByErpProductDTO(product, userMap))
            .collect(Collectors.toList());
    }

    /**
     * 将单个ERP产品转换为Eccang产品。
     *
     * @param customRuleDTO ERP产品对象
     * @param userMap       用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct customRuleToProduct(ErpCustomRuleDTO customRuleDTO, Map<Long, AdminUserRespDTO> userMap) {
        EccangProduct eccangProduct = new EccangProduct();
        ErpProductDTO productDTO = customRuleDTO.getProductDTO();
        eccangProduct.setPdDeclarationStatement(String.valueOf(customRuleDTO.getId()));

        Integer countryCode = customRuleDTO.getCountryCode();
        String countrySuffix = ObjectUtil.isNotEmpty(countryCode)
            ? getCountrySuffix(dictDataApi.getDictData(DictTypeConstants.COUNTRY_CODE, String.valueOf(countryCode)).getLabel())
            : "";

        String barCode = productDTO.getBarCode();
        String productName = productDTO.getName();
        String suffix = countrySuffix.isEmpty() ? "" : "-" + countrySuffix;

        eccangProduct.setProductTitle(productName + suffix);
        eccangProduct.setProductTitleEn(CharSequenceUtil.isNotBlank(barCode) ? barCode + suffix : barCode);
        eccangProduct.setProductSku(CharSequenceUtil.isNotBlank(barCode) ? barCode + suffix : barCode);
        // 设置货币代码
        Optional.ofNullable(customRuleDTO.getDeclaredValueCurrencyCode())
            .map(String::valueOf)
            .map(code -> dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, code))
            .filter(dictData -> ObjectUtil.isNotEmpty(dictData.getLabel()))
            .ifPresent(dictData -> eccangProduct.setPdDeclareCurrencyCode(dictData.getLabel()));

//        Integer purchasePriceCurrencyCode = customRuleDTO.getPurchasePriceCurrencyCode();
//        if (ObjUtil.isNotEmpty(purchasePriceCurrencyCode)) {
//            DictDataRespDTO dictData = dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, String.valueOf(purchasePriceCurrencyCode));
//            eccangProduct.setCurrencyCode(dictData.getLabel());
//        }
        eccangProduct.setProductDeclaredValue(customRuleDTO.getDeclaredValue().floatValue());
        eccangProduct.setPdOverseaTypeEn(customRuleDTO.getDeclaredTypeEn());

        // 设置产品尺寸和重量
        eccangProduct.setProductMaterial(productDTO.getMaterial());
        eccangProduct.setProductWeight(productDTO.getPackageWeight().floatValue());
        eccangProduct.setPdNetWeight(productDTO.getWeight().floatValue());

        //包装长-宽-高
        eccangProduct.setProductWidth(mmToCmAsFloat(productDTO.getPackageWidth()));
        eccangProduct.setProductLength(mmToCmAsFloat(productDTO.getPackageLength()));
        eccangProduct.setProductHeight(mmToCmAsFloat(productDTO.getPackageHeight()));
        //长-宽-高
        eccangProduct.setPdNetLength(mmToCmAsFloat(productDTO.getLength()));
        eccangProduct.setPdNetWidth(mmToCmAsFloat(productDTO.getWidth()));
        eccangProduct.setPdNetHeight(mmToCmAsFloat(productDTO.getHeight()));

        // 设置其他产品属性
        Optional.ofNullable(customRuleDTO.getTaxRate()).ifPresent(taxRate -> eccangProduct.setTaxRate(taxRate.floatValue()));
        eccangProduct.setPdOverseaTypeCn(customRuleDTO.getDeclaredType());
//        eccangProduct.setProductImgUrlList(Collections.singletonList(productDTO.getPrimaryImageUrl()));
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
        MapUtils.findAndThen(userMap, Long.parseLong(productDTO.getCreator()),
            user -> eccangProduct.setUserOrganizationId(eccangService.getOrganizationByNameEn(String.valueOf(productDTO.getDeptId())).getId()));//TODO-需要检查erp中产品资料库中的部门信息不为空

        // 设置品类
        TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(productDTO.getDeptId());
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
        //设置产品id
        eccangProduct.setDesc(String.valueOf(customRuleDTO.getProductId()));//Desc->productId


        //设置默认
        eccangProduct.setActionType("ADD");
        EccangProduct eccangServiceProduct = eccangService.getProduct(eccangProduct.getProductSku());
        //根据sku从eccang中获取产品，如果产品不为空，则表示已存在，操作则变为修改
        if (ObjUtil.isNotEmpty(eccangServiceProduct)) {
            eccangProduct.setActionType("EDIT");
            //如果是修改就要上传默认采购单价
            //TODO 后续有变更，请修改
            eccangProduct.setProductPurchaseValue(0.001F);
        }
        log.debug(eccangProduct.toString());
        //用product_id在供应商产品里面查，使用查到的第一个价格
        // 1. 设置默认值
        eccangProduct.setCurrencyCode(
            ObjectUtils.defaultIfNull(eccangProduct.getCurrencyCode(), "1") // 默认 CNY
        );
        eccangProduct.setProductPrice(
            ObjectUtils.defaultIfNull(eccangProduct.getProductPrice(), 0f) // 默认价格为 0.0
        );

        // 2. 获取产品并处理价格
        erpSupplierProductService.getSupplierProductPage(
                new ErpSupplierProductPageReqVO().setProductId(Long.valueOf(eccangProduct.getDesc()))
            )
            .getList().stream()
            .findFirst()
            .ifPresent(erpSupplierProductDO -> {
                // 设置货币单位
                Optional.ofNullable(erpSupplierProductDO.getPurchasePriceCurrencyCode())
                    .map(String::valueOf) // 将 Integer 转换为字符串
                    .filter(StringUtils::isNotBlank)
                    .ifPresent(eccangProduct::setCurrencyCode);

                // 设置价格，并确保价格为 BigDecimal 类型，避免转换不一致
                Optional.ofNullable(erpSupplierProductDO.getPurchasePrice())
                    .map(BigDecimal::valueOf)
                    .map(price -> price.setScale(2, RoundingMode.HALF_UP).floatValue())
                    .ifPresent(eccangProduct::setProductPrice);
            });
        return eccangProduct;
    }

    /**
     * 将单个ERP产品转换为Eccang产品。
     *
     * @param product ERP产品对象
     * @param userMap 用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct convertByErpProductDTO(ErpProductDTO product, Map<Long, AdminUserRespDTO> userMap) {
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
        eccangProduct.setPdNetWeight(product.getWeight().floatValue());
        //eccangProduct.setProductImgUrlList(Collections.singletonList(product.getPrimaryImageUrl()));产品图片属于敏感数据，不同步
        eccangProduct.setDefaultSupplierCode("默认供应商");
        //产品基础属性
        eccangProduct.setPdNetLength(mmToCmAsFloat(Float.valueOf(product.getLength())));
        eccangProduct.setPdNetWidth(mmToCmAsFloat(Float.valueOf(product.getWidth())));
        eccangProduct.setPdNetHeight(mmToCmAsFloat(Float.valueOf(product.getHeight())));
        //产品包装属性
        eccangProduct.setProductLength(mmToCmAsFloat(product.getPackageLength()));  // mm -> cm
        eccangProduct.setProductWidth(mmToCmAsFloat(product.getPackageWidth()));    // mm -> cm
        eccangProduct.setProductHeight(mmToCmAsFloat(product.getPackageHeight()));  // mm -> cm

        eccangProduct.setProductWeight(
            product.getPackageWeight() != null
                ? product.getPackageWeight().setScale(3, RoundingMode.HALF_UP).floatValue() // 保留三位小数
                : null // 如果为 null，返回 null
        );

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
        if (categoryByName != null) {
            id = categoryByName.getPcId();
            actionType = "EDIT";
        }
        DeptLevelRespDTO parentDept = getParentDeptLevel(deptLevel, deptTreeLevel);
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
     * @Date 2024/11/25
     * @Param [deptTreeLevel]
     **/
    private DeptLevelRespDTO getParentDeptLevel(Integer deptLevel, TreeSet<DeptLevelRespDTO> deptTreeLevel) {
        if (deptLevel > 3) {
            //向上找父类
            deptTreeLevel.pollLast();
            getParentDeptLevel(--deptLevel, deptTreeLevel);
        } else {
            //因为erp的层级比eccang高一级，所以移除一个
            deptTreeLevel.pollLast();
        }
        return deptTreeLevel.last();
    }
}
