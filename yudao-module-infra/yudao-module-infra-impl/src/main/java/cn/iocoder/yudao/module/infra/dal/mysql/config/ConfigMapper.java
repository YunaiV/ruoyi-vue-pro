package cn.iocoder.yudao.module.infra.dal.mysql.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ConfigMapper extends BaseMapperX<ConfigDO> {

    default ConfigDO selectByKey(String key) {
        return selectOne(new QueryWrapper<ConfigDO>().eq("`key`", key));
    }

    default PageResult<ConfigDO> selectPage(ConfigPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<ConfigDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`key`", reqVO.getKey())
                .eqIfPresent("`type`", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default List<ConfigDO> selectList(ConfigExportReqVO reqVO) {
        return selectList(new QueryWrapperX<ConfigDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`key`", reqVO.getKey())
                .eqIfPresent("`type`", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

}
