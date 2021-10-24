package cn.iocoder.yudao.coreservice.modules.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.coreservice.modules.infra.dal.dataobject.file.InfFileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfFileCoreMapper extends BaseMapperX<InfFileDO> {
    default Integer selectCountById(String id) {
        return selectCount("id", id);
    }
}
