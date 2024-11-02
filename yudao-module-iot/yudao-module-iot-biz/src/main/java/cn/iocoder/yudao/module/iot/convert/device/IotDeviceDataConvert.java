package cn.iocoder.yudao.module.iot.convert.device;

import cn.iocoder.yudao.module.iot.controller.admin.device.vo.deviceData.IotDeviceDataRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelEvent;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelProperty;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.thingModel.ThingModelService;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import cn.iocoder.yudao.module.iot.enums.product.IotProductFunctionTypeEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface IotDeviceDataConvert {

    IotDeviceDataConvert INSTANCE = Mappers.getMapper(IotDeviceDataConvert.class);

//    default  List<IotDeviceDataRespVO> convert(Map<String, Object> deviceData, IotDeviceDO device){
//        List<IotDeviceDataRespVO> list = new ArrayList<>();
//        deviceData.forEach((identifier, value) -> {
////            ThingModelProperty property = ThingModelService.INSTANCE.getProperty(device.getProductId(), identifier);
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
