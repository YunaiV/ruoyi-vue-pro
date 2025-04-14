package cn.iocoder.yudao.module.fms.service.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanyPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanySimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import cn.iocoder.yudao.module.fms.dal.mysql.finance.subject.FmsCompanyMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_SUBJECT_NOT_EXISTS;
import static cn.iocoder.yudao.module.fms.dal.redis.FmsRedisKeyConstants.FINANCE_SUBJECT_LIST;

/**
 * Fms财务公司 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class FmsCompanyServiceImpl implements FmsCompanyService {

    @Resource
    private FmsCompanyMapper CompanyMapper;

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    public Long createCompany(FmsCompanySaveReqVO createReqVO) {
        // 插入
        FmsCompanyDO Company = BeanUtils.toBean(createReqVO, FmsCompanyDO.class);
        CompanyMapper.insert(Company);
        // 返回
        return Company.getId();
    }

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateCompany(FmsCompanySaveReqVO updateReqVO) {
        // 校验存在
        validateCompanyExists(updateReqVO.getId());
        // 更新
        FmsCompanyDO updateObj = BeanUtils.toBean(updateReqVO, FmsCompanyDO.class);
        CompanyMapper.updateById(updateObj);
    }

    @Override
    @CacheEvict(value = {FINANCE_SUBJECT_LIST}, allEntries = true)
    public void deleteCompany(Long id) {
        // 校验存在
        validateCompanyExists(id);
        // 删除
        CompanyMapper.deleteById(id);
    }


    public void validateCompanyExists(Long id) {
        if (this.getCompany(id) == null) {
            throw exception(FINANCE_SUBJECT_NOT_EXISTS, id);
        }
    }

    @Override
    public FmsCompanyDO getCompany(Long id) {
        return CompanyMapper.selectById(id);
    }


    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'DO:'+#pageReqVO", unless = "#result == null")
    public PageResult<FmsCompanyDO> getCompanyPage(FmsCompanyPageReqVO pageReqVO) {
        return CompanyMapper.selectPage(pageReqVO);
    }

    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'VO:'+'simple-list'", unless = "#result == null")
    public List<FmsCompanySimpleRespVO> ListCompanySimple() {
        List<FmsCompanyDO> subjectDOS = CompanyMapper.selectListSimple();
        return BeanUtils.toBean(subjectDOS, FmsCompanySimpleRespVO.class);
    }

    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'DO:'+#ids", unless = "#result == null")
    public List<FmsCompanyDO> listCompany(List<Long> ids) {
        return CompanyMapper.selectList(FmsCompanyDO::getId, ids);
    }
}