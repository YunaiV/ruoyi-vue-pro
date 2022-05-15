package cn.iocoder.yudao.module.product.dal.mysql.attrvalue;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.attrvalue.AttrValueDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.product.controller.admin.attrvalue.vo.*;

/**
 * 规格值 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AttrValueMapper extends BaseMapperX<AttrValueDO> {

    default PageResult<AttrValueDO> selectPage(AttrValuePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AttrValueDO>()
                .betweenIfPresent(AttrValueDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .eqIfPresent(AttrValueDO::getAttrKeyId, reqVO.getAttrKeyId())
                .likeIfPresent(AttrValueDO::getAttrValueName, reqVO.getAttrValueName())
                .eqIfPresent(AttrValueDO::getStatus, reqVO.getStatus())
                .orderByDesc(AttrValueDO::getId));
    }

    default List<AttrValueDO> selectList(AttrValueExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<AttrValueDO>()
                .betweenIfPresent(AttrValueDO::getCreateTime, reqVO.getBeginCreateTime(), reqVO.getEndCreateTime())
                .eqIfPresent(AttrValueDO::getAttrKeyId, reqVO.getAttrKeyId())
                .likeIfPresent(AttrValueDO::getAttrValueName, reqVO.getAttrValueName())
                .eqIfPresent(AttrValueDO::getStatus, reqVO.getStatus())
                .orderByDesc(AttrValueDO::getId));
    }

}
