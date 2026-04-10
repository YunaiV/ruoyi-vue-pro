package cn.iocoder.yudao.module.mes.dal.mysql.dv.maintenrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordLineDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 设备保养记录明细 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesDvMaintenRecordLineMapper extends BaseMapperX<MesDvMaintenRecordLineDO> {

    default PageResult<MesDvMaintenRecordLineDO> selectPage(MesDvMaintenRecordLinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MesDvMaintenRecordLineDO>()
                .eqIfPresent(MesDvMaintenRecordLineDO::getRecordId, reqVO.getRecordId())
                .orderByDesc(MesDvMaintenRecordLineDO::getId));
    }

    default List<MesDvMaintenRecordLineDO> selectListByRecordId(Long recordId) {
        return selectList(MesDvMaintenRecordLineDO::getRecordId, recordId);
    }

    default int deleteByRecordId(Long recordId) {
        return delete(MesDvMaintenRecordLineDO::getRecordId, recordId);
    }

}
