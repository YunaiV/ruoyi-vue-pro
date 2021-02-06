package cn.iocoder.dashboard.modules.system.dal.mysql.dao.test;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.test.SysTestDemoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.dashboard.modules.system.controller.test.vo.SysTestDemoPageReqVO;

/**
* 字典类型 Mapper
*
* @author 芋艿
*/
@Mapper
public interface SysTestDemoMapper extends BaseMapperX<SysTestDemoDO> {

    default PageResult<SysTestDemoDO> selectPage(SysTestDemoPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysTestDemoDO>()
            .likeIfPresent("name", reqVO.getName())
            .eqIfPresent("dict_type", reqVO.getDictType())
            .eqIfPresent("status", reqVO.getStatus())
            .eqIfPresent("remark", reqVO.getRemark())
            .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
        );
    }

}
