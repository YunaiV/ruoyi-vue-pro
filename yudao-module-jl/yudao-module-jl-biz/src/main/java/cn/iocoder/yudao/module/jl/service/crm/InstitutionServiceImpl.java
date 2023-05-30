package cn.iocoder.yudao.module.jl.service.crm;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import java.util.*;
import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.entity.crm.Institution;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.mapper.crm.InstitutionMapper;
import cn.iocoder.yudao.module.jl.repository.crm.InstitutionRepository;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.jl.enums.ErrorCodeConstants.*;

/**
 * 机构/公司 Service 实现类
 *
 */
@Service
@Validated
public class InstitutionServiceImpl implements InstitutionService {

    @Resource
    private InstitutionRepository institutionRepository;

    @Resource
    private InstitutionMapper institutionMapper;

    @Override
    public Long createInstitution(InstitutionCreateReqVO createReqVO) {
        // 插入
        Institution institution = institutionMapper.toEntity(createReqVO);
        institutionRepository.save(institution);
        // 返回
        return institution.getId();
    }

    @Override
    public void updateInstitution(InstitutionUpdateReqVO updateReqVO) {
        // 校验存在
        validateInstitutionExists(updateReqVO.getId());
        // 更新
        Institution updateObj = institutionMapper.toEntity(updateReqVO);
        institutionRepository.save(updateObj);
    }

    @Override
    public void deleteInstitution(Long id) {
        // 校验存在
        validateInstitutionExists(id);
        // 删除
        institutionRepository.deleteById(id);
    }

    private void validateInstitutionExists(Long id) {
        institutionRepository.findById(id).orElseThrow(() -> exception(INSTITUTION_NOT_EXISTS));
    }

    @Override
    public Optional<Institution> getInstitution(Long id) {
        return institutionRepository.findById(id);
    }

    @Override
    public List<Institution> getInstitutionList(Collection<Long> ids) {
        return StreamSupport.stream(institutionRepository.findAllById(ids).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<Institution> getInstitutionPage(InstitutionPageReqVO pageReqVO) {
        return null;
    }

    @Override
    public List<Institution> getInstitutionList(InstitutionExportReqVO exportReqVO) {
        return null;
    }

}
