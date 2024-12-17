package cn.iocoder.yudao.module.iot.convert.device;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

// TODO 是否要删除？
@Mapper
public interface IotDeviceDataConvert {

    IotDeviceDataConvert INSTANCE = Mappers.getMapper(IotDeviceDataConvert.class);

//    default  List<IotDeviceDataRespVO> convert(Map<String, Object> deviceData, IotDeviceDO device){
//        List<IotDeviceDataRespVO> list = new ArrayList<>();
//        deviceData.forEach((identifier, value) -> {
////            ThinkModelProperty property = ThinkModelService.INSTANCE.getProperty(device.getProductId(), identifier);
////            if (Objects.isNull(property)) {
////                return;
////            }
//            IotDeviceDataRespVO vo = new IotDeviceDataRespVO();
//            vo.setDeviceId(device.getId());
//            vo.setProductKey(device.getProductKey());
//            vo.setDeviceName(device.getDeviceName());
//            vo.setIdentifier(identifier);
////            vo.setName(property.getName());
////            vo.setDataType(property.getDataType().getType());
//            vo.setUpdateTime(device.getUpdateTime());
//            vo.setValue(value.toString());
//            list.add(vo);
//        });
//        return list;
//    }
}
