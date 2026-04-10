package cn.iocoder.yudao.module.mes.service.qc.defect;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.defect.vo.MesQcDefectSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defect.MesQcDefectDO;
import cn.iocoder.yudao.module.mes.dal.mysql.qc.defect.MesQcDefectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.mes.enums.ErrorCodeConstants.*;

/**
 * MES 缺陷类型 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class MesQcDefectServiceImpl implements MesQcDefectService {

    @Resource
    private MesQcDefectMapper defectMapper;

    @Override
    public Long createDefect(MesQcDefectSaveReqVO createReqVO) {
        // 校验编码、名称唯一
        validateDefectSaveData(null, createReqVO);

        // 插入
        MesQcDefectDO defect = BeanUtils.toBean(createReqVO, MesQcDefectDO.class);
        defectMapper.insert(defect);
        return defect.getId();
    }

    @Override
    public void updateDefect(MesQcDefectSaveReqVO updateReqVO) {
        // 校验存在
        validateDefectExists(updateReqVO.getId());
        // 校验编码、名称唯一
        validateDefectSaveData(updateReqVO.getId(), updateReqVO);

        // 更新
        MesQcDefectDO updateObj = BeanUtils.toBean(updateReqVO, MesQcDefectDO.class);
        defectMapper.updateById(updateObj);
    }

    @Override
    public void deleteDefect(Long id) {
        // 校验存在
        validateDefectExists(id);
        // 删除
        defectMapper.deleteById(id);
    }

    private void validateDefectExists(Long id) {
        if (defectMapper.selectById(id) == null) {
            throw exception(QC_DEFECT_NOT_EXISTS);
        }
    }

    private void validateDefectSaveData(Long id, MesQcDefectSaveReqVO reqVO) {
        validateDefectCodeUnique(id, reqVO.getCode());
        validateDefectNameUnique(id, reqVO.getName());
    }

    private void validateDefectCodeUnique(Long id, String code) {
        MesQcDefectDO defect = defectMapper.selectByCode(code);
        if (defect == null) {
            return;
        }
        if (ObjUtil.notEqual(defect.getId(), id)) {
            throw exception(QC_DEFECT_CODE_DUPLICATE);
        }
    }

    private void validateDefectNameUnique(Long id, String name) {
        MesQcDefectDO defect = defectMapper.selectByName(name);
        if (defect == null) {
            return;
        }
        if (ObjUtil.notEqual(defect.getId(), id)) {
            throw exception(QC_DEFECT_NAME_DUPLICATE);
        }
    }

    @Override
    public MesQcDefectDO getDefect(Long id) {
        return defectMapper.selectById(id);
    }

    @Override
    public PageResult<MesQcDefectDO> getDefectPage(MesQcDefectPageReqVO pageReqVO) {
        return defectMapper.selectPage(pageReqVO);
    }

    @Override
    public List<MesQcDefectDO> getDefectList() {
        return defectMapper.selectList();
    }

    @Override
    public List<MesQcDefectDO> getDefectList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return defectMapper.selectByIds(ids);
    }

}
