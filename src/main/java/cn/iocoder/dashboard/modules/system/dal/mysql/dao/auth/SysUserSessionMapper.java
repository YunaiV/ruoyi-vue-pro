package cn.iocoder.dashboard.modules.system.dal.mysql.dao.auth;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.auth.SysUserSessionDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserSessionMapper extends BaseMapper<SysUserSessionDO> {
}
