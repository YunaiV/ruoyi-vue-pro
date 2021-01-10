package cn.iocoder.dashboard.modules.system.convert.user;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.user.vo.user.SysUserPageItemRespVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept.SysDeptDO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.user.SysUserDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SysUserConvert {

    SysUserConvert INSTANCE = Mappers.getMapper(SysUserConvert.class);

    SysUserPageItemRespVO convert(SysUserDO bean);

    SysUserPageItemRespVO.Dept convert(SysDeptDO bean);

    @Mapping(source = "records", target = "list")
    PageResult<SysUserDO> convertPage(IPage<SysUserDO> page);

}
