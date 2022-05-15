package cn.iocoder.yudao.module.product.dal.mysql.attrkey;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.attrkey.AttrKeyDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.attrkey.vo.*;

/**
 * 规格名称 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AttrKeyMapper extends BaseMapperX<AttrKeyDO> {

    default PageResult<AttrKeyDO> selectPage(AttrKeyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AttrKeyDO>()
                .betweenIfPresent(AttrKeyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .likeIfPresent(AttrKeyDO::getAttrName, reqVO.getAttrName())
                .eqIfPresent(AttrKeyDO::getStatus, reqVO.getStatus())
                .orderByDesc(AttrKeyDO::getId));
    }

    default List<AttrKeyDO> selectList(AttrKeyExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<AttrKeyDO>()
                .betweenIfPresent(AttrKeyDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .likeIfPresent(AttrKeyDO::getAttrName, reqVO.getAttrName())
                .eqIfPresent(AttrKeyDO::getStatus, reqVO.getStatus())
                .orderByDesc(AttrKeyDO::getId));
    }

}
