package cn.iocoder.yudao.module.wms.service.exchange.defective;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectivePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.defective.vo.WmsExchangeDefectiveSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.defective.WmsExchangeDefectiveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_DEFECTIVE_NOT_EXISTS;

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

    /**
     * @sign : D8D21927B4E9471E
     */
    @Override
    public WmsExchangeDefectiveDO createExchangeDefective(WmsExchangeDefectiveSaveReqVO createReqVO) {
        // 插入
        WmsExchangeDefectiveDO exchangeDefective = BeanUtils.toBean(createReqVO, WmsExchangeDefectiveDO.class);
        exchangeDefectiveMapper.insert(exchangeDefective);
        // 返回
        return exchangeDefective;
    }

    /**
     * @sign : B57F47DB56BDCCB1
     */
    @Override
    public WmsExchangeDefectiveDO updateExchangeDefective(WmsExchangeDefectiveSaveReqVO updateReqVO) {
        // 校验存在
        WmsExchangeDefectiveDO exists = validateExchangeDefectiveExists(updateReqVO.getId());
        // 更新
        WmsExchangeDefectiveDO exchangeDefective = BeanUtils.toBean(updateReqVO, WmsExchangeDefectiveDO.class);
        exchangeDefectiveMapper.updateById(exchangeDefective);
        // 返回
        return exchangeDefective;
    }

    /**
     * @sign : E254BEA012EE672A
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExchangeDefective(Long id) {
        // 校验存在
        WmsExchangeDefectiveDO exchangeDefective = validateExchangeDefectiveExists(id);
        // 删除
        exchangeDefectiveMapper.deleteById(id);
    }

    /**
     * @sign : 74CED14CB2129470
     */
    private WmsExchangeDefectiveDO validateExchangeDefectiveExists(Long id) {
        WmsExchangeDefectiveDO exchangeDefective = exchangeDefectiveMapper.selectById(id);
        if (exchangeDefective == null) {
            throw exception(EXCHANGE_DEFECTIVE_NOT_EXISTS);
        }
        return exchangeDefective;
    }

    @Override
    public WmsExchangeDefectiveDO getExchangeDefective(Long id) {
        return exchangeDefectiveMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeDefectiveDO> getExchangeDefectivePage(WmsExchangeDefectivePageReqVO pageReqVO) {
        return exchangeDefectiveMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsExchangeDefectiveDO
     */
    public List<WmsExchangeDefectiveDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return exchangeDefectiveMapper.selectByIds(idList);
    }

    @Override
    public List<WmsExchangeDefectiveDO> selectByExchangeId(Long id) {
        return exchangeDefectiveMapper.selectByExchangeId(id);
    }
}