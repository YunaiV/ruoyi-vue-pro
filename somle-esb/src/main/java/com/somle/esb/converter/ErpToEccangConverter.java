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
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptLevelRespDTO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangProduct;
import com.somle.eccang.service.EccangService;
import com.somle.esb.enums.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.number.LengthUtils.mmToCmAsFloat;
import static com.somle.esb.enums.ErrorCodeConstants.DEPT_LEVEL_ERROR;
import static com.somle.esb.util.ConstantConvertUtils.getCountrySuffix;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpToEccangConverter {
    private final EccangService eccangService;
    private final DeptApi deptApi;
    private final AdminUserApi userApi;
    private final DictDataApi dictDataApi;
//    private final ErpSupplierProductService erpSupplierProductService;

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
     * @param customRuleDTO 海关规则DTO
     * @param userMap       用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct customRuleToProduct(ErpCustomRuleDTO customRuleDTO, Map<Long, AdminUserRespDTO> userMap) {
        EccangProduct eccangProduct = setDefaultValue(new EccangProduct());
        ErpProductDTO productDTO = customRuleDTO.getProductDTO();
        eccangProduct.setPdDeclarationStatement(String.valueOf(customRuleDTO.getId()));

        Integer countryCode = customRuleDTO.getCountryCode();
        String countrySuffix = ObjectUtil.isNotEmpty(countryCode)
            ? getCountrySuffix(dictDataApi.getDictData(DictTypeConstants.COUNTRY_CODE, String.valueOf(countryCode)).getLabel())
            : "";
        String barCode = productDTO.getBarCode();
        String suffix = countrySuffix.isEmpty() ? "" : "-" + countrySuffix;
        eccangProduct.setProductTitle(productDTO.getName() + suffix);
        eccangProduct.setProductTitleEn(CharSequenceUtil.isNotBlank(barCode) ? barCode + suffix : barCode);
        eccangProduct.setProductSku(CharSequenceUtil.isNotBlank(barCode) ? barCode + suffix : barCode);
        //申报币种
        Optional.ofNullable(customRuleDTO.getDeclaredValueCurrencyCode())
            .map(String::valueOf)
            .map(code -> dictDataApi.getDictData(DictTypeConstants.CURRENCY_CODE, code))
            .filter(dictData -> ObjectUtil.isNotEmpty(dictData.getLabel()))
            .ifPresent(dictData -> eccangProduct.setPdDeclareCurrencyCode(dictData.getLabel()));

        eccangProduct.setProductDeclaredValue(customRuleDTO.getDeclaredValue().floatValue());
        eccangProduct.setPdOverseaTypeEn(customRuleDTO.getDeclaredTypeEn());

        this.setProductSizeAndWeight(eccangProduct, productDTO, (product, dto) -> {
        });
        //其他产品属性
        Optional.ofNullable(customRuleDTO.getTaxRate()).ifPresent(taxRate -> eccangProduct.setTaxRate(taxRate.floatValue()));
        eccangProduct.setPdOverseaTypeCn(customRuleDTO.getDeclaredType());
//        eccangProduct.setProductImgUrlList(Collections.singletonList(productDTO.getPrimaryImageUrl()));//不同步imgUrlList
//        eccangProduct.setHsCode(customRuleDTO.getHscode());//不同步hsCode
        // 物流属性
        Integer logisticAttribute = customRuleDTO.getLogisticAttribute();
        eccangProduct.setLogisticAttribute(Optional.ofNullable(logisticAttribute)
            .map(String::valueOf)
            .orElse(null));

        this.setProductCategoriesAndOrganizationId(eccangProduct, productDTO, userMap);
        //产品id
//        eccangProduct.setDesc(String.valueOf(customRuleDTO.getProductId()));//Desc->productId
        return tearDown(eccangProduct);
    }

    /**
     * 将单个ERP产品转换为Eccang产品。
     *
     * @param productDTO ERP产品对象
     * @param userMap    用户信息映射
     * @return 转换后的Eccang产品对象
     */
    private EccangProduct convertByErpProductDTO(ErpProductDTO productDTO, Map<Long, AdminUserRespDTO> userMap) {
        EccangProduct eccangProduct = setDefaultValue(new EccangProduct());
        //SKU和标题
        eccangProduct.setProductTitle(productDTO.getName());
        eccangProduct.setProductTitleEn(productDTO.getBarCode());
        eccangProduct.setProductSku(productDTO.getBarCode());
        this.setProductSizeAndWeight(eccangProduct, productDTO, (product, dto) -> {
        });
        this.setProductCategoriesAndOrganizationId(eccangProduct, productDTO, userMap);
        return tearDown(eccangProduct);
    }

    /**
     * 转换前置操作-设置eccangProduct默认值
     */
    private EccangProduct setDefaultValue(EccangProduct eccangProduct) {
        eccangProduct.setDefaultSupplierCode(
            ObjectUtils.defaultIfNull(eccangProduct.getDefaultSupplierCode(), "默认供应商")
        );
        //销售状态和声明价值
        eccangProduct.setSaleStatus(
            ObjectUtils.defaultIfNull(eccangProduct.getSaleStatus(), 2) // 销售状态
        );
        eccangProduct.setActionType(
            ObjectUtils.defaultIfNull(eccangProduct.getActionType(), "ADD") //默认新增
        );
        eccangProduct.setCurrencyCode(
            ObjectUtils.defaultIfNull(eccangProduct.getCurrencyCode(), "RMB") // 默认币种代码RMB
        );
        eccangProduct.setProductPrice(
            ObjectUtils.defaultIfNull(eccangProduct.getProductPrice(), 0F) // 默认价格为 0.0
        );
        eccangProduct.setPdDeclareCurrencyCode(
            ObjectUtils.defaultIfNull(eccangProduct.getPdDeclareCurrencyCode(), "USD") // 默认申报币种USD
        );
        eccangProduct.setProductDeclaredValue(
            ObjectUtils.defaultIfNull(eccangProduct.getProductDeclaredValue(), 0.001F));// 申报价值
        eccangProduct.setPdOverseaTypeEn(
            ObjectUtils.defaultIfNull(eccangProduct.getPdOverseaTypeEn(), "无")); //申报品名英文
        return eccangProduct;
    }


    /**
     * 转换后置操作-判断是新增还是修改
     */
    private EccangProduct tearDown(EccangProduct eccangProduct) {
        //根据sku从eccang中获取产品，存在->修改,不存在->新增
        if (ObjUtil.isNotEmpty(eccangService.getProduct(eccangProduct.getProductSku()))) {
            eccangProduct.setActionType("EDIT");
            //如果是修改就要上传默认采购单价
            //TODO 后续有变更，请修改
            eccangProduct.setProductPurchaseValue(0.001F);
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


    /**
     * 设置ERP产品的分类信息和组织id
     * 通过部门ID获取部门层级，并设置到Eccang产品的分类字段。
     *
     * @param eccangProduct Eccang产品对象
     * @param productDTO    ERP产品DTO
     */
    private void setProductCategoriesAndOrganizationId(EccangProduct eccangProduct, ErpProductDTO productDTO, Map<Long, AdminUserRespDTO> userMap) {
        // 产品创建人部门名称
        MapUtils.findAndThen(userMap, Long.parseLong(productDTO.getCreator()),
            user -> eccangProduct.setUserOrganizationId(eccangService.getOrganizationByNameEn(String.valueOf(productDTO.getDeptId())).getId()));//TODO-需要检查erp中产品资料库中的部门信息不为空
        // 品类
        TreeSet<DeptLevelRespDTO> deptTreeLevel = deptApi.getDeptTreeLevel(productDTO.getDeptId());
        if (CollectionUtil.isEmpty(deptTreeLevel) || deptTreeLevel.size() > 3) {
            throw new RuntimeException("品类部门信息转换异常，请联系管理员，检查erp中产品资料库中的部门信息");
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
    }

    // 设置产品尺寸和重量
    private void setProductSizeAndWeight(EccangProduct eccangProduct, ErpProductDTO productDTO, BiConsumer<EccangProduct, ErpProductDTO> consumer) {
        // 设置产品的材料
        eccangProduct.setProductMaterial(productDTO.getMaterial());

        // 设置产品的净重（单位：kg），从ERP产品DTO中获取重量，并转换为浮动数值
        eccangProduct.setPdNetWeight(productDTO.getWeight().floatValue());

        // 设置产品的净尺寸（单位：cm），从ERP产品DTO中获取长度、宽度和高度，并将其从毫米转换为厘米
        eccangProduct.setPdNetLength(mmToCmAsFloat(productDTO.getLength()));  // 净长
        eccangProduct.setPdNetWidth(mmToCmAsFloat(productDTO.getWidth()));    // 净宽
        eccangProduct.setPdNetHeight(mmToCmAsFloat(productDTO.getHeight()));  // 净高

        // 设置产品的包装尺寸（单位：cm），同样从ERP产品DTO中获取包装长度、宽度和高度，并进行单位转换
        eccangProduct.setProductLength(mmToCmAsFloat(productDTO.getPackageLength()));  // 包装长
        eccangProduct.setProductWidth(mmToCmAsFloat(productDTO.getPackageWidth()));    // 包装宽
        eccangProduct.setProductHeight(mmToCmAsFloat(productDTO.getPackageHeight()));  // 包装高
        eccangProduct.setProductWeight(
            Optional.ofNullable(productDTO.getPackageWeight())
                .map(weight -> weight.setScale(3, RoundingMode.HALF_UP).floatValue())//保留三位小数
                .orElse(null)
        );
        consumer.accept(eccangProduct, productDTO);//自定义尺寸和重量
    }
}
