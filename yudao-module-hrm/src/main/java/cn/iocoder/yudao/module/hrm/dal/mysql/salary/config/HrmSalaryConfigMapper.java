package cn.iocoder.yudao.module.hrm.dal.mysql.salary.config;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hrm.dal.dataobject.salary.config.HrmSalaryConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HrmSalaryConfigMapper extends BaseMapperX<HrmSalaryConfigDO> {

    default HrmSalaryConfigDO selectFirst() {
        return selectFirstOne(new LambdaQueryWrapperX<HrmSalaryConfigDO>()
                .orderByAsc(HrmSalaryConfigDO::getId));
    }

}
