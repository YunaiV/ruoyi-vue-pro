package cn.iocoder.yudao.module.iot.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ota.vo.firmware.IotOtaFirmwarePageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ota.IotOtaFirmwareDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IotOtaFirmwareMapper extends BaseMapperX<IotOtaFirmwareDO> {

    /**
     * 根据产品ID和固件版本号查询固件信息列表。
     *
     * @param productId 产品ID，用于筛选固件信息。
     * @param version   固件版本号，用于筛选固件信息。
     * @return 返回符合条件的固件信息列表。
     */
    default List<IotOtaFirmwareDO> selectByProductIdAndVersion(Long productId, String version) {
        return selectList(IotOtaFirmwareDO::getProductId, productId,
                IotOtaFirmwareDO::getVersion, version);
    }

    default PageResult<IotOtaFirmwareDO> selectPage(IotOtaFirmwarePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<IotOtaFirmwareDO>()
                .likeIfPresent(IotOtaFirmwareDO::getName, pageReqVO.getName())
                .eqIfPresent(IotOtaFirmwareDO::getProductId, pageReqVO.getProductId())
                .betweenIfPresent(IotOtaFirmwareDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(IotOtaFirmwareDO::getCreateTime));
    }

}
