package cn.iocoder.yudao.module.hrm.dal.mysql.salary.slip;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.controller.admin.salary.vo.slip.sendrecord.HrmSalarySlipSendRecordPageReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.slip.HrmSalarySlipSendRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HrmSalarySlipSendRecordMapper extends BaseMapperX<HrmSalarySlipSendRecordDO> {

    default PageResult<HrmSalarySlipSendRecordDO> selectPage(HrmSalarySlipSendRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<HrmSalarySlipSendRecordDO>()
                .eqIfPresent(HrmSalarySlipSendRecordDO::getYear, reqVO.getYear())
                .eqIfPresent(HrmSalarySlipSendRecordDO::getMonth, reqVO.getMonth())
                .orderByDesc(HrmSalarySlipSendRecordDO::getYear)
                .orderByDesc(HrmSalarySlipSendRecordDO::getMonth));
    }

}
