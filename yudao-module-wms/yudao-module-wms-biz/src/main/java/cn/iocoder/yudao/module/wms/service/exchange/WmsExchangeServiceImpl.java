package cn.iocoder.yudao.module.wms.service.exchange;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.WmsExchangeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 换货单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsExchangeServiceImpl implements WmsExchangeService {

    @Resource
    private WmsExchangeMapper exchangeMapper;

    @Override
    public Long createExchange(WmsExchangeSaveReqVO createReqVO) {
        // 插入
        WmsExchangeDO exchange = BeanUtils.toBean(createReqVO, WmsExchangeDO.class);
        exchangeMapper.insert(exchange);
        // 返回
        return exchange.getId();
    }

    @Override
    public void updateExchange(WmsExchangeSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeExists(updateReqVO.getId());
        // 更新
        WmsExchangeDO updateObj = BeanUtils.toBean(updateReqVO, WmsExchangeDO.class);
        exchangeMapper.updateById(updateObj);
    }

    @Override
    public void deleteExchange(Long id) {
        // 校验存在
        validateExchangeExists(id);
        // 删除
        exchangeMapper.deleteById(id);
    }

    private void validateExchangeExists(Long id) {
        if (exchangeMapper.selectById(id) == null) {
            // throw exception(EXCHANGE_NOT_EXISTS);
        }
    }

    @Override
    public WmsExchangeDO getExchange(Long id) {
        return exchangeMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeDO> getExchangePage(WmsExchangePageReqVO pageReqVO) {
        return exchangeMapper.selectPage(pageReqVO);
    }

}