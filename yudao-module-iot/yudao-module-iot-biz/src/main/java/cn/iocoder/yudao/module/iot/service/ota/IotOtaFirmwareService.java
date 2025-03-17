package cn.iocoder.yudao.module.iot.service.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareCreateReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwareUpdateReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;

import javax.validation.Valid;

// TODO @li：注释写的有点冗余，可以看看别的模块哈。= = AI 生成的注释，有的时候太啰嗦了，需要处理下的哈
/**
 * OTA 固件管理 Service
 *
 * @author Shelly Chan
 */
public interface IotOtaFirmwareService {

    /**
     * 创建 OTA 固件
     *
     * @param saveReqVO OTA固件保存请求对象，包含固件的相关信息
     * @return 返回新创建的固件的ID
     */
    Long createOtaFirmware(@Valid IotOtaFirmwareCreateReqVO saveReqVO);

    /**
     * 更新 OTA 固件信息
     *
     * @param updateReqVO OTA固件保存请求对象，包含需要更新的固件信息
     */
    void updateOtaFirmware(@Valid IotOtaFirmwareUpdateReqVO updateReqVO);

    /**
     * 根据 ID 获取 OTA 固件信息
     *
     * @param id OTA固件的唯一标识符
     * @return 返回OTA固件的详细信息对象
     */
    IotOtaFirmwareDO getOtaFirmware(Long id);

    /**
     * 分页查询 OTA 固件信息
     *
     * @param pageReqVO 包含分页查询条件的请求对象
     * @return 返回分页查询结果，包含固件信息列表和分页信息
     */
    PageResult<IotOtaFirmwareDO> getOtaFirmwarePage(@Valid IotOtaFirmwarePageReqVO pageReqVO);

    /**
     * 验证物联网 OTA 固件是否存在
     *
     * @param id 固件的唯一标识符
     *           该方法用于检查系统中是否存在与给定ID关联的物联网OTA固件信息
     *           主要目的是在进行固件更新操作前，确保目标固件已经存在并可以被访问
     *           如果固件不存在，该方法可能抛出异常或返回错误信息，具体行为未定义
     */
    IotOtaFirmwareDO validateFirmwareExists(Long id);

}
