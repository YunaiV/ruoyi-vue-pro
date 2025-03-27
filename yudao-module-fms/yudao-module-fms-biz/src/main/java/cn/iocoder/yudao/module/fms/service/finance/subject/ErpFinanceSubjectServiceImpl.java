package cn.iocoder.yudao.module.fms.service.finance.subject;

import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.ErpFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.ErpFinanceSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.ErpFinanceSubjectSimpleRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.ErpFinanceSubjectDO;
import cn.iocoder.yudao.module.fms.dal.mysql.finance.subject.ErpFinanceSubjectMapper;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static cn.iocoder.yudao.module.fms.dal.redis.FmsRedisKeyConstants.FINANCE_SUBJECT_LIST;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_SUBJECT_NOT_EXISTS;

/**
 * Erp财务主体 Service 实现类
 *
 * @author 王岽宇
 */
@Service
@Validated
public class ErpFinanceSubjectServiceImpl implements ErpFinanceSubjectService {

    @Resource
    private ErpFinanceSubjectMapper financeSubjectMapper;

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    public Long createFinanceSubject(ErpFinanceSubjectSaveReqVO createReqVO) {
        // 插入
        ErpFinanceSubjectDO financeSubject = BeanUtils.toBean(createReqVO, ErpFinanceSubjectDO.class);
        financeSubjectMapper.insert(financeSubject);
        // 返回
        return financeSubject.getId();
    }

    @Override
    @CacheEvict(value = FINANCE_SUBJECT_LIST, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void updateFinanceSubject(ErpFinanceSubjectSaveReqVO updateReqVO) {
        // 校验存在
        validateFinanceSubjectExists(updateReqVO.getId());
        // 更新
        ErpFinanceSubjectDO updateObj = BeanUtils.toBean(updateReqVO, ErpFinanceSubjectDO.class);
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

    private void validateFinanceSubjectExists(Long id) {
        if (financeSubjectMapper.selectById(id) == null) {
            throw exception(FINANCE_SUBJECT_NOT_EXISTS, id);
        }
    }

    @Override
    public ErpFinanceSubjectDO getFinanceSubject(Long id) {
        return financeSubjectMapper.selectById(id);
    }


    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'DO'+#pageReqVO", unless = "#result == null")
    public PageResult<ErpFinanceSubjectDO> getFinanceSubjectPage(ErpFinanceSubjectPageReqVO pageReqVO) {
        return financeSubjectMapper.selectPage(pageReqVO);
    }

    @Override
    @Cacheable(value = FINANCE_SUBJECT_LIST, key = "'VO'+'simple-list'", unless = "#result == null")
    public List<ErpFinanceSubjectSimpleRespVO> ListFinanceSubjectSimple() {
        List<ErpFinanceSubjectDO> subjectDOS = financeSubjectMapper.selectListSimple();
        return BeanUtils.toBean(subjectDOS, ErpFinanceSubjectSimpleRespVO.class);
    }

    @Override
    public List<ErpFinanceSubjectDO> listFinanceSubject(List<Long> ids) {
        return financeSubjectMapper.selectList(ErpFinanceSubjectDO::getId, ids);
    }
}