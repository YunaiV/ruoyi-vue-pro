package cn.iocoder.yudao.module.mp.dal.mysql.fanstag;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.fanstag.WxFansTagDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.fanstag.vo.*;

/**
 * 粉丝标签 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxFansTagMapper extends BaseMapperX<WxFansTagDO> {

    default PageResult<WxFansTagDO> selectPage(WxFansTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxFansTagDO>()
                .likeIfPresent(WxFansTagDO::getName, reqVO.getName())
                .eqIfPresent(WxFansTagDO::getCount, reqVO.getCount())
                .eqIfPresent(WxFansTagDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxFansTagDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansTagDO::getId));
    }

    default List<WxFansTagDO> selectList(WxFansTagExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxFansTagDO>()
                .likeIfPresent(WxFansTagDO::getName, reqVO.getName())
                .eqIfPresent(WxFansTagDO::getCount, reqVO.getCount())
                .eqIfPresent(WxFansTagDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxFansTagDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxFansTagDO::getId));
    }

}
