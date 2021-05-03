package cn.iocoder.yudao.adminserver.modules.tool.dal.mysql.test;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.adminserver.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.adminserver.modules.tool.controller.test.vo.*;

/**
 * 字典类型 Mapper
 *
 * @author 芋艿
 */
@Mapper
public interface ToolTestDemoMapper extends BaseMapperX<ToolTestDemoDO> {

    default PageResult<ToolTestDemoDO> selectPage(ToolTestDemoPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<ToolTestDemoDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<ToolTestDemoDO> selectList(ToolTestDemoExportReqVO reqVO) {
        return selectList(new QueryWrapperX<ToolTestDemoDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

}
