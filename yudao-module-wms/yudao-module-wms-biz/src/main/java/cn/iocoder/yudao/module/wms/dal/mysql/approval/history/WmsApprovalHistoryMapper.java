package cn.iocoder.yudao.module.wms.dal.mysql.approval.history;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.wms.controller.admin.approval.history.vo.WmsApprovalHistoryPageReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.approval.history.WmsApprovalHistoryDO;
import cn.iocoder.yudao.module.system.enums.somle.BillType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 审批历史 Mapper
 *
 * @author 李方捷
 */
@Mapper
public interface WmsApprovalHistoryMapper extends BaseMapperX<WmsApprovalHistoryDO> {

    default PageResult<WmsApprovalHistoryDO> selectPage(WmsApprovalHistoryPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WmsApprovalHistoryDO>()
				.eqIfPresent(WmsApprovalHistoryDO::getBillType, reqVO.getBillType())
				.eqIfPresent(WmsApprovalHistoryDO::getBillId, reqVO.getBillId())
				.eqIfPresent(WmsApprovalHistoryDO::getStatusType, reqVO.getStatusType())
				.eqIfPresent(WmsApprovalHistoryDO::getStatusBefore, reqVO.getStatusBefore())
				.eqIfPresent(WmsApprovalHistoryDO::getStatusAfter, reqVO.getStatusAfter())
				.eqIfPresent(WmsApprovalHistoryDO::getComment, reqVO.getComment())
				.betweenIfPresent(WmsApprovalHistoryDO::getCreateTime, reqVO.getCreateTime())
				.orderByDesc(WmsApprovalHistoryDO::getId));
    }

    /**
     * 按 bill_type,status_type,bill_id 查询 WmsApprovalHistoryDO 清单
     */
    default List<WmsApprovalHistoryDO> selectByIdxBill(String billType, String statusType, String billId) {
        return selectList(new LambdaQueryWrapperX<WmsApprovalHistoryDO>().eq(WmsApprovalHistoryDO::getBillType, billType).eq(WmsApprovalHistoryDO::getStatusType, statusType).eq(WmsApprovalHistoryDO::getBillId, billId));
    }

    /**
     * 按 bill_type,status_type,bill_id 查询 WmsApprovalHistoryDO 清单
     */
    default List<WmsApprovalHistoryDO> selectByIdxBill(Integer billType, String statusType, String billId) {
        return selectList(new LambdaQueryWrapperX<WmsApprovalHistoryDO>().eq(WmsApprovalHistoryDO::getBillType, billType).eq(WmsApprovalHistoryDO::getStatusType, statusType).eq(WmsApprovalHistoryDO::getBillId, billId));
    }

    /**
     * 按 bill_type,status_type,bill_id 查询 WmsApprovalHistoryDO 清单
     */
    default List<WmsApprovalHistoryDO> selectByIdxBill(Integer billType, String statusType, Long billId) {
        return selectList(new LambdaQueryWrapperX<WmsApprovalHistoryDO>().eq(WmsApprovalHistoryDO::getBillType, billType).eq(WmsApprovalHistoryDO::getStatusType, statusType).eq(WmsApprovalHistoryDO::getBillId, billId));
    }

    default List<WmsApprovalHistoryDO> selectGroupedApprovalHistory(BillType billType, List<Long> billIds) {
        LambdaQueryWrapperX<WmsApprovalHistoryDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(WmsApprovalHistoryDO::getBillType, billType.getValue())
            .in(WmsApprovalHistoryDO::getBillId, billIds);
        wrapper.orderByAsc(WmsApprovalHistoryDO::getCreateTime);
        return selectList(wrapper);
    }
}
