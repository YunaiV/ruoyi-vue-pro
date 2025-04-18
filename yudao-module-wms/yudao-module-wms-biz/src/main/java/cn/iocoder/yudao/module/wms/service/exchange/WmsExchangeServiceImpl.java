package cn.iocoder.yudao.module.wms.service.exchange;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.collection.StreamX;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangePageReqVO;
import cn.iocoder.yudao.module.wms.controller.admin.exchange.vo.WmsExchangeSaveReqVO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.WmsExchangeDO;
import cn.iocoder.yudao.module.wms.dal.dataobject.exchange.defective.WmsExchangeDefectiveDO;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.WmsExchangeMapper;
import cn.iocoder.yudao.module.wms.dal.mysql.exchange.defective.WmsExchangeDefectiveMapper;
import cn.iocoder.yudao.module.wms.dal.redis.no.WmsNoRedisDAO;
import cn.iocoder.yudao.module.wms.enums.exchange.WmsExchangeAuditStatus;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_CAN_NOT_DELETE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_CAN_NOT_EDIT;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_CODE_DUPLICATE;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_DEFECTIVE_EXISTS;
import static cn.iocoder.yudao.module.wms.enums.ErrorCodeConstants.EXCHANGE_NOT_EXISTS;

/**
 * 换货单 Service 实现类
 *
 * @author 李方捷
 */
@Service
@Validated
public class WmsExchangeServiceImpl implements WmsExchangeService {

    @Resource
    @Lazy
    private WmsExchangeDefectiveMapper exchangeDefectiveMapper;

    @Resource
    private WmsNoRedisDAO noRedisDAO;

    @Resource
    private WmsExchangeMapper exchangeMapper;

    /**
     * @sign : 48FA5E8619B15D35
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsExchangeDO createExchange(WmsExchangeSaveReqVO createReqVO) {
        // 设置单据号
        String code = noRedisDAO.generate(WmsNoRedisDAO.EXCHANGE_NO_PREFIX, 3);
        createReqVO.setCode(code);
        createReqVO.setAuditStatus(WmsExchangeAuditStatus.DRAFT.getValue());
        if (exchangeMapper.getByCode(createReqVO.getCode()) != null) {
            throw exception(EXCHANGE_CODE_DUPLICATE);
        }
        // 插入
        WmsExchangeDO exchange = BeanUtils.toBean(createReqVO, WmsExchangeDO.class);
        exchangeMapper.insert(exchange);
        // 保存良次换货详情详情
        if (createReqVO.getDefectiveList() != null) {
            List<WmsExchangeDefectiveDO> toInsetList = new ArrayList<>();
            StreamX.from(createReqVO.getDefectiveList()).filter(Objects::nonNull).forEach(item -> {
                item.setId(null);
                // 设置归属
                item.setExchangeId(exchange.getId());
                toInsetList.add(BeanUtils.toBean(item, WmsExchangeDefectiveDO.class));
            });
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsExchangeDefectiveDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(EXCHANGE_DEFECTIVE_EXISTS);
            }
            exchangeDefectiveMapper.insertBatch(toInsetList);
        }
        // 返回
        return exchange;
    }

    /**
     * @sign : E0A7EE421ACD0093
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsExchangeDO updateExchange(WmsExchangeSaveReqVO updateReqVO) {
        // 校验存在
        WmsExchangeDO exists = validateExchangeExists(updateReqVO.getId());
        // 校验状态
        WmsExchangeAuditStatus auditStatus = WmsExchangeAuditStatus.parse(exists.getAuditStatus());
        if(auditStatus.matchAny(WmsExchangeAuditStatus.AUDITING,WmsExchangeAuditStatus.PASS)) {
           throw exception(EXCHANGE_CAN_NOT_EDIT);
        }
        // 单据号不允许被修改
        updateReqVO.setCode(exists.getCode());
        // 保存良次换货详情详情
        if (updateReqVO.getDefectiveList() != null) {
            List<WmsExchangeDefectiveDO> existsInDB = exchangeDefectiveMapper.selectByExchangeId(updateReqVO.getId());
            StreamX.CompareResult<WmsExchangeDefectiveDO> compareResult = StreamX.compare(existsInDB, BeanUtils.toBean(updateReqVO.getDefectiveList(), WmsExchangeDefectiveDO.class), WmsExchangeDefectiveDO::getId);
            List<WmsExchangeDefectiveDO> toInsetList = compareResult.getTargetMoreThanBaseList();
            List<WmsExchangeDefectiveDO> toUpdateList = compareResult.getIntersectionList();
            List<WmsExchangeDefectiveDO> toDeleteList = compareResult.getBaseMoreThanTargetList();
            List<WmsExchangeDefectiveDO> finalList = new ArrayList<>();
            finalList.addAll(toInsetList);
            finalList.addAll(toUpdateList);
            // 校验 toInsetList 中是否有重复的 productId
            boolean isProductIdRepeated = StreamX.isRepeated(toInsetList, WmsExchangeDefectiveDO::getProductId);
            if (isProductIdRepeated) {
                throw exception(EXCHANGE_DEFECTIVE_EXISTS);
            }
            // 设置归属
            finalList.forEach(item -> {
                item.setExchangeId(updateReqVO.getId());
            });
            // 保存详情
            exchangeDefectiveMapper.insertBatch(toInsetList);
            exchangeDefectiveMapper.updateBatch(toUpdateList);
            exchangeDefectiveMapper.deleteBatchIds(toDeleteList);
        }
        // 更新
        WmsExchangeDO exchange = BeanUtils.toBean(updateReqVO, WmsExchangeDO.class);
        exchangeMapper.updateById(exchange);
        // 返回
        return exchange;
    }

    /**
     * @sign : CA8438BC1760D4D2
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExchange(Long id) {
        // 校验存在
        WmsExchangeDO exchange = validateExchangeExists(id);
        // 校验状态
        WmsExchangeAuditStatus auditStatus = WmsExchangeAuditStatus.parse(exchange.getAuditStatus());
        if(auditStatus.matchAny(WmsExchangeAuditStatus.AUDITING,WmsExchangeAuditStatus.PASS)) {
            throw exception(EXCHANGE_CAN_NOT_DELETE);
        }
        // 唯一索引去重
        exchange.setCode(exchangeMapper.flagUKeyAsLogicDelete(exchange.getCode()));
        exchangeMapper.updateById(exchange);
        // 删除
        exchangeMapper.deleteById(id);
    }

    /**
     * @sign : B9AD7AEF52B150BB
     */
    private WmsExchangeDO validateExchangeExists(Long id) {
        WmsExchangeDO exchange = exchangeMapper.selectById(id);
        if (exchange == null) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
        return exchange;
    }

    @Override
    public WmsExchangeDO getExchange(Long id) {
        return exchangeMapper.selectById(id);
    }

    @Override
    public PageResult<WmsExchangeDO> getExchangePage(WmsExchangePageReqVO pageReqVO) {
        return exchangeMapper.selectPage(pageReqVO);
    }

    /**
     * 按 ID 集合查询 WmsExchangeDO
     */
    public List<WmsExchangeDO> selectByIds(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return List.of();
        }
        return exchangeMapper.selectByIds(idList);
    }
}
