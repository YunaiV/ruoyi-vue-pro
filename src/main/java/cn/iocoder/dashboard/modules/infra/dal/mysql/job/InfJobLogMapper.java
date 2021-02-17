package cn.iocoder.dashboard.modules.infra.dal.mysql.job;

import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfJobLogMapper extends BaseMapperX<InfJobLogDO> {
}
