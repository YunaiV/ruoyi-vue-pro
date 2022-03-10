package cn.iocoder.yudao.module.infra.dal.mysql.codegen;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodegenTableMapper extends BaseMapperX<CodegenTableDO> {

    default CodegenTableDO selectByTableName(String tableName) {
        return selectOne(new QueryWrapper<CodegenTableDO>().eq("table_name", tableName));
    }

    default PageResult<CodegenTableDO> selectPage(CodegenTablePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new QueryWrapperX<CodegenTableDO>()
                .likeIfPresent("table_name", pageReqVO.getTableName())
                .likeIfPresent("table_comment", pageReqVO.getTableComment())
                .betweenIfPresent("create_time", pageReqVO.getBeginCreateTime(), pageReqVO.getEndCreateTime()));
    }

}
