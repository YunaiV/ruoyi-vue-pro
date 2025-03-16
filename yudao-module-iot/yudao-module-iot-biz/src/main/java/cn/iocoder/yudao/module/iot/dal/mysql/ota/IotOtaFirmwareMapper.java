package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

// TODO @li：参考 IotOtaUpgradeRecordMapper 的写法
@Mapper
public interface IotOtaFirmwareMapper extends BaseMapperX<IotOtaFirmwareDO> {

    /**
     * 根据产品ID和固件版本号查询固件信息列表。
     *
     * @param productId 产品ID，用于筛选固件信息。
     * @param version   固件版本号，用于筛选固件信息。
     * @return 返回符合条件的固件信息列表。
     */
    default List<IotOtaFirmwareDO> selectByProductIdAndVersion(String productId, String version) {
        return selectList(IotOtaFirmwareDO::getProductId, productId,
                IotOtaFirmwareDO::getVersion, version);
    }

    /**
     * 分页查询固件信息，支持根据名称和产品ID进行筛选，并按创建时间降序排列。
     *
     * @param pageReqVO 分页查询请求对象，包含分页参数和筛选条件。
     * @return 返回分页查询结果，包含符合条件的固件信息列表。
     */
    default PageResult<IotOtaFirmwareDO> selectPage(IotOtaFirmwarePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaFirmwareDO>()
                .likeIfPresent(IotOtaFirmwareDO::getName, pageReqVO.getName())
                .eqIfPresent(IotOtaFirmwareDO::getProductId, pageReqVO.getProductId())
                .orderByDesc(IotOtaFirmwareDO::getCreateTime));
    }

}
