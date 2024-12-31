package cn.iocoder.yudao.module.iot.dal.tdengine;

import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.tdengine.ThingModelMessageDO;
import cn.iocoder.yudao.module.iot.framework.tdengine.core.annotation.TDengineDS;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 处理 TD 中物模型消息日志的操作
 */
@Mapper
@TDengineDS
@InterceptorIgnore(tenantLine = "true") // 避免 SQL 解析，因为 JSqlParser 对 TDengine 的 SQL 解析会报错
public interface TdThingModelMessageMapper {

    /**
     * 创建物模型消息日志超级表超级表
     *
     */

    void createSuperTable(@Param("productKey") String productKey);

    /**
     * 创建子表
     *
     */

    void createTableWithTag(@Param("productKey") String productKey,@Param("deviceKey") String deviceKey);

}
