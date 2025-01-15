package cn.iocoder.yudao.module.erp.dal.mysql.logistic.customrule;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpCustomRuleDTO;
import cn.iocoder.yudao.module.erp.controller.admin.logistic.customrule.vo.ErpCustomRulePageReqVO;
import cn.iocoder.yudao.module.erp.dal.dataobject.logistic.customrule.ErpCustomRuleDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.dataobject.purchase.ErpSupplierProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ERP 海关规则 Mapper
 *
 * @author 索迈管理员
 */
@Mapper
public interface ErpCustomRuleMapper extends BaseMapperX<ErpCustomRuleDO> {
    default MPJLambdaWrapper<ErpCustomRuleDO> getWrapper() {
        return new MPJLambdaWrapperX<ErpCustomRuleDO>()
            .selectAll(ErpCustomRuleDO.class)
            .leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpCustomRuleDO::getProductId)
            .selectAs(ErpProductDO::getName, ErpCustomRuleDTO::getProductName)
            .selectAs(ErpProductDO::getPrimaryImageUrl, ErpCustomRuleDTO::getProductImageUrl)
            .selectAs(ErpProductDO::getWeight, ErpCustomRuleDTO::getProductWeight)
            .selectAs(ErpProductDO::getLength, ErpCustomRuleDTO::getProductLength)
            .selectAs(ErpProductDO::getWidth, ErpCustomRuleDTO::getProductWidth)
            .selectAs(ErpProductDO::getHeight, ErpCustomRuleDTO::getProductHeight)
            .selectAs(ErpProductDO::getPackageHeight, ErpCustomRuleDTO::getPackageHeight)
            .selectAs(ErpProductDO::getPackageLength, ErpCustomRuleDTO::getPackageLength)
            .selectAs(ErpProductDO::getPackageWeight, ErpCustomRuleDTO::getPackageWeight)
            .selectAs(ErpProductDO::getPackageWidth, ErpCustomRuleDTO::getPackageWidth)
            .selectAs(ErpProductDO::getMaterial, ErpCustomRuleDTO::getProductMaterial)
            .selectAs(ErpProductDO::getCreator, ErpCustomRuleDTO::getProductCreatorId)
            .selectAs(ErpProductDO::getBarCode, ErpCustomRuleDTO::getBarCode)
            .selectAs(ErpProductDO::getDeptId, ErpCustomRuleDTO::getProductDeptId);
    }
    /**
     * 分页查询ERP海关规则数据
     *
     * @param reqVO 包含分页和筛选条件的请求对象
     * @return 满足条件的海关规则数据分页结果
     */
    default PageResult<ErpCustomRuleDO> selectPage(ErpCustomRulePageReqVO reqVO) {
        // 构建查询，应用条件并进行分页查询
        MPJLambdaWrapper<ErpCustomRuleDO> query = new MPJLambdaWrapperX<ErpCustomRuleDO>()
            .selectAll(ErpCustomRuleDO.class)  // 选择所有列
            .eqIfPresent(ErpCustomRuleDO::getCountryCode, reqVO.getCountryCode())  // 国家编码
            .likeIfPresent(ErpCustomRuleDO::getDeclaredTypeEn, reqVO.getDeclaredTypeEn())  // 申报品名（英文）
            .likeIfPresent(ErpCustomRuleDO::getDeclaredType, reqVO.getDeclaredType())  // 申报品名
            .eqIfPresent(ErpCustomRuleDO::getDeclaredValue, reqVO.getDeclaredValue())  // 申报金额
            .eqIfPresent(ErpCustomRuleDO::getDeclaredValueCurrencyCode, reqVO.getDeclaredValueCurrencyCode())  // 申报金额币种
            .eqIfPresent(ErpCustomRuleDO::getTaxRate, reqVO.getTaxRate())  // 税率
            .eqIfPresent(ErpCustomRuleDO::getHscode, reqVO.getHscode())  // HS编码
            .eqIfPresent(ErpCustomRuleDO::getLogisticAttribute, reqVO.getLogisticAttribute())  // 物流属性
            .likeIfPresent(ErpCustomRuleDO::getFbaBarCode, reqVO.getFbaBarCode())  // FBA条形码
            .betweenIfPresent(ErpCustomRuleDO::getCreateTime, reqVO.getCreateTime())  // 创建时间范围
            .orderByDesc(ErpCustomRuleDO::getId)  // 按id降序排序
            .leftJoin(ErpProductDO.class, ErpProductDO::getId, ErpCustomRuleDO::getProductId)  // 左连接产品表
            .likeIfExists(ErpProductDO::getBarCode, reqVO.getBarCode()); // 产品SKU编码

        return selectJoinPage(reqVO, ErpCustomRuleDO.class, query);
    }


    /**
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     * @Author Wqh
     * @Description 根据海关规则id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     **/
    default List<ErpCustomRuleDTO> selectProductAllInfoListByCustomRuleId(@NotNull(message = "海关规则id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpCustomRuleDO::getId, id));
    }

    /**
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     * @Author Wqh
     * @Description 根据产品id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     **/
    default List<ErpCustomRuleDTO> selectProductAllInfoListById(@NotNull(message = "产品id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpProductDO::getId, id));
    }

    /**
     * @return java.util.List<cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO>
     * @Author Wqh
     * @Description 根据供应商产品id获取产品的全量信息（海关规则，产品供应商）
     * @Date 10:40 2024/11/5
     * @Param [id]
     **/
    default List<ErpCustomRuleDTO> selectProductAllInfoListBySupplierId(@NotNull(message = "供应商产品id不能为空") Long id) {
        return selectJoinList(ErpCustomRuleDTO.class, getWrapper().eq(ErpSupplierProductDO::getId, id));
    }


    /**
     * 根据 城市code 和 产品id 查询一个
     *
     * @param countryCode 国家代码
     * @param productId   产品id
     * @return ErpCustomRuleDO
     */
    default ErpCustomRuleDO getCustomRuleByCountryCodeAndProductId(Integer countryCode, Long productId) {
        return selectOne(new LambdaQueryWrapperX<ErpCustomRuleDO>()
            .eq(ErpCustomRuleDO::getCountryCode, countryCode)
            .eq(ErpCustomRuleDO::getProductId, productId));
    }
}
