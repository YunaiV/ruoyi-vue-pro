package cn.iocoder.yudao.module.srm.dal.mysql.purchase;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.purchase.vo.in.req.SrmPurchaseInPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInDO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmPurchaseInItemDO;
import cn.iocoder.yudao.module.srm.service.purchase.bo.in.SrmPurchaseInItemBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import de.danielbechler.util.Collections;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 采购到货项 Mapper
 *
 * @author wdy
 */
@Mapper
public interface SrmPurchaseInItemMapper extends BaseMapperX<SrmPurchaseInItemDO> {

    default MPJLambdaWrapperX<SrmPurchaseInItemDO> buildWrapper(SrmPurchaseInPageReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new SrmPurchaseInPageReqVO();
        }
        MPJLambdaWrapperX<SrmPurchaseInItemDO> wrapper = new MPJLambdaWrapperX<SrmPurchaseInItemDO>()
            .selectAll(SrmPurchaseInItemDO.class);

        // 处理明细表查询条件
        if (reqVO.getItemQuery() != null) {
            SrmPurchaseInPageReqVO.ItemQuery itemQuery = reqVO.getItemQuery();
            wrapper
                // ========== 产品信息 ==========
                .eqIfPresent(SrmPurchaseInItemDO::getProductId, itemQuery.getProductId())
                .eqIfPresent(SrmPurchaseInItemDO::getProductUnitId, itemQuery.getProductUnitId())
                .likeIfPresent(SrmPurchaseInItemDO::getProductName, itemQuery.getProductName())
                .likeIfPresent(SrmPurchaseInItemDO::getDeclaredType, itemQuery.getDeclaredType())
                .likeIfPresent(SrmPurchaseInItemDO::getDeclaredTypeEn, itemQuery.getDeclaredTypeEn())
                .likeIfPresent(SrmPurchaseInItemDO::getProductCode, itemQuery.getProductCode())
                // ========== 仓库信息 ==========
                .eqIfPresent(SrmPurchaseInItemDO::getWarehouseId, itemQuery.getWarehouseId())
                // ========== 订单信息 ==========
                .eqIfPresent(SrmPurchaseInItemDO::getOrderItemId, itemQuery.getOrderItemId())
                .likeIfPresent(SrmPurchaseInItemDO::getOrderCode, itemQuery.getOrderCode())
                .likeIfPresent(SrmPurchaseInItemDO::getSource, itemQuery.getSource())
                // ========== 申请人信息 ==========
                .eqIfPresent(SrmPurchaseInItemDO::getApplicantId, itemQuery.getApplicantId())
                .eqIfPresent(SrmPurchaseInItemDO::getApplicationDeptId, itemQuery.getApplicationDeptId())
                // ========== 状态信息 ==========
                .eqIfPresent(SrmPurchaseInItemDO::getInboundStatus, itemQuery.getInboundStatus())
                .eqIfPresent(SrmPurchaseInItemDO::getPayStatus, itemQuery.getPayStatus())
                .orderByDesc(SrmPurchaseInItemDO::getCreateTime) // 按时间降序排序
            ;
        }

        return wrapper.orderByDesc(SrmPurchaseInItemDO::getCreateTime);
    }

    default MPJLambdaWrapperX<SrmPurchaseInItemDO> buildBOWrapper(SrmPurchaseInPageReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new SrmPurchaseInPageReqVO();
        }
        MPJLambdaWrapperX<SrmPurchaseInItemDO> wrapper = buildWrapper(reqVO)
            // ========== 关联主表 ==========
            .leftJoin(SrmPurchaseInDO.class, SrmPurchaseInDO::getId, SrmPurchaseInItemDO::getArriveId)
            .selectAll(SrmPurchaseInDO.class);

        // 处理主表查询条件
        if (reqVO.getMainQuery() != null) {
            SrmPurchaseInPageReqVO.MainQuery mainQuery = reqVO.getMainQuery();
            wrapper
                // ========== 基础信息 ==========
                .eqIfPresent(SrmPurchaseInDO::getId, mainQuery.getId())
                .likeIfPresent(SrmPurchaseInDO::getCode, mainQuery.getCode())
                // ========== 供应商信息 ==========
                .eqIfPresent(SrmPurchaseInDO::getSupplierId, mainQuery.getSupplierId())
                .likeIfPresent(SrmPurchaseInDO::getAddress, mainQuery.getAddress())
                // ========== 结算信息 ==========
                .eqIfPresent(SrmPurchaseInDO::getAccountId, mainQuery.getAccountId())
                .eqIfPresent(SrmPurchaseInDO::getCurrencyId, mainQuery.getCurrencyId())
                // ========== 时间信息 ==========
                .betweenIfPresent(SrmPurchaseInDO::getBillTime, mainQuery.getBillTime())
                .betweenIfPresent(SrmPurchaseInDO::getArriveTime, mainQuery.getArriveTime())
                // ========== 审核信息 ==========
                .eqIfPresent(SrmPurchaseInDO::getAuditorId, mainQuery.getAuditorId())
                .betweenIfPresent(SrmPurchaseInDO::getAuditTime, mainQuery.getAuditTime())
                .eqIfPresent(SrmPurchaseInDO::getAuditStatus, mainQuery.getAuditStatus())
                // ========== 状态信息 ==========
                .eqIfPresent(SrmPurchaseInDO::getInboundStatus, mainQuery.getInboundStatus())
                // ========== 时间范围 ==========
                .betweenIfPresent(SrmPurchaseInDO::getCreateTime, mainQuery.getCreateTime())
                .orderByDesc(SrmPurchaseInDO::getCreateTime)
            ;
        }

        return wrapper;
    }

    //page
    default PageResult<SrmPurchaseInItemBO> selectBOPage(SrmPurchaseInPageReqVO reqVO) {
        if (reqVO == null) {
            reqVO = new SrmPurchaseInPageReqVO();
        }
        MPJLambdaWrapper<SrmPurchaseInItemDO> wrapper = buildBOWrapper(reqVO).selectAssociation(SrmPurchaseInDO.class, SrmPurchaseInItemBO::getSrmPurchaseInDO);
        return selectJoinPage(reqVO, SrmPurchaseInItemBO.class, wrapper);
    }

    //list
    default List<SrmPurchaseInItemBO> selectBOList(Set<Long> inIds) {
        if (Collections.isEmpty(inIds)) {
            return List.of();
        }
        MPJLambdaWrapper<SrmPurchaseInItemDO> wrapper = buildBOWrapper(new SrmPurchaseInPageReqVO()).selectAssociation(SrmPurchaseInDO.class, SrmPurchaseInItemBO::getSrmPurchaseInDO);
        wrapper.in(SrmPurchaseInItemDO::getArriveId, inIds);
        return selectJoinList(SrmPurchaseInItemBO.class, wrapper);
    }

    //id
    default SrmPurchaseInItemBO selectBOById(Long inIds) {
        if (inIds == null) {
            return null;
        }
        MPJLambdaWrapper<SrmPurchaseInItemDO> wrapper = buildBOWrapper(new SrmPurchaseInPageReqVO()).selectAssociation(SrmPurchaseInDO.class, SrmPurchaseInItemBO::getSrmPurchaseInDO);
        wrapper.eq(SrmPurchaseInDO::getId, inIds);
        return selectJoinOne(SrmPurchaseInItemBO.class, wrapper);
    }
    default List<SrmPurchaseInItemDO> selectListByInId(Long inId) {
        return selectList(SrmPurchaseInItemDO::getArriveId, inId);
    }

    default List<SrmPurchaseInItemDO> selectListByInIds(Collection<Long> inIds) {
        return selectList(SrmPurchaseInItemDO::getArriveId, inIds);
    }

    default int deleteByInId(Long inId) {
        return delete(SrmPurchaseInItemDO::getArriveId, inId);
    }

    //根据订单项id获得入库项item数量
    default Long sumInItemsByItemId(Long orderItemId) {
        return selectCount(SrmPurchaseInItemDO::getOrderItemId, orderItemId);
    }

    //根据orderItemId找对应的入库项
    default List<SrmPurchaseInItemDO> selectListByOrderItemIds(List<Long> orderItemIds) {
        return selectList(SrmPurchaseInItemDO::getOrderItemId, orderItemIds);
    }

    //根据orderItemId是否存在入库项
    default boolean existsByOrderItemId(Long orderItemId) {
        return selectCount(SrmPurchaseInItemDO::getOrderItemId, orderItemId) > 0;
    }

    //根据oderItemId找对应的入库项 list
    default List<SrmPurchaseInItemDO> selectListByOrderItemId(Long orderItemId) {
        return selectList(SrmPurchaseInItemDO::getOrderItemId, orderItemId);
    }
}