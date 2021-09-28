package cn.iocoder.yudao.userserver.modules.infra.dal.mysql.logger;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.infra.dal.dataobject.logger.InfApiAccessLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * API 访问日志 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface InfApiAccessLogMapper extends BaseMapperX<InfApiAccessLogDO> {
}
