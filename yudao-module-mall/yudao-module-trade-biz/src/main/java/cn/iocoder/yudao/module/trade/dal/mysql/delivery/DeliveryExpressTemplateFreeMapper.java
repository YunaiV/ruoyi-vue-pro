package cn.iocoder.yudao.module.trade.dal.mysql.delivery;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateChargeDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.delivery.DeliveryExpressTemplateFreeDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
public interface DeliveryExpressTemplateFreeMapper extends BaseMapperX<DeliveryExpressTemplateFreeDO> {

    @Repository
    class BatchInsertMapper extends ServiceImpl<DeliveryExpressTemplateFreeMapper, DeliveryExpressTemplateFreeDO> {
    }

    default List<DeliveryExpressTemplateFreeDO> selectListByTemplateId(Long templateId) {
        return selectList(new LambdaQueryWrapper<DeliveryExpressTemplateFreeDO>()
                .eq(DeliveryExpressTemplateFreeDO::getTemplateId, templateId));
    }

    default int deleteByTemplateId(Long templateId) {
        return delete(new LambdaQueryWrapper<DeliveryExpressTemplateFreeDO>()
                .eq(DeliveryExpressTemplateFreeDO::getTemplateId, templateId));
    }
}




