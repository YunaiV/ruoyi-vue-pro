package cn.iocoder.yudao.module.infra.dal.mysql.logger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 访问日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiAccessLogCoreMapper extends BaseMapperX<InfApiAccessLogDO> {
}
