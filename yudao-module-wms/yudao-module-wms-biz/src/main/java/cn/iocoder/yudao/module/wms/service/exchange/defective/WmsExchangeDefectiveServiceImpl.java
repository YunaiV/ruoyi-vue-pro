package cn.iocoder.yudao.module.wms.service.exchange.defective;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectivePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.defective.WmsExchangeDefectiveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 良次换货详情 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsExchangeDefectiveServiceImpl implements WmsExchangeDefectiveService {

    @Resource
    private WmsExchangeDefectiveMapper exchangeDefectiveMapper;

    @Override
    public Long createExchangeDefective(WmsExchangeDefectiveSaveReqVO createReqVO) {
        // 插入
        WmsExchangeDefectiveDO exchangeDefective = BeanUtils.toBean(createReqVO, WmsExchangeDefectiveDO.class);
        exchangeDefectiveMapper.insert(exchangeDefective);
        // 返回
        return exchangeDefective.getId();
    }

    @Override
    public void updateExchangeDefective(WmsExchangeDefectiveSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeDefectiveExists(updateReqVO.getId());
        // 更新
        WmsExchangeDefectiveDO updateObj = BeanUtils.toBean(updateReqVO, WmsExchangeDefectiveDO.class);
        exchangeDefectiveMapper.updateById(updateObj);
    }

    @Override
    public void deleteExchangeDefective(Long id) {
        // 校验存在
        validateExchangeDefectiveExists(id);
        // 删除
        exchangeDefectiveMapper.deleteById(id);
    }

    private void validateExchangeDefectiveExists(Long id) {
        if (exchangeDefectiveMapper.selectById(id) == null) {
            // throw exception(EXCHANGE_DEFECTIVE_NOT_EXISTS);
        }
    }

    @Override
    public WmsExchangeDefectiveDO getExchangeDefective(Long id) {
        return exchangeDefectiveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeDefectiveDO> getExchangeDefectivePage(WmsExchangeDefectivePageReqVO pageReqVO) {
        return exchangeDefectiveMapper.selectPage(pageReqVO);
    }

}