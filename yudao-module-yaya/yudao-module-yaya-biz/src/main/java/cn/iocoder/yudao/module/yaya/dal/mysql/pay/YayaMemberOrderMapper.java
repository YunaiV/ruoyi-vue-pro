package cn.iocoder.yudao.module.yaya.dal.mysql.pay;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.pay.YayaMemberOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface YayaMemberOrderMapper extends BaseMapperX<YayaMemberOrderDO> {

    default YayaMemberOrderDO selectByPayOrderId(Long payOrderId) {
        return selectOne(YayaMemberOrderDO::getPayOrderId, payOrderId);
    }

}
