package cn.iocoder.yudao.module.mes.dal.mysql.wm.outsourcereceipt;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourcereceipt.vo.line.MesWmOutsourceReceiptLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourcereceipt.MesWmOutsourceReceiptLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 外协入库单行 Mapper
 */
@Mapper
public interface MesWmOutsourceReceiptLineMapper extends BaseMapperX<MesWmOutsourceReceiptLineDO> {

    default PageResult<MesWmOutsourceReceiptLineDO> selectPage(MesWmOutsourceReceiptLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesWmOutsourceReceiptLineDO>()
                .eqIfPresent(MesWmOutsourceReceiptLineDO::getReceiptId, reqVO.getReceiptId())
                .eqIfPresent(MesWmOutsourceReceiptLineDO::getItemId, reqVO.getItemId())
                .orderByDesc(MesWmOutsourceReceiptLineDO::getId));
    }

    default List<MesWmOutsourceReceiptLineDO> selectListByReceiptId(Long receiptId) {
        return selectList(new LambdaQueryWrapperX<MesWmOutsourceReceiptLineDO>()
                .eq(MesWmOutsourceReceiptLineDO::getReceiptId, receiptId)
                .orderByAsc(MesWmOutsourceReceiptLineDO::getId));
    }

    default int deleteByReceiptId(Long receiptId) {
        return delete(new LambdaQueryWrapperX<MesWmOutsourceReceiptLineDO>()
                .eq(MesWmOutsourceReceiptLineDO::getReceiptId, receiptId));
    }

}
