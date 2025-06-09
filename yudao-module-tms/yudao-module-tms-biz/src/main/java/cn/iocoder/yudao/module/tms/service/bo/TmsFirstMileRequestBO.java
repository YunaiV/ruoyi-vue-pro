package cn.iocoder.yudao.module.tms.service.bo;

import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.TmsFirstMileRequestDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.first.mile.request.item.TmsFirstMileRequestItemDO;
import lombok.Data;

import java.util.List;

@Data
public class TmsFirstMileRequestBO extends TmsFirstMileRequestDO {
    //1:N 主子表
    private List<TmsFirstMileRequestItemDO> items;
}
