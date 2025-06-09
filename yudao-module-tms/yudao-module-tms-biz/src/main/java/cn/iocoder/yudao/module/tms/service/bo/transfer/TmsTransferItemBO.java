package cn.iocoder.yudao.module.tms.service.bo.transfer;

import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.TmsTransferDO;
import cn.iocoder.yudao.module.tms.dal.dataobject.transfer.item.TmsTransferItemDO;
import lombok.Data;

@Data
public class TmsTransferItemBO extends TmsTransferItemDO {

    private TmsTransferDO tmsTransferDO;
}
