package cn.iocoder.yudao.userserver.modules.infra.dal.mysql.logger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.infra.dal.dataobject.logger.InfApiErrorLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 错误日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiErrorLogMapper extends BaseMapperX<InfApiErrorLogDO> {
}
