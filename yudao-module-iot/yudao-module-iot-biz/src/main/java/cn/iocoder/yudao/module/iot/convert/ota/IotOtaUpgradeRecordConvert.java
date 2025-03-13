package cn.iocoder.yudao.module.iot.convert.ota;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface IotOtaUpgradeRecordConvert {

    IotOtaUpgradeRecordConvert INSTANCE = Mappers.getMapper(IotOtaUpgradeRecordConvert.class);

}
