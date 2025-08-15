package cn.iocoder.yudao.module.system.dal.mysql.group;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.group.GroupDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.system.controller.admin.group.vo.*;

/**
 * 单表生成用户组 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface GroupMapper extends BaseMapperX<GroupDO> {

    default PageResult<GroupDO> selectPage(GroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GroupDO>()
                .likeIfPresent(GroupDO::getName, reqVO.getName())
                .eqIfPresent(GroupDO::getDescription, reqVO.getDescription())
                .eqIfPresent(GroupDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(GroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GroupDO::getId));
    }

}