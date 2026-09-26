package cn.iocoder.yudao.module.mes.service.md.autocode;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.md.autocode.vo.part.MesMdAutoCodePartSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodePartDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.autocode.MesMdAutoCodeRuleDO;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodePartMapper;
import cn.iocoder.yudao.module.mes.dal.mysql.md.autocode.MesMdAutoCodeRuleMapper;
import cn.iocoder.yudao.module.mes.enums.md.autocode.MesMdAutoCodePartTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.AUTO_CODE_PART_FIXED_CHAR_DUPLICATE;
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
    private MesMdAutoCodeRuleMapper ruleMapper;

    @Resource
    private MesMdAutoCodeRuleService ruleService;

    @Override
    public Long createAutoCodePart(MesMdAutoCodePartSaveReqVO createReqVO) {
        // 校验规则存在
        ruleService.validateAutoCodeRuleExists(createReqVO.getRuleId());
        // 校验流水号分段唯一性
        validateSerialNumberPartUnique(null, createReqVO.getRuleId(), createReqVO.getType());
        // 校验固定字符（编码前缀）与其他规则不重复
        validateFixedCharUnique(null, createReqVO);

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
        // 校验固定字符（编码前缀）与其他规则不重复
        validateFixedCharUnique(updateReqVO.getId(), updateReqVO);

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

    /**
     * 校验固定字符（编码前缀）与其他编码规则不重复
     *
     * 规则的前缀＝该规则全部固定字符分段（按分段排序、截取到分段长度内的有效值）拼接。
     * 两条规则前缀相同（含均无固定字符分段）时，生成的编码无法区分归属业务，保存时拒绝，
     * 从源头避免不同规则生成相同编码；生成编码时的全局查重仍保留，兜底存量配置的冲突。
     */
    private void validateFixedCharUnique(Long id, MesMdAutoCodePartSaveReqVO reqVO) {
        // 1. 全租户固定字符分段，按规则分组
        Map<Long, List<MesMdAutoCodePartDO>> fixedPartsByRuleId = partMapper.selectFixedCharPartList().stream()
                .collect(Collectors.groupingBy(MesMdAutoCodePartDO::getRuleId));
        // 2. 本次保存后，本规则的前缀指纹（排除正在修改的分段，加入本次保存的分段）
        List<MesMdAutoCodePartDO> myFixedParts = new ArrayList<>(
                fixedPartsByRuleId.getOrDefault(reqVO.getRuleId(), Collections.emptyList()));
        myFixedParts.removeIf(part -> ObjUtil.equal(id, part.getId()));
        if (MesMdAutoCodePartTypeEnum.FIXED_CHAR.getType().equals(reqVO.getType())) {
            myFixedParts.add(BeanUtils.toBean(reqVO, MesMdAutoCodePartDO.class));
        }
        String myFingerprint = buildFixedCharFingerprint(myFixedParts);
        // 3. 与租户内其他未删除规则的前缀指纹逐一比对
        for (MesMdAutoCodeRuleDO rule : ruleMapper.selectList()) {
            if (ObjUtil.equal(rule.getId(), reqVO.getRuleId())) {
                continue;
            }
            List<MesMdAutoCodePartDO> otherFixedParts = fixedPartsByRuleId.getOrDefault(rule.getId(), Collections.emptyList());
            if (myFingerprint.equals(buildFixedCharFingerprint(otherFixedParts))) {
                throw exception(AUTO_CODE_PART_FIXED_CHAR_DUPLICATE);
            }
        }
    }

    /**
     * 构建前缀指纹：固定字符分段按分段排序，拼接截取到分段长度内的固定字符（空值按空串）
     */
    private String buildFixedCharFingerprint(List<MesMdAutoCodePartDO> fixedParts) {
        return fixedParts.stream()
                .sorted(Comparator.comparing(MesMdAutoCodePartDO::getSort))
                .map(part -> StrUtil.sub(StrUtil.emptyToDefault(part.getFixCharacter(), ""), 0, part.getLength()))
                .collect(Collectors.joining());
    }

}
