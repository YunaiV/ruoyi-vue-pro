package cn.iocoder.yudao.module.tms.service.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.fee.TmsFeeDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import lombok.Data;

import java.util.List;

/**
 * 头程单 BO
 */
@Data
public class TmsFirstMileBO extends TmsFirstMileDO {

    //头程单明细
    private List<TmsFirstMileItemDO> items;

    //费用明细
    private List<TmsFeeDO> fees;

    //最新跟踪信息
    private TmsVesselTrackingDO tracking;
}
