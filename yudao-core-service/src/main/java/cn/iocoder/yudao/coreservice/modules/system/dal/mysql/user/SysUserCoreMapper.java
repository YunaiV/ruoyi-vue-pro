package cn.iocoder.yudao.coreservice.modules.system.dal.mysql.user;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.user.SysUserDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysUserCoreMapper extends BaseMapperX<SysUserDO> {

}
