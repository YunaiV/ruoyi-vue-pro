package cn.iocoder.yudao.module.mes.service.qc.indicator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.indicator.vo.MesQcIndicatorSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.indicator.MesQcIndicatorDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.indicator.MesQcIndicatorMapper;
import cn.iocoder.yudao.module.mes.enums.qc.MesQcResultValueTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 质检指标 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcIndicatorServiceImpl implements MesQcIndicatorService {

    @Resource
    private MesQcIndicatorMapper indicatorMapper;

    @Override
    public Long createIndicator(MesQcIndicatorSaveReqVO createReqVO) {
        // 校验相关参数
        validateIndicatorSaveData(null, createReqVO);

        // 插入
        MesQcIndicatorDO indicator = BeanUtils.toBean(createReqVO, MesQcIndicatorDO.class);
        indicatorMapper.insert(indicator);
        return indicator.getId();
    }

    @Override
    public void updateIndicator(MesQcIndicatorSaveReqVO updateReqVO) {
        // 校验存在
        validateIndicatorExists(updateReqVO.getId());
        // 校验相关参数
        validateIndicatorSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        MesQcIndicatorDO updateObj = BeanUtils.toBean(updateReqVO, MesQcIndicatorDO.class);
        indicatorMapper.updateById(updateObj);
    }

    @Override
    public void deleteIndicator(Long id) {
        // 校验存在
        validateIndicatorExists(id);
        // 删除
        indicatorMapper.deleteById(id);
    }

    private void validateIndicatorSaveData(Long id, MesQcIndicatorSaveReqVO saveReqVO) {
        // 校验编码唯一
        validateIndicatorCodeUnique(id, saveReqVO.getCode());
        // 校验名称唯一
        validateIndicatorNameUnique(id, saveReqVO.getName());
        // 校验结果值属性
        validateResultSpecification(saveReqVO.getResultType(), saveReqVO.getResultSpecification());
    }

    private void validateIndicatorExists(Long id) {
        if (indicatorMapper.selectById(id) == null) {
            throw exception(QC_INDICATOR_NOT_EXISTS);
        }
    }

    private void validateIndicatorCodeUnique(Long id, String code) {
        MesQcIndicatorDO indicator = indicatorMapper.selectByCode(code);
        if (indicator == null) {
            return;
        }
        if (ObjUtil.notEqual(indicator.getId(), id)) {
            throw exception(QC_INDICATOR_CODE_DUPLICATE);
        }
    }

    private void validateIndicatorNameUnique(Long id, String name) {
        MesQcIndicatorDO indicator = indicatorMapper.selectByName(name);
        if (indicator == null) {
            return;
        }
        if (ObjUtil.notEqual(indicator.getId(), id)) {
            throw exception(QC_INDICATOR_NAME_DUPLICATE);
        }
    }

    private void validateResultSpecification(Integer resultType, String resultSpecification) {
        if (ObjectUtils.equalsAny(resultType, MesQcResultValueTypeEnum.FILE.getType(),
                MesQcResultValueTypeEnum.DICT.getType())) {
            if (StrUtil.isBlank(resultSpecification)) {
                throw exception(QC_INDICATOR_RESULT_SPECIFICATION_REQUIRED);
            }
        }
    }

    @Override
    public MesQcIndicatorDO getIndicator(Long id) {
        return indicatorMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcIndicatorDO> getIndicatorPage(MesQcIndicatorPageReqVO pageReqVO) {
        return indicatorMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesQcIndicatorDO> getIndicatorList() {
        return indicatorMapper.selectList();
    }

    @Override
    public List<MesQcIndicatorDO> getIndicatorList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return indicatorMapper.selectByIds(ids);
    }

}
