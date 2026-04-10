package cn.iocoder.yudao.module.mes.dal.mysql.wm.miscreceipt;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.miscreceipt.MesWmMiscReceiptDetailDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 杂项入库明细 Mapper
 */
@Mapper
public interface MesWmMiscReceiptDetailMapper extends BaseMapperX<MesWmMiscReceiptDetailDO> {

    default List<MesWmMiscReceiptDetailDO> selectListByLineId(Long lineId) {
        return selectList(MesWmMiscReceiptDetailDO::getLineId, lineId);
    }

    default List<MesWmMiscReceiptDetailDO> selectListByReceiptId(Long receiptId) {
        return selectList(MesWmMiscReceiptDetailDO::getReceiptId, receiptId);
    }

    default void deleteByReceiptId(Long receiptId) {
        delete(MesWmMiscReceiptDetailDO::getReceiptId, receiptId);
    }

    default void deleteByLineId(Long lineId) {
        delete(MesWmMiscReceiptDetailDO::getLineId, lineId);
    }

}
