package cn.iocoder.yudao.module.iot.convert.ota;

import cn.hutool.core.convert.Convert;
import cn.iocoder.yudao.module.iot.dal.dataobject.device.IotDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaUpgradeTaskDO;
import cn.iocoder.yudao.module.iot.enums.ota.IotOtaUpgradeRecordStatusEnum;
import cn.iocoder.yudao.module.iot.service.ota.bo.upgrade.record.IotOtaUpgradeRecordCreateReqBO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface IotOtaUpgradeRecordConvert {

    IotOtaUpgradeRecordConvert INSTANCE = Mappers.getMapper(IotOtaUpgradeRecordConvert.class);

    default List<IotOtaUpgradeRecordCreateReqBO> convertBOList(IotOtaUpgradeTaskDO upgradeTask, IotOtaFirmwareDO firmware, List<IotDeviceDO> deviceList) {
        return deviceList.stream().map(device -> {
            IotOtaUpgradeRecordCreateReqBO createReqBO = new IotOtaUpgradeRecordCreateReqBO();
            createReqBO.setFirmwareId(firmware.getId());
            createReqBO.setTaskId(upgradeTask.getId());
            createReqBO.setProductKey(device.getProductKey());
            createReqBO.setDeviceName(device.getDeviceName());
            createReqBO.setDeviceId(Convert.toStr(device.getId()));
            createReqBO.setFromFirmwareId(Convert.toLong(device.getFirmwareId()));
            createReqBO.setStatus(IotOtaUpgradeRecordStatusEnum.PENDING.getStatus());
            createReqBO.setProgress(0);
            return createReqBO;
        }).toList();
    }

}
