package cn.iocoder.mudule.fms.api.finance;

import cn.iocoder.mudule.fms.service.finance.subject.ErpFinanceSubjectService;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.finance.ErpFinanceSubjectApi;
import cn.iocoder.yudao.module.erp.api.finance.dto.ErpFinanceSubjectDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_SUBJECT_NOT_EXISTS;

@Service
public class ErpFinanceSubjectApiImpl implements ErpFinanceSubjectApi {

    @Autowired
    ErpFinanceSubjectService subjectService;


    @Override
    public List<ErpFinanceSubjectDTO> validateFinanceSubject(List<Long> ids) {
        //ids去重
        ids = ids.stream().distinct().toList();
        List<ErpFinanceSubjectDTO> dtoList = BeanUtils.toBean(subjectService.listFinanceSubject(ids), ErpFinanceSubjectDTO.class);
        //如果ids长度不等于dtoList长度，则说明有id不存在
        if (ids.size() != dtoList.size()) {
            List<Long> idList = dtoList.stream().map(ErpFinanceSubjectDTO::getId).toList();
            List<Long> notExistIds = ids.stream().filter(id -> !idList.contains(id)).toList();
            ThrowUtil.ifThrow(!notExistIds.isEmpty(), FINANCE_SUBJECT_NOT_EXISTS, notExistIds);
        }
        return dtoList;
    }
}
