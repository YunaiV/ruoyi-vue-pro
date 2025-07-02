package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import jakarta.validation.Valid;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OTA 固件管理 Service 接口
 *
 * @author Shelly Chan
 */
public interface IotOtaFirmwareService {

    /**
     * 创建 OTA 固件
     *
     * @param saveReqVO 固件信息
     * @return 固件编号
     */
    Long createOtaFirmware(@Valid IotOtaFirmwareCreateReqVO saveReqVO);

    /**
     * 更新 OTA 固件信息
     *
     * @param updateReqVO 固件信息
     */
    void updateOtaFirmware(@Valid IotOtaFirmwareUpdateReqVO updateReqVO);

    /**
     * 获取 OTA 固件信息
     *
     * @param id 固件编号
     * @return 固件信息
     */
    IotOtaFirmwareDO getOtaFirmware(Long id);

    /**
     * 获取 OTA 固件信息列表
     *
     * @param ids 固件编号集合
     * @return 固件信息列表
     */
    List<IotOtaFirmwareDO> getOtaFirmwareList(Collection<Long> ids);

    /**
     * 获取 OTA 固件信息 Map
     *
     * @param ids 固件编号集合
     * @return 固件信息 Map
     */
    default Map<Long, IotOtaFirmwareDO> getOtaFirmwareMap(Collection<Long> ids) {
        return convertMap(getOtaFirmwareList(ids), IotOtaFirmwareDO::getId);
    }

    /**
     * 分页查询 OTA 固件信息
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    PageResult<IotOtaFirmwareDO> getOtaFirmwarePage(@Valid IotOtaFirmwarePageReqVO pageReqVO);

    /**
     * 验证 OTA 固件是否存在
     *
     * @param id  固件编号
     * @return 固件信息
     */
    IotOtaFirmwareDO validateFirmwareExists(Long id);

}
