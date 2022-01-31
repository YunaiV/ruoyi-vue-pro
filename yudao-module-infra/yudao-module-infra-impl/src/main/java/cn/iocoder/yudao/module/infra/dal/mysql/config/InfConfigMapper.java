package cn.iocoder.yudao.module.infra.dal.mysql.config;

import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.config.InfConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InfConfigMapper extends BaseMapperX<InfConfigDO> {

    default InfConfigDO selectByKey(String key) {
        return selectOne(new QueryWrapper<InfConfigDO>().eq("`key`", key));
    }

    default PageResult<InfConfigDO> selectPage(ConfigPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<InfConfigDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`key`", reqVO.getKey())
                .eqIfPresent("`type`", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

    default List<InfConfigDO> selectList(ConfigExportReqVO reqVO) {
        return selectList(new QueryWrapperX<InfConfigDO>()
                .likeIfPresent("name", reqVO.getName())
                .likeIfPresent("`key`", reqVO.getKey())
                .eqIfPresent("`type`", reqVO.getType())
                .betweenIfPresent("create_time", reqVO.getBeginTime(), reqVO.getEndTime()));
    }

}
