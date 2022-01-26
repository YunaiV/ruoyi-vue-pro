package cn.iocoder.yudao.adminserver.modules.system.dal.mysql.dept;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostPageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.dept.SysPostDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface SysPostMapper extends BaseMapperX<SysPostDO> {

    default List<SysPostDO> selectList(Collection<Long> ids, Collection<Integer> statuses) {
        return selectList(new QueryWrapperX<SysPostDO>().inIfPresent("id", ids)
                .inIfPresent("status", statuses));
    }

    default PageResult<SysPostDO> selectPage(SysPostPageReqVO reqVO) {
        return selectPage(reqVO, new QueryWrapperX<SysPostDO>()
                .likeIfPresent("code", reqVO.getCode())
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus()));
    }

    default List<SysPostDO> selectList(SysPostExportReqVO reqVO) {
        return selectList(new QueryWrapperX<SysPostDO>()
                .likeIfPresent("code", reqVO.getCode())
                .likeIfPresent("name", reqVO.getName())
                .eqIfPresent("status", reqVO.getStatus()));
    }

    default SysPostDO selectByName(String name) {
        return selectOne(new QueryWrapper<SysPostDO>().eq("name", name));
    }

    default SysPostDO selectByCode(String code) {
        return selectOne(new QueryWrapper<SysPostDO>().eq("code", code));
    }

}
