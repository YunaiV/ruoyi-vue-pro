package cn.iocoder.yudao.module.tms.service.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.TmsFirstMileDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.item.TmsFirstMileItemDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.vessel.tracking.TmsVesselTrackingDO;
import lombok.Data;

/**
 * 头程单明细 BO
 */
@Data
public class TmsFirstMileItemBO extends TmsFirstMileItemDO {

    private TmsFirstMileDO tmsFirstMileDO;
    //物流信息(船) 最新
    private TmsVesselTrackingDO tmsVesselTrackingDO;
}
