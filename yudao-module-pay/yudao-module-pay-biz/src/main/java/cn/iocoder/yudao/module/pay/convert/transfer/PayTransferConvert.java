package cn.iocoder.yudao.module.pay.convert.transfer;

import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayTransferConvert {

    PayTransferConvert INSTANCE = Mappers.getMapper(PayTransferConvert.class);

    @Mapping(source = "title", target = "subject") // TODO @jason：是不是都改成 subject 完事呀？
    PayTransferDO convert(PayTransferCreateReqDTO dto);

    PayTransferCreateReqDTO convert(PayDemoTransferCreateReqVO vo);

}
