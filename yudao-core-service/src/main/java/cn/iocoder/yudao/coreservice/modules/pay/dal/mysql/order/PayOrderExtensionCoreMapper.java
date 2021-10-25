package cn.iocoder.yudao.coreservice.modules.pay.dal.mysql.order;

import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderExtensionCoreMapper extends BaseMapperX<PayOrderExtensionDO> {

    default PayOrderExtensionDO selectByOrderExtensionNo(String orderExtensionNo) {
        return selectOne("order_extension_no", orderExtensionNo);
    }

    default int updateByIdAndStatus(Long id, Integer status, PayOrderExtensionDO update) {
        return update(update, new QueryWrapper<PayOrderExtensionDO>()
                .eq("id", id).eq("status", status));
    }

}
