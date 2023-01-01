package cn.iocoder.yudao.module.mp.dal.mysql.accountfanstag;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag.WxAccountFansTagDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.accountfanstag.vo.*;

/**
 * 粉丝标签关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxAccountFansTagMapper extends BaseMapperX<WxAccountFansTagDO> {

    default PageResult<WxAccountFansTagDO> selectPage(WxAccountFansTagPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxAccountFansTagDO>()
                .eqIfPresent(WxAccountFansTagDO::getOpenid, reqVO.getOpenid())
                .eqIfPresent(WxAccountFansTagDO::getTagId, reqVO.getTagId())
                .eqIfPresent(WxAccountFansTagDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxAccountFansTagDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountFansTagDO::getId));
    }

    default List<WxAccountFansTagDO> selectList(WxAccountFansTagExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxAccountFansTagDO>()
                .eqIfPresent(WxAccountFansTagDO::getOpenid, reqVO.getOpenid())
                .eqIfPresent(WxAccountFansTagDO::getTagId, reqVO.getTagId())
                .eqIfPresent(WxAccountFansTagDO::getWxAccountId, reqVO.getWxAccountId())
                .betweenIfPresent(WxAccountFansTagDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxAccountFansTagDO::getId));
    }

}
