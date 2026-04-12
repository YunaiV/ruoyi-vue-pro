package cn.iocoder.yudao.module.im.dal.mysql.group;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.im.dal.dataobject.group.ImGroupDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.im.controller.admin.group.vo.*;

/**
 * IM 群 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImGroupMapper extends BaseMapperX<ImGroupDO> {

    default PageResult<ImGroupDO> selectPage(ImGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImGroupDO>()
                .likeIfPresent(ImGroupDO::getName, reqVO.getName())
                .eqIfPresent(ImGroupDO::getOwnerUserId, reqVO.getOwnerUserId())
                .eqIfPresent(ImGroupDO::getNotice, reqVO.getNotice())
                .betweenIfPresent(ImGroupDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ImGroupDO::getId));
    }

}