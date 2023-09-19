package cn.iocoder.yudao.module.trade.dal.mysql.brokerage.withdraw;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.withdraw.vo.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.withdraw.BrokerageWithdrawDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 佣金提现 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface BrokerageWithdrawMapper extends BaseMapperX<BrokerageWithdrawDO> {

    default PageResult<BrokerageWithdrawDO> selectPage(BrokerageWithdrawPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageWithdrawDO>()
                .eqIfPresent(BrokerageWithdrawDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BrokerageWithdrawDO::getType, reqVO.getType())
                .likeIfPresent(BrokerageWithdrawDO::getName, reqVO.getName())
                .eqIfPresent(BrokerageWithdrawDO::getAccountNo, reqVO.getAccountNo())
                .likeIfPresent(BrokerageWithdrawDO::getBankName, reqVO.getBankName())
                .eqIfPresent(BrokerageWithdrawDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(BrokerageWithdrawDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(BrokerageWithdrawDO::getStatus).orderByDesc(BrokerageWithdrawDO::getId));
    }

    default int updateByIdAndStatus(Integer id, Integer status, BrokerageWithdrawDO updateObj) {
        return update(updateObj, new LambdaUpdateWrapper<BrokerageWithdrawDO>()
                .eq(BrokerageWithdrawDO::getId, id)
                .eq(BrokerageWithdrawDO::getStatus, status));
    }

}
