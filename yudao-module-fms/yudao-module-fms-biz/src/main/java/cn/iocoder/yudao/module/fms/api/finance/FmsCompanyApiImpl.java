package cn.iocoder.yudao.module.fms.api.finance;

import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.fms.api.finance.dto.FmsCompanyDTO;
import cn.iocoder.yudao.module.fms.service.finance.subject.FmsCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.FINANCE_SUBJECT_NOT_EXISTS;

@Service
public class FmsCompanyApiImpl implements FmsCompanyApi {

    @Autowired
    FmsCompanyService subjectService;


    @Override
    public List<FmsCompanyDTO> validateCompany(List<Long> ids) {
        //ids去重
        ids = ids.stream().distinct().toList();
        List<FmsCompanyDTO> dtoList = BeanUtils.toBean(subjectService.listCompany(ids), FmsCompanyDTO.class);
        //如果ids长度不等于dtoList长度，则说明有id不存在
        if (ids.size() != dtoList.size()) {
            List<Long> idList = dtoList.stream().map(FmsCompanyDTO::getId).toList();
            List<Long> notExistIds = ids.stream().filter(id -> !idList.contains(id)).toList();
            ThrowUtil.ifThrow(!notExistIds.isEmpty(), FINANCE_SUBJECT_NOT_EXISTS, notExistIds);
        }
        return dtoList;
    }
}
