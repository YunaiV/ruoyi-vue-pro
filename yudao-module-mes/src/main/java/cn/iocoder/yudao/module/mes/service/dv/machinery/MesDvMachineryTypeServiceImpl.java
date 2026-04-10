package cn.iocoder.yudao.module.mes.service.dv.machinery;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type.MesDvMachineryTypeListReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo.type.MesDvMachineryTypeSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.machinery.MesDvMachineryTypeDO;
import cn.iocoder.yudao.module.mes.dal.mysql.dv.machinery.MesDvMachineryTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 设备类型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesDvMachineryTypeServiceImpl implements MesDvMachineryTypeService {

    @Resource
    private MesDvMachineryTypeMapper machineryTypeMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private MesDvMachineryService machineryService;

    @Override
    public Long createMachineryType(MesDvMachineryTypeSaveReqVO createReqVO) {
        // 校验
        validateMachineryTypeSaveData(createReqVO);

        // 插入
        MesDvMachineryTypeDO machineryType = BeanUtils.toBean(createReqVO, MesDvMachineryTypeDO.class);
        machineryTypeMapper.insert(machineryType);
        return machineryType.getId();
    }

    @Override
    public void updateMachineryType(MesDvMachineryTypeSaveReqVO updateReqVO) {
        // 校验
        validateMachineryTypeSaveData(updateReqVO);

        // 更新
        MesDvMachineryTypeDO updateObj = BeanUtils.toBean(updateReqVO, MesDvMachineryTypeDO.class);
        machineryTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteMachineryType(Long id) {
        // 1.1 校验存在
        validateMachineryTypeExists(id);
        // 1.2 校验是否有子类型
        if (machineryTypeMapper.selectCountByParentId(id) > 0) {
            throw exception(DV_MACHINERY_TYPE_EXITS_CHILDREN);
        }
        // 1.3 校验是否有设备
        if (machineryService.getMachineryCountByMachineryTypeId(id) > 0) {
            throw exception(DV_MACHINERY_TYPE_HAS_MACHINERY);
        }

        // 2. 删除
        machineryTypeMapper.deleteById(id);
    }

    private void validateMachineryTypeSaveData(MesDvMachineryTypeSaveReqVO saveReqVO) {
        if (saveReqVO.getId() != null) {
            validateMachineryTypeExists(saveReqVO.getId());
        }
        // 校验父级节点有效性
        validateParentMachineryType(saveReqVO.getId(), saveReqVO.getParentId());
        // 校验类型名称唯一性
        validateMachineryTypeNameUnique(saveReqVO.getId(), saveReqVO.getParentId(), saveReqVO.getName());
        // 校验类型编码唯一性
        validateMachineryTypeCodeUnique(saveReqVO.getId(), saveReqVO.getParentId(), saveReqVO.getCode());
    }

    private void validateMachineryTypeExists(Long id) {
        if (machineryTypeMapper.selectById(id) == null) {
            throw exception(DV_MACHINERY_TYPE_NOT_EXISTS);
        }
    }

    private void validateParentMachineryType(Long id, Long parentId) {
        if (parentId == null || MesDvMachineryTypeDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父类型
        if (Objects.equals(id, parentId)) {
            throw exception(DV_MACHINERY_TYPE_PARENT_ERROR);
        }
        // 2. 父类型不存在
        MesDvMachineryTypeDO parentType = machineryTypeMapper.selectById(parentId);
        if (parentType == null) {
            throw exception(DV_MACHINERY_TYPE_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父类型，如果父类型是自己的子类型，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentType.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DV_MACHINERY_TYPE_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父类型
            if (parentId == null || MesDvMachineryTypeDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentType = machineryTypeMapper.selectById(parentId);
            if (parentType == null) {
                break;
            }
        }
    }

    private void validateMachineryTypeNameUnique(Long id, Long parentId, String name) {
        MesDvMachineryTypeDO machineryType = machineryTypeMapper.selectByParentIdAndName(parentId, name);
        if (machineryType == null) {
            return;
        }
        if (ObjUtil.notEqual(machineryType.getId(), id)) {
            throw exception(DV_MACHINERY_TYPE_NAME_DUPLICATE);
        }
    }

    private void validateMachineryTypeCodeUnique(Long id, Long parentId, String code) {
        MesDvMachineryTypeDO machineryType = machineryTypeMapper.selectByCode(code);
        if (machineryType == null) {
            return;
        }
        if (ObjUtil.notEqual(machineryType.getId(), id)) {
            throw exception(DV_MACHINERY_TYPE_CODE_DUPLICATE);
        }
    }

    @Override
    public MesDvMachineryTypeDO getMachineryType(Long id) {
        return machineryTypeMapper.selectById(id);
    }

    @Override
    public List<MesDvMachineryTypeDO> getMachineryTypeList(MesDvMachineryTypeListReqVO listReqVO) {
        return machineryTypeMapper.selectList(listReqVO);
    }

    @Override
    public List<MesDvMachineryTypeDO> getMachineryTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return machineryTypeMapper.selectByIds(ids);
    }

    @Override
    public List<MesDvMachineryTypeDO> getMachineryTypeChildrenList(Long parentId) {
        List<MesDvMachineryTypeDO> allList = machineryTypeMapper.selectList(); // 类型总量小，全量查
        // 1. 构建 parentId -> children 的 Map（类似 menu 的 treeNodeMap 思路）
        Map<Long, List<MesDvMachineryTypeDO>> parentChildrenMap = new LinkedHashMap<>();
        allList.forEach(type -> parentChildrenMap
                .computeIfAbsent(type.getParentId(), k -> new ArrayList<>()).add(type));
        // 2. 收集所有后代节点
        List<MesDvMachineryTypeDO> result = new ArrayList<>();
        List<Long> parentIds = new ArrayList<>();
        parentIds.add(parentId);
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            if (i >= parentIds.size()) {
                break;
            }
            List<MesDvMachineryTypeDO> children = parentChildrenMap.get(parentIds.get(i));
            if (children == null) {
                continue;
            }
            for (MesDvMachineryTypeDO child : children) {
                result.add(child);
                parentIds.add(child.getId());
            }
        }
        return result;
    }

}
