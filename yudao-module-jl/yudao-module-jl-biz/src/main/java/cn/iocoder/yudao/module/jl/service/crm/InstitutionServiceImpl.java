package cn.iocoder.yudao.module.jl.service.crm;

import cn.iocoder.yudao.module.jl.dal.dataobject.crm.Institution;
import cn.iocoder.yudao.module.jl.mapper.InstitutionMapper;
import cn.iocoder.yudao.module.jl.repository.InstitutionRepository;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import cn.iocoder.yudao.module.jl.controller.admin.crm.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.crm.InstitutionDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import cn.iocoder.yudao.module.jl.convert.crm.InstitutionConvert;

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
    private InstitutionRepository institutionRepository;

    @Resource
    private InstitutionMapper institutionMapper;

    @Override
    public Long createInstitution(InstitutionCreateReq createReqVO) {
        // 插入
        Institution institution = institutionMapper.toEntity(createReqVO);
        institutionRepository.save(institution);
        // 返回
        return institution.getId();
    }

    @Override
    public void updateInstitution(InstitutionDto updateReqVO) {
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
        institutionRepository.findById(id).orElseThrow(() -> exception(COMPETITOR_NOT_EXISTS));
    }

    @Override
    public InstitutionDto getInstitution(Long id) {
        return institutionRepository.findById(id).map(institutionMapper::toDto).orElse(null);
    }

    @Override
    public List<InstitutionDto> getInstitutionList(Collection<Long> ids) {
        return StreamSupport.stream(institutionRepository.findAllById(ids).spliterator(), false)
                .map(institutionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<InstitutionDto> getInstitutionPage(InstitutionPageReqVO pageReqVO) {
        return null;
    }

    @Override
    public List<InstitutionDto> getInstitutionList(InstitutionExportReqVO exportReqVO) {
        return null;
    }

}
