package cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 外协入库明细 Mapper
 */
@Mapper
public interface MesWmOutsourceReceiptDetailMapper extends BaseMapperX<MesWmOutsourceReceiptDetailDO> {

    default List<MesWmOutsourceReceiptDetailDO> selectListByReceiptId(Long receiptId) {
        return selectList(new LambdaQueryWrapperX<MesWmOutsourceReceiptDetailDO>()
                .eq(MesWmOutsourceReceiptDetailDO::getReceiptId, receiptId)
                .orderByAsc(MesWmOutsourceReceiptDetailDO::getId));
    }

    default List<MesWmOutsourceReceiptDetailDO> selectListByLineId(Long lineId) {
        return selectList(new LambdaQueryWrapperX<MesWmOutsourceReceiptDetailDO>()
                .eq(MesWmOutsourceReceiptDetailDO::getLineId, lineId)
                .orderByAsc(MesWmOutsourceReceiptDetailDO::getId));
    }

    default int deleteByReceiptId(Long receiptId) {
        return delete(new LambdaQueryWrapperX<MesWmOutsourceReceiptDetailDO>()
                .eq(MesWmOutsourceReceiptDetailDO::getReceiptId, receiptId));
    }

    default int deleteByLineId(Long lineId) {
        return delete(new LambdaQueryWrapperX<MesWmOutsourceReceiptDetailDO>()
                .eq(MesWmOutsourceReceiptDetailDO::getLineId, lineId));
    }

}
