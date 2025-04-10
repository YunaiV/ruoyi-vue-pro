package cn.iocoder.yudao.module.fms.service.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectSimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsFinanceSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.finance.subject.FmsFinanceSubjectMapper;
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
 * Erp财务主体 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class FmsFinanceSubjectServiceImpl implements FmsFinanceSubjectService {

    @Resource
    private FmsFinanceSubjectMapper financeSubjectMapper;

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    public Long createFinanceSubject(FmsFinanceSubjectSaveReqVO createReqVO) {
        // 插入
        FmsFinanceSubjectDO financeSubject = BeanUtils.toBean(createReqVO, FmsFinanceSubjectDO.class);
        financeSubjectMapper.insert(financeSubject);
        // 返回
        return financeSubject.getId();
    }

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceSubject(FmsFinanceSubjectSaveReqVO updateReqVO) {
        // 校验存在
        validateFinanceSubjectExists(updateReqVO.getId());
        // 更新
        FmsFinanceSubjectDO updateObj = BeanUtils.toBean(updateReqVO, FmsFinanceSubjectDO.class);
        financeSubjectMapper.updateById(updateObj);
    }

    @Override
    @CacheEvict(value = {FINANCE_SUBJECT_LIST}, allEntries = true)
    public void deleteFinanceSubject(Long id) {
        // 校验存在
        validateFinanceSubjectExists(id);
        // 删除
        financeSubjectMapper.deleteById(id);
    }


    public void validateFinanceSubjectExists(Long id) {
        if (this.getFinanceSubject(id) == null) {
            throw exception(FINANCE_SUBJECT_NOT_EXISTS, id);
        }
    }

    @Override
    public FmsFinanceSubjectDO getFinanceSubject(Long id) {
        return financeSubjectMapper.selectById(id);
    }


    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'DO:'+#pageReqVO", unless = "#result == null")
    public PageResult<FmsFinanceSubjectDO> getFinanceSubjectPage(FmsFinanceSubjectPageReqVO pageReqVO) {
        return financeSubjectMapper.selectPage(pageReqVO);
    }

    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'VO:'+'simple-list'", unless = "#result == null")
    public List<FmsFinanceSubjectSimpleRespVO> ListFinanceSubjectSimple() {
        List<FmsFinanceSubjectDO> subjectDOS = financeSubjectMapper.selectListSimple();
        return BeanUtils.toBean(subjectDOS, FmsFinanceSubjectSimpleRespVO.class);
    }

    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'DO:'+#ids", unless = "#result == null")
    public List<FmsFinanceSubjectDO> listFinanceSubject(List<Long> ids) {
        return financeSubjectMapper.selectList(FmsFinanceSubjectDO::getId, ids);
    }
}