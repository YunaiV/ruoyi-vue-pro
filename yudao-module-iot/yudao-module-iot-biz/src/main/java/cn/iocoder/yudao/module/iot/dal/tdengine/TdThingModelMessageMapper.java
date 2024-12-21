package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessageDO;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Mapper;

/**
 * 处理 TD 中物模型消息日志的操作
 */
@Mapper
@DS("tdengine")
public interface TdThingModelMessageMapper {

    /**
     * 创建物模型消息日志超级表超级表
     *
     */
    @TenantIgnore
    void createSuperTable(ThingModelMessageDO superTable);

    /**
     * 创建子表
     *
     */
    @TenantIgnore
    void createTableWithTag(ThingModelMessageDO table);

}
