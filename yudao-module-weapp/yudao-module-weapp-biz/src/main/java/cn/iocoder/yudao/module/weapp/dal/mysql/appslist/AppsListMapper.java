package cn.iocoder.yudao.module.weapp.dal.mysql.appslist;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appslist.AppsListDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.weapp.controller.admin.appslist.vo.*;

/**
 * 小程序清单 Mapper
 *
 * @author jingjianqian
 */
@Mapper
public interface AppsListMapper extends BaseMapperX<AppsListDO> {
    //默认翻页查询
    default PageResult<AppsListDO> selectPage(AppsListPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AppsListDO>()
                .likeIfPresent(AppsListDO::getWeappName, reqVO.getWeappName())
                .eqIfPresent(AppsListDO::getWeappOpenid, reqVO.getWeappOpenid())
                .eqIfPresent(AppsListDO::getClassId, reqVO.getClassId())
                .eqIfPresent(AppsListDO::getLogoImg, reqVO.getLogoImg())
                .eqIfPresent(AppsListDO::getStatus, reqVO.getStatus())
                .eqIfPresent(AppsListDO::getDescription, reqVO.getDescription())
                .orderByDesc(AppsListDO::getId));
    }

    default  List<AppsListDO> selectBannerList(Integer isBanner){
        return selectList("is_banner", isBanner);
    }
}