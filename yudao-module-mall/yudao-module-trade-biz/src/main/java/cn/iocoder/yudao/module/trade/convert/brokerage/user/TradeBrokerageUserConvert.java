package cn.iocoder.yudao.module.trade.convert.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.TradeBrokerageUserRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 分销用户 Convert
 *
 * @author owen
 */
@Mapper
public interface TradeBrokerageUserConvert {

    TradeBrokerageUserConvert INSTANCE = Mappers.getMapper(TradeBrokerageUserConvert.class);

    TradeBrokerageUserRespVO convert(TradeBrokerageUserDO bean);

    List<TradeBrokerageUserRespVO> convertList(List<TradeBrokerageUserDO> list);

    PageResult<TradeBrokerageUserRespVO> convertPage(PageResult<TradeBrokerageUserDO> page);

}
