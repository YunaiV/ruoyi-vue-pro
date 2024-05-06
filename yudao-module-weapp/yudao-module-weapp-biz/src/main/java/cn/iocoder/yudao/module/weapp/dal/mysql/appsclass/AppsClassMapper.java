package cn.iocoder.yudao.module.weapp.dal.mysql.appsclass;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.weapp.dal.dataobject.appsclass.AppsClassDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.weapp.controller.admin.appsclass.vo.*;

/**
 * 小程序分类 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AppsClassMapper extends BaseMapperX<AppsClassDO> {

    default PageResult<AppsClassDO> selectPage(AppsClassPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AppsClassDO>()
                .likeIfPresent(AppsClassDO::getClassName, reqVO.getClassName())
                .eqIfPresent(AppsClassDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AppsClassDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AppsClassDO::getId));
    }

}