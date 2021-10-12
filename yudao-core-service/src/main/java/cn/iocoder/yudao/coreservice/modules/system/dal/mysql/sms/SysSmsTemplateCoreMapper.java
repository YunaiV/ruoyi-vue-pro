package cn.iocoder.yudao.coreservice.modules.system.dal.mysql.sms;

import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Date;

@Mapper
public interface SysSmsTemplateCoreMapper extends BaseMapperX<SysSmsTemplateDO> {

    @Select("SELECT id FROM sys_sms_template WHERE update_time > #{maxUpdateTime} LIMIT 1")
    Long selectExistsByUpdateTimeAfter(Date maxUpdateTime);

}
