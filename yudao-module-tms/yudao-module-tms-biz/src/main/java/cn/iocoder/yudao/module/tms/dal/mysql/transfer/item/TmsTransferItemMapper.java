package cn.iocoder.yudao.module.tms.dal.mysql.transfer.item;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.item.vo.TmsTransferItemPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.transfer.vo.TmsTransferPageReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import cn.iocoder.yudao.module.tms.service.bo.transfer.TmsTransferItemBO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 调拨单明细 Mapper
 *
 * @author wdy
 */
@Mapper
public interface TmsTransferItemMapper extends BaseMapperX<TmsTransferItemDO> {

    /**
     * 构建调拨单明细查询条件
     *
     * @param vo 查询参数
     * @return 查询条件构造器
     */
    default MPJLambdaWrapperX<TmsTransferItemDO> buildWrapper(TmsTransferPageReqVO vo) {
        if (vo == null) {
            vo = new TmsTransferPageReqVO();
        }
        if (vo.getMainQueryVo() == null) {
            vo.setMainQueryVo(new TmsTransferPageReqVO.MainQueryVo());
        }
        if (vo.getItemQueryVo() == null) {
            vo.setItemQueryVo(new TmsTransferItemPageReqVO());
        }
        return new MPJLambdaWrapperX<TmsTransferItemDO>()
            .selectAll(TmsTransferItemDO.class)
            .betweenIfPresent(TmsTransferItemDO::getCreateTime, vo.getMainQueryVo().getCreateTime())
            .betweenIfPresent(TmsTransferItemDO::getUpdateTime, vo.getMainQueryVo().getUpdateTime())
            .eqIfPresent(TmsTransferItemDO::getUpdater, vo.getItemQueryVo().getUpdater())
            .eqIfPresent(TmsTransferItemDO::getCreator, vo.getItemQueryVo().getCreator())
            .eqIfPresent(TmsTransferItemDO::getProductId, vo.getItemQueryVo().getProductId())
            .eqIfPresent(TmsTransferItemDO::getQty, vo.getItemQueryVo().getQty())
            .eqIfPresent(TmsTransferItemDO::getBoxQty, vo.getItemQueryVo().getBoxQty())
            .eqIfPresent(TmsTransferItemDO::getPackageWeight, vo.getItemQueryVo().getPackageWeight())
            .eqIfPresent(TmsTransferItemDO::getPackageVolume, vo.getItemQueryVo().getPackageVolume())
            .eqIfPresent(TmsTransferItemDO::getStockCompanyId, vo.getItemQueryVo().getStockCompanyId())
            .eqIfPresent(TmsTransferItemDO::getRemark, vo.getItemQueryVo().getRemark())
            .eqIfPresent(TmsTransferItemDO::getOutboundClosedQty, vo.getItemQueryVo().getOutboundClosedQty())
            .eqIfPresent(TmsTransferItemDO::getInboundClosedQty, vo.getItemQueryVo().getInboundClosedQty());
    }

    default MPJLambdaWrapperX<TmsTransferItemDO> buildBOWrapper(TmsTransferPageReqVO vo) {
        if (vo == null) {
            vo = new TmsTransferPageReqVO();
        }
        if (vo.getMainQueryVo() == null) {
            vo.setMainQueryVo(new TmsTransferPageReqVO.MainQueryVo());
        }
        return buildWrapper(vo)
            .leftJoin(TmsTransferDO.class, TmsTransferDO::getId, TmsTransferItemDO::getTransferId)
            .selectAll(TmsTransferDO.class)
            .eqIfPresent(TmsTransferDO::getId, vo.getMainQueryVo().getId())
            .eqIfPresent(TmsTransferDO::getCreator, vo.getMainQueryVo().getCreator())
            .betweenIfPresent(TmsTransferDO::getCreateTime, vo.getMainQueryVo().getCreateTime())
            .eqIfPresent(TmsTransferDO::getUpdater, vo.getMainQueryVo().getUpdater())
            .betweenIfPresent(TmsTransferDO::getUpdateTime, vo.getMainQueryVo().getUpdateTime())
            .eqIfPresent(TmsTransferDO::getCode, vo.getMainQueryVo().getCode())
            .eqIfPresent(TmsTransferDO::getFromWarehouseId, vo.getMainQueryVo().getFromWarehouseId())
            .eqIfPresent(TmsTransferDO::getToWarehouseId, vo.getMainQueryVo().getToWarehouseId())
            .eqIfPresent(TmsTransferDO::getAuditorId, vo.getMainQueryVo().getAuditorId())
            .eqIfPresent(TmsTransferDO::getAuditStatus, vo.getMainQueryVo().getAuditStatus())
            .betweenIfPresent(TmsTransferDO::getAuditTime, vo.getMainQueryVo().getAuditTime())
            .eqIfPresent(TmsTransferDO::getAuditAdvice, vo.getMainQueryVo().getAuditAdvice())
            .eqIfPresent(TmsTransferDO::getOutboundStatus, vo.getMainQueryVo().getOutboundStatus())
            .betweenIfPresent(TmsTransferDO::getOutboundTime, vo.getMainQueryVo().getOutboundTime())
            .eqIfPresent(TmsTransferDO::getInboundStatus, vo.getMainQueryVo().getInboundStatus())
            .betweenIfPresent(TmsTransferDO::getInboundTime, vo.getMainQueryVo().getInboundTime())
            .eqIfPresent(TmsTransferDO::getTraceNo, vo.getMainQueryVo().getTraceNo())
            .betweenIfPresent(TmsTransferDO::getTotalValue, vo.getMainQueryVo().getTotalValue())
            .betweenIfPresent(TmsTransferDO::getNetWeight, vo.getMainQueryVo().getNetWeight())
            .betweenIfPresent(TmsTransferDO::getTotalWeight, vo.getMainQueryVo().getTotalWeight())
            .betweenIfPresent(TmsTransferDO::getTotalVolume, vo.getMainQueryVo().getTotalVolume())
            .betweenIfPresent(TmsTransferDO::getTotalQty, vo.getMainQueryVo().getTotalQty())
            .eqIfPresent(TmsTransferDO::getOutboundId, vo.getMainQueryVo().getOutboundId())
            .eqIfPresent(TmsTransferDO::getOutboundCode, vo.getMainQueryVo().getOutboundCode())
            .eqIfPresent(TmsTransferDO::getInboundId, vo.getMainQueryVo().getInboundId())
            .eqIfPresent(TmsTransferDO::getInboundCode, vo.getMainQueryVo().getInboundCode());
    }

    default PageResult<TmsTransferItemBO> selectBOPage(TmsTransferPageReqVO vo) {
        if (vo == null) {
            vo = new TmsTransferPageReqVO();
        }
        MPJLambdaWrapper<TmsTransferItemDO> wrapper = buildBOWrapper(vo);
        wrapper.selectAssociation(TmsTransferDO.class, TmsTransferItemBO::getTmsTransferDO);
        return selectJoinPage(vo, TmsTransferItemBO.class, wrapper);
    }


    default List<TmsTransferItemDO> selectListByTransferId(Long transferId) {
        return selectList(new LambdaQueryWrapperX<TmsTransferItemDO>()
            .eq(TmsTransferItemDO::getTransferId, transferId)
            .orderByDesc(TmsTransferItemDO::getId));
    }

    default void deleteByTransferId(Long transferId) {
        delete(new LambdaQueryWrapperX<TmsTransferItemDO>()
            .eq(TmsTransferItemDO::getTransferId, transferId));
    }

}