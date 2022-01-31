package cn.iocoder.yudao.module.tool.dal.mysql.test;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.tool.controller.admin.test.vo.TestDemoExportReqVO;
import cn.iocoder.yudao.module.tool.controller.admin.test.vo.TestDemoPageReqVO;
import cn.iocoder.yudao.module.tool.dal.dataobject.test.TestDemoDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestDemoMapper extends BaseMapperX<TestDemoDO> {

    default PageResult<TestDemoDO> selectPage(TestDemoPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<TestDemoDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

    default List<TestDemoDO> selectList(TestDemoExportReqVO reqVO) {
        return selectList(new QueryWrapperX<TestDemoDO>()
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus())
                .eqIfPresent("type", reqVO.getType())
                .eqIfPresent("category", reqVO.getCategory())
                .eqIfPresent("remark", reqVO.getRemark())
                .betweenIfPresent("create_time", reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc("id"));
    }

}
