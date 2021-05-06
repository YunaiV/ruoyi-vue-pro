package cn.iocoder.yudao.userserver.modules.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.userserver.modules.infra.dal.dataobject.file.InfFileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfFileMapper extends BaseMapperX<InfFileDO> {

}
