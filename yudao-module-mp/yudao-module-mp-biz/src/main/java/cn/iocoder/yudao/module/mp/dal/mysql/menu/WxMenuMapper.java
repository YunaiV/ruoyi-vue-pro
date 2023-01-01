package cn.iocoder.yudao.module.mp.dal.mysql.menu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mp.dal.dataobject.menu.WxMenuDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.mp.controller.admin.menu.vo.*;

/**
 * 微信菜单 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface WxMenuMapper extends BaseMapperX<WxMenuDO> {

    default PageResult<WxMenuDO> selectPage(WxMenuPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WxMenuDO>()
                .eqIfPresent(WxMenuDO::getParentId, reqVO.getParentId())
                .likeIfPresent(WxMenuDO::getMenuName, reqVO.getMenuName())
                .eqIfPresent(WxMenuDO::getMenuType, reqVO.getMenuType())
                .eqIfPresent(WxMenuDO::getMenuLevel, reqVO.getMenuLevel())
                .eqIfPresent(WxMenuDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxMenuDO::getMenuUrl, reqVO.getMenuUrl())
                .eqIfPresent(WxMenuDO::getMenuSort, reqVO.getMenuSort())
                .eqIfPresent(WxMenuDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxMenuDO::getMiniprogramAppid, reqVO.getMiniprogramAppid())
                .eqIfPresent(WxMenuDO::getMiniprogramPagepath, reqVO.getMiniprogramPagepath())
                .betweenIfPresent(WxMenuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxMenuDO::getId));
    }

    default List<WxMenuDO> selectList(WxMenuExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<WxMenuDO>()
                .eqIfPresent(WxMenuDO::getParentId, reqVO.getParentId())
                .likeIfPresent(WxMenuDO::getMenuName, reqVO.getMenuName())
                .eqIfPresent(WxMenuDO::getMenuType, reqVO.getMenuType())
                .eqIfPresent(WxMenuDO::getMenuLevel, reqVO.getMenuLevel())
                .eqIfPresent(WxMenuDO::getTplId, reqVO.getTplId())
                .eqIfPresent(WxMenuDO::getMenuUrl, reqVO.getMenuUrl())
                .eqIfPresent(WxMenuDO::getMenuSort, reqVO.getMenuSort())
                .eqIfPresent(WxMenuDO::getWxAccountId, reqVO.getWxAccountId())
                .eqIfPresent(WxMenuDO::getMiniprogramAppid, reqVO.getMiniprogramAppid())
                .eqIfPresent(WxMenuDO::getMiniprogramPagepath, reqVO.getMiniprogramPagepath())
                .betweenIfPresent(WxMenuDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .orderByDesc(WxMenuDO::getId));
    }

}
