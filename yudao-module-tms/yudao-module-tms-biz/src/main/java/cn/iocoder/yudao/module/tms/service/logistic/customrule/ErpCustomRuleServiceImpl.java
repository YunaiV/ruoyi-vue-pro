package cn.iocoder.yudao.module.tms.service.logistic.customrule;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.enums.enums.DictTypeConstants;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.product.ErpProductApi;
import cn.iocoder.yudao.module.srm.enums.ErpDictTypeConstants;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.ErpCustomRuleApi;
import cn.iocoder.yudao.module.tms.api.logistic.customrule.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.ErpCustomRulePageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.ErpCustomRuleSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.ErpCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule.ErpCustomRuleMapper;
import cn.iocoder.yudao.module.tms.service.logistic.category.product.ErpCustomProductService;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.ErpCustomRuleBO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_BATCH_INSERT_ERROR;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.DB_UPDATE_ERROR;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.system.enums.esb.EsbChannels.ERP_CUSTOM_RULE_CHANNEL;
import static cn.iocoder.yudao.module.tms.enums.ErrorCodeConstants.*;

/**
 * ERP 海关规则 Service 实现类
 *
 * @author 索迈管理员
 */
@Slf4j
@Service
@Validated
public class ErpCustomRuleServiceImpl implements ErpCustomRuleService {
    @Autowired
    ErpCustomRuleMapper customRuleMapper;
    @Resource(name = ERP_CUSTOM_RULE_CHANNEL)
    MessageChannel channel;
    @Autowired
    ErpCustomRuleApi tmsCustomRuleApi;
    @Autowired
    DictDataApi dictDataApi;
    @Autowired
    ErpProductApi erpProductApi;
    @Autowired
    ErpCustomProductService erpCustomProductService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCustomRule(ErpCustomRuleSaveReqVO createReqVO) {
        baseValidator(createReqVO);
        //判断国别+产品编码是否重复
        createReqVO.getCountryCode().forEach(countryCode -> validateExist(null, countryCode, createReqVO.getProductId()));
        List<ErpCustomRuleDO> doList = createReqVO.getCountryCode().stream().map(countryCode -> copyPropertiesIgnoreType(createReqVO, countryCode)).toList();
        validProductHasCustomData(createReqVO);
        //批量添加
        ThrowUtil.ifThrow(!customRuleMapper.insertBatch(doList, doList.size()), DB_BATCH_INSERT_ERROR);
        //同步数据
        List<Long> ids = doList.stream().map(ErpCustomRuleDO::getId).toList();
        this.syncErpCustomRule(ids);
        // 返回
        return (long) ids.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCustomRule(ErpCustomRuleSaveReqVO updateReqVO) {
        baseValidator(updateReqVO);
        Long id = updateReqVO.getId();
        //判断国别+产品编码是否重复
        //获得updateReqVO国别的第一个
        Integer countryCode = updateReqVO.getCountryCode().stream().findFirst().orElseThrow(() -> exception(DB_UPDATE_ERROR));
        validateExist(id, countryCode, updateReqVO.getProductId());
        // 校验存在
        validateCustomRuleExists(id);
        validProductHasCustomData(updateReqVO);
        // 更新
        ErpCustomRuleDO updateObj = copyPropertiesIgnoreType(updateReqVO, countryCode);
        ThrowUtil.ifSqlThrow(customRuleMapper.updateById(updateObj), DB_UPDATE_ERROR);
        //同步数据
        this.syncErpCustomRule(Collections.singletonList(id));
    }

    //验证产品存在海关分类数据
    private void validProductHasCustomData(ErpCustomRuleSaveReqVO updateReqVO) {
//        ThrowUtil.ifThrow(erpProductApi.getProductDto(updateReqVO.getProductId()).getCustomCategoryId() == null, CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);

        //根据产品id查询，如果不存在就异常
        ErpCustomProductDO customProduct = erpCustomProductService.getCustomProductByProductId(updateReqVO.getProductId());
        if (customProduct == null) {
            throw exception(CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);
        }
    }


    //同步海关规则方法
    private void syncErpCustomRule(List<Long> ruleIds) {
        List<ErpCustomRuleDTO> dtos = tmsCustomRuleApi.listCustomRules(ruleIds);
        channel.send(MessageBuilder.withPayload(dtos).build());
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
        if (pageReqVO == null) {
            pageReqVO = new ErpCustomRulePageReqVO();
        }
        return customRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public ErpCustomRuleBO getCustomRuleBOById(@NotNull Long id) {
        //检查规则下的产品是否存在海关分类
//        ThrowUtil.ifThrow(customRuleMapper.selectById(id) == null,CUSTOM_RULE_CATEGORY_ITEM_NOT_EXISTS_BY_PRODUCT_ID);
        ErpCustomRuleDO ruleDO = customRuleMapper.selectById(id);
        if (ruleDO == null) return null;
        if (erpProductApi.getProductDto(ruleDO.getProductId()).getCustomCategoryId() == null) {
            //不存在海关规则->原始table
            return BeanUtils.toBean(ruleDO, ErpCustomRuleBO.class);
        } else {
            //存在->关联查询bo
            return customRuleMapper.getCustomRuleBOById(id);
        }

    }

    @Override
    public List<ErpCustomRuleBO> getCustomRuleBOList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return customRuleMapper.selectCustomRuleBOByIds(ids);
    }

    @Override
    public PageResult<ErpCustomRuleBO> getCustomRuleBOPage(ErpCustomRulePageReqVO pageReqVO) {
        if (pageReqVO == null) {
            pageReqVO = new ErpCustomRulePageReqVO();
        }
        return customRuleMapper.selectBOPage(pageReqVO);
    }

    private void validateExist(Long id, Integer countryCode, Long productId) {
        //校验产品是否存在
        erpProductApi.validProductList(Collections.singleton(productId));
        ErpCustomRuleDO tmsCustomRuleDO = customRuleMapper.getCustomRuleByCountryCodeAndProductId(countryCode, productId);
        if (tmsCustomRuleDO == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的字典类型
        String barCode = erpProductApi.getProductDto(productId).getBarCode();
        String countryDesc = dictDataApi.getDictDataLabel(DictTypeConstants.COUNTRY_CODE, countryCode);
        ThrowUtil.ifThrow(id == null, NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE, barCode + countryDesc);
        ThrowUtil.ifThrow(!tmsCustomRuleDO.getId().equals(id), NO_REPEAT_OF_COUNTRY_CODE_AND_PRODUCT_CODE, barCode + countryDesc);
    }

    private void baseValidator(ErpCustomRuleSaveReqVO vo) {
        //国家
        Optional.ofNullable(vo.getCountryCode())
                .ifPresent(countryCodes -> dictDataApi.validateDictDataList(DictTypeConstants.COUNTRY_CODE,
                        countryCodes.stream()
                                .map(String::valueOf)
                                .collect(Collectors.toSet())));
        //货币
        Optional.ofNullable(vo.getDeclaredValueCurrencyCode()).ifPresent(i -> dictDataApi.validateDictDataList(DictTypeConstants.CURRENCY_CODE, Collections.singleton(String.valueOf(i))));
        //物流属性
        Optional.ofNullable(vo.getLogisticAttribute()).ifPresent(i -> dictDataApi.validateDictDataList(ErpDictTypeConstants.ERP_LOGISTIC_ATTRIBUTE, Collections.singleton(String.valueOf(i))));

    }

    /**
     * 自定义转换VO->DO。比较特殊。
     */
    protected ErpCustomRuleDO copyPropertiesIgnoreType(ErpCustomRuleSaveReqVO source, Integer countryCode) {
        //手动映射
        source.setCountryCode(null);
        return BeanUtils.toBean(source, ErpCustomRuleDO.class).setCountryCode(countryCode);
    }
}