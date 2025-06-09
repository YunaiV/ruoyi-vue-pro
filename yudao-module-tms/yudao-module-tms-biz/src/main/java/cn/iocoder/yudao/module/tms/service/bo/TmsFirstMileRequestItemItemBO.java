package cn.iocoder.yudao.module.tms.service.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import lombok.Data;

@Data
public class TmsFirstMileRequestItemItemBO extends TmsFirstMileRequestItemDO {

    //主表
    private TmsFirstMileRequestDO tmsFirstMileRequestDO;
}
