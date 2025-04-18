package cn.iocoder.yudao.module.tms.dal.mysql.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.logistic.customrule.vo.TmsCustomRulePageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.TmsCustomCategoryDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.item.TmsCustomCategoryItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.category.product.TmsCustomProductDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.logistic.customrule.TmsCustomRuleDO;
import cn.iocoder.yudao.module.tms.service.logistic.customrule.bo.TmsCustomRuleBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Tms 海关规则 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface TmsCustomRuleMapper extends BaseMapperX<TmsCustomRuleDO> {
    //规则->产品
    default MPJLambdaWrapper<TmsCustomRuleDO> bindQueryWrapper(TmsCustomRulePageReqVO reqVO) {
        return new MPJLambdaWrapperX<TmsCustomRuleDO>()
            .selectAll(TmsCustomRuleDO.class)  // 选择所有列
            .eqIfPresent(TmsCustomRuleDO::getCountryCode, reqVO.getCountryCode())  // 国家编码
            .eqIfPresent(TmsCustomRuleDO::getDeclaredValue, reqVO.getDeclaredValue())  // 申报金额
            .eqIfPresent(TmsCustomRuleDO::getDeclaredValueCurrencyCode, reqVO.getDeclaredValueCurrencyCode())  // 申报金额币种
            .eqIfPresent(TmsCustomRuleDO::getLogisticAttribute, reqVO.getLogisticAttribute())  // 物流属性
            .likeIfPresent(TmsCustomRuleDO::getFbaBarCode, reqVO.getFbaBarCode())  // FBA条形码
            .betweenIfPresent(TmsCustomRuleDO::getCreateTime, reqVO.getCreateTime())  // 创建时间范围
            .betweenIfPresent(TmsCustomRuleDO::getUpdateTime, reqVO.getUpdateTime())  // 更新时间范围
            .eqIfExists(TmsCustomRuleDO::getProductId, reqVO.getProductId()) // 产品id
            .orderByAsc(TmsCustomRuleDO::getId)  // 按id降序排序
            .leftJoin(TmsCustomProductDO.class, TmsCustomProductDO::getProductId, TmsCustomRuleDO::getProductId)  // 左连接产品表
            //            .likeIfExists(TmsProductDO::getBarCode, reqVO.getBarCode()) // 产品SKU编码
            ;
    }

    //海关分类
    private MPJLambdaWrapper<TmsCustomRuleDO> getBOWrapper(@NotNull TmsCustomRulePageReqVO reqVO) {
        return bindQueryWrapper(reqVO)
            .leftJoin(TmsCustomCategoryDO.class, TmsCustomCategoryDO::getId, TmsCustomProductDO::getCustomCategoryId)
            .likeIfExists(TmsCustomCategoryDO::getDeclaredType, reqVO.getDeclaredType())  // 申报品名
            .likeIfExists(TmsCustomCategoryDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())  // 申报品名en
            .selectAsClass(TmsCustomCategoryDO.class, TmsCustomRuleBO.class)
//            .leftJoin(TmsCustomCategoryItemDO.class, TmsCustomCategoryItemDO::getCustomCategoryId, TmsCustomCategoryDO::getId )
            .leftJoin(TmsCustomCategoryItemDO.class, on ->
                on.eq(TmsCustomCategoryItemDO::getCustomCategoryId, TmsCustomCategoryDO::getId)
                    .eq(TmsCustomCategoryItemDO::getCountryCode, TmsCustomRuleDO::getCountryCode)
            )
//            .eq(TmsCustomRuleDO::getCountryCode,TmsCustomCategoryItemDO::getCountryCode)
            .selectAsClass(TmsCustomCategoryItemDO.class, TmsCustomRuleBO.class)
            .eqIfExists(TmsCustomCategoryItemDO::getCountryCode, reqVO.getCountryCode())  // countryCode
            .likeIfExists(TmsCustomCategoryItemDO::getHscode, reqVO.getHscode())  // hsCode
            .likeIfExists(TmsCustomCategoryItemDO::getTaxRate, reqVO.getTaxRate())  // taxRate
            .orderByAsc(TmsCustomRuleDO::getId)
            ;
    }

    default PageResult<TmsCustomRuleDO> selectPage(TmsCustomRulePageReqVO reqVO) {
        // 构建查询，应用条件并进行分页查询
        return selectJoinPage(reqVO, TmsCustomRuleDO.class, bindQueryWrapper(reqVO));
    }

    /**
     * 根据 城市code 和 产品id 查询一个
     *
     * @param countryCode 国家代码
     * @param productId   产品id
     * @return TmsCustomRuleDO
     */
    default TmsCustomRuleDO getCustomRuleByCountryCodeAndProductId(Integer countryCode, Long productId) {
        return selectOne(new LambdaQueryWrapperX<TmsCustomRuleDO>()
            .eq(TmsCustomRuleDO::getCountryCode, countryCode)
            .eq(TmsCustomRuleDO::getProductId, productId));
    }

    /**
     * 根据 产品id 查询多个规则BO
     *
     * @param productIds ids
     * @return List<TmsCustomRuleDO>
     */
    default List<TmsCustomRuleBO> selectByProductId(@NotNull List<Long> productIds) {
        MPJLambdaWrapper<TmsCustomRuleDO> boWrapper = getBOWrapper(new TmsCustomRulePageReqVO());
        boWrapper.in(TmsCustomRuleDO::getProductId, productIds);
        return selectJoinList(TmsCustomRuleBO.class, boWrapper);
    }


    // 分页查询 TmsCustomRuleBO 海关规则数据(3表联查)海关规则->产品->分类 ，不关联国家查询
    default PageResult<TmsCustomRuleBO> selectBOPage(@NotNull TmsCustomRulePageReqVO reqVO) {
        return selectJoinPage(reqVO, TmsCustomRuleBO.class, getBOWrapper(reqVO));
    }

    // list查询 TmsCustomRuleBO 海关规则数据(3表联查)海关规则->产品->分类，不关联国家查询
    default List<TmsCustomRuleBO> selectBOList(@NotNull TmsCustomRulePageReqVO reqVO) {
        return selectJoinList(TmsCustomRuleBO.class, getBOWrapper(reqVO));
    }

    //关联国家查询 customCategoryId
    default List<TmsCustomRuleBO> selectBOListEqCountryCodeByCategoryId(@NotNull TmsCustomRulePageReqVO reqVO, @NotNull Long customCategoryId) {
        return selectJoinList(TmsCustomRuleBO.class, getBOWrapper(reqVO)
            .eq(TmsCustomRuleDO::getCountryCode, TmsCustomCategoryItemDO::getCountryCode)
            .eq(TmsCustomCategoryItemDO::getCustomCategoryId, customCategoryId)
        );
    }

    //关联国家查询 customCategoryItemId
    default List<TmsCustomRuleBO> selectBOListEqCountryCodeByItemId(@NotNull TmsCustomRulePageReqVO reqVO, @NotNull List<Long> customCategoryItemId) {
        return selectJoinList(TmsCustomRuleBO.class, getBOWrapper(reqVO)
            .eq(TmsCustomRuleDO::getCountryCode, TmsCustomCategoryItemDO::getCountryCode)
            .in(TmsCustomCategoryItemDO::getId, customCategoryItemId)
        );
    }

    //查到TmsCustomRuleBO通过id 关联国家查询
    default TmsCustomRuleBO getCustomRuleBOById(@NotNull Long id) {
        MPJLambdaWrapper<TmsCustomRuleDO> wrapper = getBOWrapper(new TmsCustomRulePageReqVO());
        wrapper.eq(TmsCustomRuleDO::getId, id);
        //根据海关规则的国别1-1映射 分类子表
        wrapper.eq(TmsCustomRuleDO::getCountryCode, TmsCustomCategoryItemDO::getCountryCode);
        return this.selectJoinOne(TmsCustomRuleBO.class, wrapper);
    }

    //通过ids查TmsCustomRuleBO集合 关联国家查询1:1
    default List<TmsCustomRuleBO> selectCustomRuleBOByIds(Collection<Long> ids) {
        MPJLambdaWrapper<TmsCustomRuleDO> wrapper = getBOWrapper(new TmsCustomRulePageReqVO());
        if (ids != null) {
            wrapper.in(TmsCustomRuleDO::getId, ids);
        }
        wrapper.eq(TmsCustomRuleDO::getCountryCode, TmsCustomCategoryItemDO::getCountryCode);
        return this.selectJoinList(TmsCustomRuleBO.class, wrapper);
    }
}
