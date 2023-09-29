package cn.iocoder.yudao.module.pay.convert.transfer;

import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @author jason
 */
@Mapper
public interface PayTransferConvert {

    PayTransferConvert  INSTANCE = Mappers.getMapper(PayTransferConvert.class);

    PayTransferDO convert(PayTransferCreateReqDTO dto);
    @Mapping(source = "transferType", target = "type")
    PayTransferCreateReqDTO convert(PayDemoTransferCreateReqVO vo);

}
