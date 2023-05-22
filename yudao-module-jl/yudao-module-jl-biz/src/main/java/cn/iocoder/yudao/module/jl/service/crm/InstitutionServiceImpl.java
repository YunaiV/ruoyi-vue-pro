package cn.iocoder.yudao.module.jl.service.crm;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.InstitutionConvert;
import cn.iocoder.yudao.module.jl.dal.mysql.crm.InstitutionMapper;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * CRM 模块的机构/公司 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class InstitutionServiceImpl implements InstitutionService {

    @Resource
    private InstitutionMapper institutionMapper;

    @Override
    public Long createInstitution(InstitutionCreateReqVO createReqVO) {
        // 插入
        InstitutionDO institution = InstitutionConvert.INSTANCE.convert(createReqVO);
        institutionMapper.insert(institution);
        // 返回
        return institution.getId();
    }

    @Override
    public void updateInstitution(InstitutionUpdateReqVO updateReqVO) {
        // 校验存在
        validateInstitutionExists(updateReqVO.getId());
        // 更新
        InstitutionDO updateObj = InstitutionConvert.INSTANCE.convert(updateReqVO);
        institutionMapper.updateById(updateObj);
    }

    @Override
    public void deleteInstitution(Long id) {
        // 校验存在
        validateInstitutionExists(id);
        // 删除
        institutionMapper.deleteById(id);
    }

    private void validateInstitutionExists(Long id) {
        if (institutionMapper.selectById(id) == null) {
            throw exception(INSTITUTION_NOT_EXISTS);
        }
    }

    @Override
    public InstitutionDO getInstitution(Long id) {
        return institutionMapper.selectById(id);
    }

    @Override
    public List<InstitutionDO> getInstitutionList(Collection<Long> ids) {
        return institutionMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<InstitutionDO> getInstitutionPage(InstitutionPageReqVO pageReqVO) {
        return institutionMapper.selectPage(pageReqVO);
    }

    @Override
    public List<InstitutionDO> getInstitutionList(InstitutionExportReqVO exportReqVO) {
        return institutionMapper.selectList(exportReqVO);
    }

}
