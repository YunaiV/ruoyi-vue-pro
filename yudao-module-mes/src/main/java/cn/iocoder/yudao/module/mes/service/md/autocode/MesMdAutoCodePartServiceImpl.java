package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodePartMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.AUTO_CODE_PART_NOT_EXISTS;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.AUTO_CODE_PART_SERIAL_NUMBER_DUPLICATE;

/**
 * MES 编码规则组成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesMdAutoCodePartServiceImpl implements MesMdAutoCodePartService {

    @Resource
    private MesMdAutoCodePartMapper partMapper;

    @Resource
    private MesMdAutoCodeRuleService ruleService;

    @Override
    public Long createAutoCodePart(MesMdAutoCodePartSaveReqVO createReqVO) {
        // 校验规则存在
        ruleService.validateAutoCodeRuleExists(createReqVO.getRuleId());
        // 校验流水号分段唯一性
        validateSerialNumberPartUnique(null, createReqVO.getRuleId(), createReqVO.getType());

        // 插入
        MesMdAutoCodePartDO part = BeanUtils.toBean(createReqVO, MesMdAutoCodePartDO.class);
        partMapper.insert(part);
        return part.getId();
    }

    @Override
    public void updateAutoCodePart(MesMdAutoCodePartSaveReqVO updateReqVO) {
        // 校验存在
        validatePartExists(updateReqVO.getId());
        // 校验规则存在
        ruleService.validateAutoCodeRuleExists(updateReqVO.getRuleId());
        // 校验流水号分段唯一性
        validateSerialNumberPartUnique(updateReqVO.getId(), updateReqVO.getRuleId(), updateReqVO.getType());

        // 更新
        MesMdAutoCodePartDO updateObj = BeanUtils.toBean(updateReqVO, MesMdAutoCodePartDO.class);
        partMapper.updateById(updateObj);
    }

    @Override
    public void deleteAutoCodePart(Long id) {
        // 校验存在
        validatePartExists(id);

        // 删除
        partMapper.deleteById(id);
    }

    @Override
    public MesMdAutoCodePartDO getAutoCodePart(Long id) {
        return partMapper.selectById(id);
    }

    @Override
    public List<MesMdAutoCodePartDO> getAutoCodePartListByRuleId(Long ruleId) {
        return partMapper.selectListByRuleId(ruleId);
    }

    // ==================== 校验方法 ====================

    private void validatePartExists(Long id) {
        if (partMapper.selectById(id) == null) {
            throw exception(AUTO_CODE_PART_NOT_EXISTS);
        }
    }

    private void validateSerialNumberPartUnique(Long id, Long ruleId, Integer type) {
        // 只有流水号类型才需要校验
        if (ObjUtil.notEqual(MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType(), type)) {
            return;
        }
        // 查询该规则下所有流水号分段
        List<MesMdAutoCodePartDO> parts = partMapper.selectListByRuleId(ruleId);
        long count = parts.stream()
                .filter(part -> MesMdAutoCodePartTypeEnum.SERIAL_NUMBER.getType().equals(part.getType()))
                .filter(part -> ObjUtil.notEqual(id, part.getId())) // 排除自己
                .count();
        if (count > 0) {
            throw exception(AUTO_CODE_PART_SERIAL_NUMBER_DUPLICATE);
        }
    }

}
