package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order;

import java.util.*;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayRefundDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;

import org.apache.ibatis.annotations.Mapper;


/**
 * 退款订单 Mapper
 *
 */
@Mapper
public interface PayRefundMapper extends BaseMapperX<PayRefundDO> {


}
