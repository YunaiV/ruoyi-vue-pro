package cn.iocoder.yudao.module.pay.dal.mysql.notify;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.pay.controller.admin.notify.vo.PayNotifyTaskPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyTaskDO;
import cn.iocoder.yudao.module.pay.enums.notify.PayNotifyStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PayNotifyTaskMapper extends BaseMapperX<PayNotifyTaskDO> {

    /**
     * 获得需要通知的 PayNotifyTaskDO 记录。需要满足如下条件：
     *
     * 1. status 非成功
     * 2. nextNotifyTime 小于当前时间
     *
     * @return PayTransactionNotifyTaskDO 数组
     */
    default List<PayNotifyTaskDO> selectListByNotify() {
        return selectList(new LambdaQueryWrapper<PayNotifyTaskDO>()
                .in(PayNotifyTaskDO::getStatus, PayNotifyStatusEnum.WAITING.getStatus(),
                        PayNotifyStatusEnum.REQUEST_SUCCESS.getStatus(), PayNotifyStatusEnum.REQUEST_FAILURE.getStatus())
                .le(PayNotifyTaskDO::getNextNotifyTime, LocalDateTime.now()));
    }

    default PageResult<PayNotifyTaskDO> selectPage(PayNotifyTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayNotifyTaskDO>()
                .eqIfPresent(PayNotifyTaskDO::getAppId, reqVO.getAppId())
                .eqIfPresent(PayNotifyTaskDO::getType, reqVO.getType())
                .eqIfPresent(PayNotifyTaskDO::getDataId, reqVO.getDataId())
                .eqIfPresent(PayNotifyTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(PayNotifyTaskDO::getMerchantOrderId, reqVO.getMerchantOrderId())
                .betweenIfPresent(PayNotifyTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PayNotifyTaskDO::getId));
    }

}
