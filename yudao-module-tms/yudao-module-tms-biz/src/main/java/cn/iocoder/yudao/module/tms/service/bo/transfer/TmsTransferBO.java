package cn.iocoder.yudao.module.tms.service.bo.transfer;

import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import lombok.Data;

import java.util.List;

@Data
public class TmsTransferBO extends TmsTransferDO {

    private List<TmsTransferItemDO> tmsTransferItemDOList;
}
