package cn.iocoder.yudao.module.jl.dal.mysql.join;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinCustomer2saleDO;
import org.apache.ibatis.annotations.Mapper;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;

/**
 * 客户所属的销售人员 Mapper
 *
 * @author 惟象科技
 */
@Mapper
public interface JoinCustomer2saleMapper extends BaseMapperX<JoinCustomer2saleDO> {

    default PageResult<JoinCustomer2saleDO> selectPage(JoinCustomer2salePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<JoinCustomer2saleDO>()
                .betweenIfPresent(JoinCustomer2saleDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinCustomer2saleDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(JoinCustomer2saleDO::getSalesId, reqVO.getSalesId())
                .orderByDesc(JoinCustomer2saleDO::getId));
    }

    default List<JoinCustomer2saleDO> selectList(JoinCustomer2saleExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<JoinCustomer2saleDO>()
                .betweenIfPresent(JoinCustomer2saleDO::getCreateTime, reqVO.getCreateTime())
                .eqIfPresent(JoinCustomer2saleDO::getCustomerId, reqVO.getCustomerId())
                .eqIfPresent(JoinCustomer2saleDO::getSalesId, reqVO.getSalesId())
                .orderByDesc(JoinCustomer2saleDO::getId));
    }

}
