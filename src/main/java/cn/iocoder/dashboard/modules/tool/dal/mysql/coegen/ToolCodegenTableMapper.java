package cn.iocoder.dashboard.modules.tool.dal.mysql.coegen;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ToolCodegenTableMapper extends BaseMapperX<ToolCodegenTableDO> {

    default ToolCodegenTableDO selectByTableName(String tableName) {
        return selectOne(new QueryWrapper<ToolCodegenTableDO>().eq("table_name", tableName));
    }

    default PageResult<ToolCodegenTableDO> selectPage(ToolCodegenTablePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new QueryWrapperX<ToolCodegenTableDO>()
                .likeIfPresent("table_name", pageReqVO.getTableName())
                .likeIfPresent("table_comment", pageReqVO.getTableComment())
                .betweenIfPresent("create_time", pageReqVO.getBeginCreateTime(), pageReqVO.getEndCreateTime()));
    }

}
