package cn.iocoder.yudao.module.erp.service.logistic.customrule;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRulePageReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.erp.controller.admin.product.vo.product.ErpProductRespVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.CUSTOM_RULE_NOT_EXISTS;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE;

/**
 * ERP 海关规则 Service 实现类
 *
 * @author 索迈管理员
 */
@Slf4j
@Service
@Validated
public class ErpCustomRuleServiceImpl implements ErpCustomRuleService {
    @Resource
    private MessageChannel erpCustomRuleChannel;

    @Resource
    ErpCustomRuleMapper customRuleMapper;

    @Resource
    ErpProductService erpProductService;


    @Override
    public Long createCustomRule(ErpCustomRuleSaveReqVO createReqVO) {
        //判断国别+产品编码是否重复
        validateExist(null, createReqVO.getCountryCode(), createReqVO.getProductId());
        //插入
        ErpCustomRuleDO customRule = BeanUtils.toBean(createReqVO, ErpCustomRuleDO.class);
        //填充barCode(产品sku),根据产品id
        ThrowUtil.ifSqlThrow(customRuleMapper.insert(customRule
        ), DB_INSERT_ERROR);
        Long id = customRule.getId();
        //同步数据
        var dtos = customRuleMapper.selectProductAllInfoListByCustomRuleId(id);
        erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos).build());
        // 返回
        return id;
    }

    @Override
    public void updateCustomRule(ErpCustomRuleSaveReqVO updateReqVO) {
        Long id = updateReqVO.getId();
        //判断国别+产品编码是否重复
        validateExist(id, updateReqVO.getCountryCode(), updateReqVO.getProductId());
        // 校验存在
        validateCustomRuleExists(id);
        // 更新
        ErpCustomRuleDO updateObj = BeanUtils.toBean(updateReqVO, ErpCustomRuleDO.class);
        ThrowUtil.ifSqlThrow(customRuleMapper.updateById(updateObj), DB_UPDATE_ERROR);
        //同步数据
        var dtos = customRuleMapper.selectProductAllInfoListByCustomRuleId(id);
        erpCustomRuleChannel.send(MessageBuilder.withPayload(dtos).build());
    }

    @Override
    public void deleteCustomRule(Long id) {
        // 校验存在
        validateCustomRuleExists(id);
        // 删除
        customRuleMapper.deleteById(id);
    }

    private void validateCustomRuleExists(Long id) {
        if (customRuleMapper.selectById(id) == null) {
            throw exception(CUSTOM_RULE_NOT_EXISTS);
        }
    }

    @Override
    public ErpCustomRuleDO getCustomRule(Long id) {
        return customRuleMapper.selectById(id);
    }

    @Override
    public PageResult<ErpCustomRuleDO> getCustomRulePage(ErpCustomRulePageReqVO pageReqVO) {
        return customRuleMapper.selectPage(pageReqVO);
    }

    private void validateExist(Long id, Integer countryCode, Long productId) {
        //TODO 城市code+产品id是否存在,校验-wdy
        ErpCustomRuleDO erpCustomRuleDO = customRuleMapper.getCustomRuleByCountryCodeAndProductId(countryCode, productId);
        if (erpCustomRuleDO == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        if (id == null) {
            throw exception(NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE);
        }
        if (!erpCustomRuleDO.getId().equals(id)) {
            throw exception(NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE);
        }
    }


    //DO 转 DTO
    @Override
    public ErpCustomRuleDTO convertToDTO(ErpCustomRuleDO customRuleDO) {
        // 使用 BeanUtils 进行字段基本映射
        ErpCustomRuleDTO dto = BeanUtils.toBean(customRuleDO, ErpCustomRuleDTO.class);
        // 根据产品ID查询产品信息，填充 DTO
        ErpProductRespVO product = erpProductService.getProduct(customRuleDO.getProductId());
        dto.setProductName(product.getName());           // 设置产品名称
        dto.setProductImageUrl(product.getPrimaryImageUrl());  // 设置产品图片地址
        dto.setProductId(product.getId());               // 设置产品ID1
        dto.setProductDeptId(product.getDeptId());       // 设置产品部门ID
        dto.setProductWeight(product.getWeight() != null ? product.getWeight().floatValue() : null); // 设置产品重量
        dto.setProductLength(product.getLength() != null ? product.getLength().floatValue() : null); // 设置产品基础长度
        dto.setProductWidth(product.getWidth() != null ? product.getWidth().floatValue() : null);   // 设置产品基础宽度
        dto.setProductHeight(product.getHeight() != null ? product.getHeight().floatValue() : null); // 设置产品基础高度
        dto.setProductMaterial(product.getMaterial());  // 设置产品材料

        // 其他字段的映射
        dto.setDeclaredValue(customRuleDO.getDeclaredValue() != null ? customRuleDO.getDeclaredValue().floatValue() : null); // 申报价值
        dto.setDeclaredType(customRuleDO.getDeclaredType());   // 申报品名CN
        dto.setDeclaredTypeEn(customRuleDO.getDeclaredTypeEn()); // 申报品名EN
        dto.setTaxRate(customRuleDO.getTaxRate() != null ? customRuleDO.getTaxRate().floatValue() : null); // 税率
        dto.setHscode(customRuleDO.getHscode());  // HS编码
        dto.setLogisticAttribute(customRuleDO.getLogisticAttribute()); // 物流属性
        dto.setFbaBarCode(customRuleDO.getFbaBarCode()); // 条形码

        // 如果有包装相关的信息可以映射
        dto.setPackageWeight(product.getPackageWeight() != null ? product.getPackageWeight().floatValue() : null);  // 产品的包装重量
        dto.setPackageLength(product.getPackageLength() != null ? product.getPackageLength().floatValue() : null);  // 产品的包装长度
        dto.setPackageWidth(product.getPackageWidth() != null ? product.getPackageWidth().floatValue() : null);    // 产品的包装宽度
        dto.setPackageHeight(product.getPackageHeight() != null ? product.getPackageHeight().floatValue() : null);  // 产品的包装高度

        return dto;
    }

    // DO List 转 DTO List
    @Override
    public List<ErpCustomRuleDTO> convertToDTOList(List<ErpCustomRuleDO> customRuleDOList) {
        return customRuleDOList.stream()
            .map(this::convertToDTO)  // 使用 map 方法调用 convertToDTO 方法转换每一个 DO
            .collect(Collectors.toList());  // 收集成列表返回
    }


}