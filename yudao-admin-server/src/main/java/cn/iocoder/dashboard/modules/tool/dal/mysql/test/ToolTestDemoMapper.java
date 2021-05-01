package cn.iocoder.dashboard.modules.tool.dal.mysql.test;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoExportReqVO;
import cn.iocoder.dashboard.modules.tool.controller.test.vo.ToolTestDemoPageReqVO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.test.ToolTestDemoDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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
        );
    }

    default List<ToolTestDemoDO> selectList(ToolTestDemoExportReqVO reqVO) {
        return selectList(new QueryWrapperX<ToolTestDemoDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
        );
    }

}
