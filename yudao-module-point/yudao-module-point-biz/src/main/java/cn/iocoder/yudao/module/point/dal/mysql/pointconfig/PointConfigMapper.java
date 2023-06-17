package cn.iocoder.yudao.module.point.dal.mysql.pointconfig;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;

/**
 * 积分设置 Mapper
 *
 * @author QingX
 */
@Mapper
public interface PointConfigMapper extends BaseMapperX<PointConfigDO> {

    default PageResult<PointConfigDO> selectPage(PointConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PointConfigDO>()
                .eqIfPresent(PointConfigDO::getTradeDeductEnable, reqVO.getTradeDeductEnable())
                .orderByDesc(PointConfigDO::getId));
    }

    default List<PointConfigDO> selectList(PointConfigExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<PointConfigDO>()
                .eqIfPresent(PointConfigDO::getTradeDeductEnable, reqVO.getTradeDeductEnable())
                .orderByDesc(PointConfigDO::getId));
    }

}
