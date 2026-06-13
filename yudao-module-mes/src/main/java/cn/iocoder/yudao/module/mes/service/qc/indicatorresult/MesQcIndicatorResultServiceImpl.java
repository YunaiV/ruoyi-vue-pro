package cn.iocoder.yudao.module.mes.service.qc.indicatorresult;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicatorresult.vo.MesQcIndicatorResultSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicatorresult.MesQcIndicatorResultDetailDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.indicatorresult.MesQcIndicatorResultMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcResultValueTypeEnum;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcTypeEnum;
import cn.iocoder.yudao.module.mes.service.qc.indicator.MesQcIndicatorService;
import cn.iocoder.yudao.module.mes.service.qc.ipqc.MesQcIpqcService;
import cn.iocoder.yudao.module.mes.service.qc.iqc.MesQcIqcService;
import cn.iocoder.yudao.module.mes.service.qc.oqc.MesQcOqcService;
import cn.iocoder.yudao.module.mes.service.qc.rqc.MesQcRqcService;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.net.URI;
import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 检验结果 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcIndicatorResultServiceImpl implements MesQcIndicatorResultService {

    @Resource
    private MesQcIndicatorResultMapper resultMapper;

    @Resource
    private MesQcIndicatorResultDetailService resultDetailService;
    @Resource
    private MesQcIndicatorService indicatorService;
    @Resource
    private MesQcIqcService iqcService;
    @Resource
    @Lazy
    private MesQcIpqcService ipqcService;
    @Resource
    @Lazy
    private MesQcOqcService oqcService;
    @Resource
    @Lazy
    private MesQcRqcService rqcService;

    @Resource
    private DictDataApi dictDataApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createIndicatorResult(MesQcIndicatorResultSaveReqVO createReqVO) {
        // 1.1 根据 qcType 查询源质检单，获取 itemId
        Long itemId = getItemIdFromQcDoc(createReqVO.getQcId(), createReqVO.getQcType());
        // 1.2 校验明细数据（indicatorId 存在性 + 值格式）
        validateItemsSaveData(createReqVO.getItems());

        // 2.1 插入主表
        MesQcIndicatorResultDO result = BeanUtils.toBean(createReqVO, MesQcIndicatorResultDO.class);
        result.setItemId(itemId);
        resultMapper.insert(result);
        // 2.2 批量插入明细
        List<MesQcIndicatorResultDetailDO> details = BeanUtils.toBean(createReqVO.getItems(),
                MesQcIndicatorResultDetailDO.class);
        details.forEach(detail -> detail.setResultId(result.getId()));
        resultDetailService.createDetailList(details);
        return result.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateIndicatorResult(MesQcIndicatorResultSaveReqVO updateReqVO) {
        // 1.1 校验存在
        validateIndicatorResultExists(updateReqVO.getId());
        // 1.2 校验明细数据（indicatorId 存在性 + 值格式）
        validateItemsSaveData(updateReqVO.getItems());

        // 2.1 更新主表（锁定 qcId/qcType/itemId，不允许改挂到其他质检单）
        MesQcIndicatorResultDO updateObj = BeanUtils.toBean(updateReqVO, MesQcIndicatorResultDO.class);
        updateObj.setQcId(null).setQcType(null).setItemId(null);
        resultMapper.updateById(updateObj);
        // 2.2 批量更新明细
        List<MesQcIndicatorResultDetailDO> details = BeanUtils.toBean(updateReqVO.getItems(),
                MesQcIndicatorResultDetailDO.class);
        resultDetailService.createOrUpdateDetailList(details);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteIndicatorResult(Long id) {
        // 1. 校验存在
        validateIndicatorResultExists(id);

        // 2.1 级联删除明细
        resultDetailService.deleteDetailByResultId(id);
        // 2.2 删除主表
        resultMapper.deleteById(id);
    }

    @Override
    public MesQcIndicatorResultDO getIndicatorResult(Long id) {
        return resultMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcIndicatorResultDO> getIndicatorResultPage(MesQcIndicatorResultPageReqVO pageReqVO) {
        return resultMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesQcIndicatorResultDetailDO> getIndicatorResultDetailListByResultId(Long resultId) {
        return resultDetailService.getDetailListByResultId(resultId);
    }

    @Override
    public Long getIndicatorResultCountByQcIdAndType(Long qcId, Integer qcType) {
        return resultMapper.selectCountByQcIdAndType(qcId, qcType);
    }

    @Override
    public void validateIndicatorResultExistsByQcIdAndType(Long qcId, Integer qcType) {
        Long resultCount = getIndicatorResultCountByQcIdAndType(qcId, qcType);
        if (resultCount == 0) {
            throw exception(QC_FINISH_INDICATOR_RESULT_REQUIRED);
        }
    }

    // ==================== 私有方法 ====================

    private MesQcIndicatorResultDO validateIndicatorResultExists(Long id) {
        MesQcIndicatorResultDO result = resultMapper.selectById(id);
        if (result == null) {
            throw exception(QC_RESULT_NOT_EXISTS);
        }
        return result;
    }

    /**
     * 校验明细保存数据（indicatorId 存在性 + 值格式）
     */
    private void validateItemsSaveData(List<MesQcIndicatorResultSaveReqVO.Item> items) {
        // 校验所有明细的 indicatorId 是否存在
        Map<Long, MesQcIndicatorDO> indicatorMap = validateIndicatorIds(items);
        // 校验明细值格式是否符合指标的 resultType
        validateDetailValues(items, indicatorMap);
    }

    /**
     * 校验所有明细的 indicatorId 是否存在
     *
     * @return indicatorId -> MesQcIndicatorDO 映射，供后续值格式校验使用
     */
    private Map<Long, MesQcIndicatorDO> validateIndicatorIds(List<MesQcIndicatorResultSaveReqVO.Item> items) {
        if (CollUtil.isEmpty(items)) {
            return Collections.emptyMap();
        }
        Set<Long> indicatorIds = convertSet(items, MesQcIndicatorResultSaveReqVO.Item::getIndicatorId);
        return indicatorService.validateIndicatorListExists(indicatorIds);
    }

    /**
     * 按检测项的 resultType 校验明细值格式
     *
     * <p>FLOAT → 必须可解析为 BigDecimal；INTEGER → 必须可解析为整数；
     * DICT → 必须属于字典值域；FILE → 必须为 http/https URL；TEXT → 放行
     */
    private void validateDetailValues(List<MesQcIndicatorResultSaveReqVO.Item> items,
                                      Map<Long, MesQcIndicatorDO> indicatorMap) {
        if (CollUtil.isEmpty(items)) {
            return;
        }
        for (MesQcIndicatorResultSaveReqVO.Item item : items) {
            if (item.getIndicatorId() == null || StrUtil.isBlank(item.getValue())) {
                continue;
            }
            MesQcIndicatorDO indicator = indicatorMap.get(item.getIndicatorId());
            if (indicator == null || indicator.getResultType() == null) {
                continue;
            }
            Integer resultType = indicator.getResultType();
            if (Objects.equals(resultType, MesQcResultValueTypeEnum.FLOAT.getType())) {
                try {
                    new BigDecimal(item.getValue());
                } catch (NumberFormatException e) {
                    throw exception(QC_RESULT_VALUE_FORMAT_INVALID,
                            "检测项[" + indicator.getName() + "]要求浮点数，实际值=" + item.getValue());
                }
            } else if (Objects.equals(resultType, MesQcResultValueTypeEnum.INTEGER.getType())) {
                try {
                    Long.parseLong(item.getValue());
                } catch (NumberFormatException e) {
                    throw exception(QC_RESULT_VALUE_FORMAT_INVALID,
                            "检测项[" + indicator.getName() + "]要求整数，实际值=" + item.getValue());
                }
            }
            // DICT：校验值属于对应字典类型
            if (Objects.equals(resultType, MesQcResultValueTypeEnum.DICT.getType())) {
                String dictType = indicator.getResultSpecification();
                if (StrUtil.isNotBlank(dictType)) {
                    dictDataApi.validateDictDataList(dictType, Collections.singleton(item.getValue()));
                }
            }
            if (Objects.equals(resultType, MesQcResultValueTypeEnum.FILE.getType())
                    && !isHttpUrl(item.getValue())) {
                throw exception(QC_RESULT_VALUE_FORMAT_INVALID,
                        "检测项[" + indicator.getName() + "]要求文件 URL，实际值=" + item.getValue());
            }
            // TEXT 不做格式校验
        }
    }

    private boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return StrUtil.isNotBlank(uri.getHost())
                    && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 根据 qcType 查询源质检单，获取 itemId
     */
    private Long getItemIdFromQcDoc(Long qcId, Integer qcType) {
        if (Objects.equals(qcType, MesQcTypeEnum.IQC.getType())) {
            MesQcIqcDO iqc = iqcService.validateIqcExists(qcId);
            return iqc.getItemId();
        } else if (Objects.equals(qcType, MesQcTypeEnum.IPQC.getType())) {
            MesQcIpqcDO ipqc = ipqcService.validateIpqcExists(qcId);
            return ipqc.getItemId();
        } else if (Objects.equals(qcType, MesQcTypeEnum.OQC.getType())) {
            MesQcOqcDO oqc = oqcService.validateOqcExists(qcId);
            return oqc.getItemId();
        } else if (Objects.equals(qcType, MesQcTypeEnum.RQC.getType())) {
            MesQcRqcDO rqc = rqcService.validateRqcExists(qcId);
            return rqc.getItemId();
        }
        throw new IllegalArgumentException("暂不支持 qcType=" + qcType);
    }

}
