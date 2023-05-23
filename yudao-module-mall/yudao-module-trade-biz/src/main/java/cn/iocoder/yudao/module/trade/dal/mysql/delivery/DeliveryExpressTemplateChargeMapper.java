package cn.iocoder.yudao.module.trade.dal.mysql.delivery;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DeliveryExpressTemplateChargeMapper extends BaseMapperX<DeliveryExpressTemplateChargeDO> {

    @Repository
    class BatchInsertMapper extends ServiceImpl<DeliveryExpressTemplateChargeMapper, DeliveryExpressTemplateChargeDO> {
    }

    default List<DeliveryExpressTemplateChargeDO> selectListByTemplateId(Long templateId){
        return selectList(new LambdaQueryWrapper<DeliveryExpressTemplateChargeDO>()
                .eq(DeliveryExpressTemplateChargeDO::getTemplateId, templateId));
    }

    default int deleteByTemplateId(Long templateId){
       return delete(new LambdaQueryWrapper<DeliveryExpressTemplateChargeDO>()
               .eq(DeliveryExpressTemplateChargeDO::getTemplateId, templateId));
    }
}




