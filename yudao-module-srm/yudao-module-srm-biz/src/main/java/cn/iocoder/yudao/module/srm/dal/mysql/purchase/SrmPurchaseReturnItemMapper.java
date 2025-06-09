package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.returns.SrmPurchaseReturnPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseReturnItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.refund.SrmPurchaseReturnItemBO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ERP 采购退货项 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseReturnItemMapper extends BaseMapperX<SrmPurchaseReturnItemDO> {

    default MPJLambdaWrapperX<SrmPurchaseReturnItemDO> buildWrapper(SrmPurchaseReturnPageReqVO vo) {
        if (vo == null) {
            vo = new SrmPurchaseReturnPageReqVO();
        }
        if (vo.getItemQuery() == null) {
            vo.setItemQuery(new SrmPurchaseReturnPageReqVO.ItemQuery());
        }
        return new MPJLambdaWrapperX<SrmPurchaseReturnItemDO>().selectAll(SrmPurchaseReturnItemDO.class)
            .eqIfPresent(SrmPurchaseReturnItemDO::getArriveItemId, vo.getItemQuery().getArriveItemId()) // 入库项id
            .likeIfPresent(SrmPurchaseReturnItemDO::getArriveCode, vo.getItemQuery().getArriveCode()) // 入库单code
            .eqIfPresent(SrmPurchaseReturnItemDO::getWarehouseId, vo.getItemQuery().getWarehouseId()) // 仓库编号
            .eqIfPresent(SrmPurchaseReturnItemDO::getProductId, vo.getItemQuery().getProductId()) // 产品编号
            .eqIfPresent(SrmPurchaseReturnItemDO::getProductUnitId, vo.getItemQuery().getProductUnitId()) // 产品单位单位
            .eqIfPresent(SrmPurchaseReturnItemDO::getProductPrice, vo.getItemQuery().getProductPrice()) // 产品单位单价
            .likeIfPresent(SrmPurchaseReturnItemDO::getProductUnitName, vo.getItemQuery().getProductUnitName()) // 产品单位名称
            .eqIfPresent(SrmPurchaseReturnItemDO::getQty, vo.getItemQuery().getQty()) // 数量
            .eqIfPresent(SrmPurchaseReturnItemDO::getTotalPrice, vo.getItemQuery().getTotalPrice()) // 总价
            .eqIfPresent(SrmPurchaseReturnItemDO::getTaxRate, vo.getItemQuery().getTaxRate()) // 税率
            .eqIfPresent(SrmPurchaseReturnItemDO::getTax, vo.getItemQuery().getTax()) // 税额
            .eqIfPresent(SrmPurchaseReturnItemDO::getGrossPrice, vo.getItemQuery().getGrossPrice()) // 含税单价
            .likeIfPresent(SrmPurchaseReturnItemDO::getRemark, vo.getItemQuery().getRemark()) // 备注
            .likeIfPresent(SrmPurchaseReturnItemDO::getContainerRate, vo.getItemQuery().getContainerRate()) // 箱率
            .eqIfPresent(SrmPurchaseReturnItemDO::getApplicantId, vo.getItemQuery().getApplicantId()) // 申请人id
            .eqIfPresent(SrmPurchaseReturnItemDO::getApplicationDeptId, vo.getItemQuery().getApplicationDeptId()) // 申请部门id
            .likeIfPresent(SrmPurchaseReturnItemDO::getDeclaredType, vo.getItemQuery().getDeclaredType()) // 报关品名
            .likeIfPresent(SrmPurchaseReturnItemDO::getDeclaredTypeEn, vo.getItemQuery().getDeclaredTypeEn()) // 报关品名英文
            .likeIfPresent(SrmPurchaseReturnItemDO::getProductCode, vo.getItemQuery().getProductCode()) // 产品sku
            .likeIfPresent(SrmPurchaseReturnItemDO::getProductName, vo.getItemQuery().getProductName()) // 产品名称
            .eqIfPresent(SrmPurchaseReturnItemDO::getOutboundStatus, vo.getItemQuery().getOutboundStatus())
            .orderByDesc(SrmPurchaseReturnItemDO::getCreateTime)
            ;
    }

    default MPJLambdaWrapperX<SrmPurchaseReturnItemDO> buildBOWrapper(SrmPurchaseReturnPageReqVO vo) {
        if (vo == null) {
            vo = new SrmPurchaseReturnPageReqVO();
        }
        if (vo.getMainQuery() == null) {
            vo.setMainQuery(new SrmPurchaseReturnPageReqVO.MainQuery());
        }
        return buildWrapper(vo).leftJoin(SrmPurchaseReturnDO.class, SrmPurchaseReturnDO::getId, SrmPurchaseReturnItemDO::getReturnId)
            // 主表查询条件
            .likeIfPresent(SrmPurchaseReturnDO::getCode, vo.getMainQuery().getCode()) // 退货单编号
            .eqIfPresent(SrmPurchaseReturnDO::getAuditStatus, vo.getMainQuery().getAuditStatus()) // 审核状态
            .eqIfPresent(SrmPurchaseReturnDO::getAuditorId, vo.getMainQuery().getAuditorId()) // 审核人id
            .betweenIfPresent(SrmPurchaseReturnDO::getAuditTime, vo.getMainQuery().getAuditTime()) // 审核时间
            .eqIfPresent(SrmPurchaseReturnDO::getSupplierId, vo.getMainQuery().getSupplierId()) // 供应商编号
            .eqIfPresent(SrmPurchaseReturnDO::getAccountId, vo.getMainQuery().getAccountId()) // 结算账户编号
            .betweenIfPresent(SrmPurchaseReturnDO::getReturnTime, vo.getMainQuery().getReturnTime()) // 退货时间
            .eqIfPresent(SrmPurchaseReturnDO::getCurrencyId, vo.getMainQuery().getCurrencyId()) // 币种编号
            .eqIfPresent(SrmPurchaseReturnDO::getGrossTotalPrice, vo.getMainQuery().getGrossTotalPrice()) // 价税合计
            .eqIfPresent(SrmPurchaseReturnDO::getTotalCount, vo.getMainQuery().getTotalCount()) // 合计数量
            .eqIfPresent(SrmPurchaseReturnDO::getTotalPrice, vo.getMainQuery().getTotalPrice()) // 最终合计价格
            .eqIfPresent(SrmPurchaseReturnDO::getTotalWeight, vo.getMainQuery().getTotalWeight()) // 总毛重
            .eqIfPresent(SrmPurchaseReturnDO::getTotalVolume, vo.getMainQuery().getTotalVolume()) // 总体积
            .eqIfPresent(SrmPurchaseReturnDO::getRefundPrice, vo.getMainQuery().getRefundPrice()) // 已退款金额
            .eqIfPresent(SrmPurchaseReturnDO::getTotalProductPrice, vo.getMainQuery().getTotalProductPrice()) // 合计产品价格
            .eqIfPresent(SrmPurchaseReturnDO::getTotalGrossPrice, vo.getMainQuery().getTotalGrossPrice()) // 合计税额
            .eqIfPresent(SrmPurchaseReturnItemDO::getId, vo.getMainQuery().getId()) //ID
            .eqIfPresent(SrmPurchaseReturnItemDO::getCreator, vo.getMainQuery().getCreator()) // 创建人
            .eqIfPresent(SrmPurchaseReturnItemDO::getOutboundStatus, vo.getMainQuery().getOutboundStatus())
            .orderByDesc(SrmPurchaseReturnItemDO::getCreateTime)
            ;
    }

    default PageResult<SrmPurchaseReturnItemBO> selectBOPage(SrmPurchaseReturnPageReqVO vo) {
        if (vo == null) {
            vo = new SrmPurchaseReturnPageReqVO();
        }
        MPJLambdaWrapper<SrmPurchaseReturnItemDO> wrapper = buildBOWrapper(vo).selectAssociation(SrmPurchaseReturnDO.class, SrmPurchaseReturnItemBO::getSrmPurchaseReturnDO);
        return selectJoinPage(vo, SrmPurchaseReturnItemBO.class, wrapper);
    }

    default List<SrmPurchaseReturnItemDO> selectListByReturnId(Long returnId) {
        return selectList(SrmPurchaseReturnItemDO::getReturnId, returnId);
    }

    default List<SrmPurchaseReturnItemDO> selectListByReturnIds(Collection<Long> returnIds) {
        return selectList(SrmPurchaseReturnItemDO::getReturnId, returnIds);
    }

    default int deleteByReturnId(Long returnId) {
        return delete(SrmPurchaseReturnItemDO::getReturnId, returnId);
    }

    //通过ids查询,如果ids是空，则返回空集合
    default List<SrmPurchaseReturnItemDO> selectListByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().in(SrmPurchaseReturnItemDO::getId, ids));
    }

    //通过入库项id查找对应的采购退货项
    default List<SrmPurchaseReturnItemDO> selectListByInItemId(Long arriveItemId) {
        return selectList(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().eq(SrmPurchaseReturnItemDO::getArriveItemId, arriveItemId));
    }

    //入库项id存在对应的采购退货项
    default boolean existsByInItemId(Long arriveItemId) {
        return selectCount(new LambdaQueryWrapper<SrmPurchaseReturnItemDO>().eq(SrmPurchaseReturnItemDO::getArriveItemId, arriveItemId)) > 0;
    }
}