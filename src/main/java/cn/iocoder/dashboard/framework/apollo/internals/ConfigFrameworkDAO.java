package cn.iocoder.dashboard.framework.apollo.internals;

import cn.iocoder.dashboard.modules.infra.dal.mysql.dataobject.config.InfConfigDO;

import java.util.Date;
import java.util.List;

/**
 * 配置 Framework DAO 接口
 */
public interface ConfigFrameworkDAO {

    /**
     * 查询是否存在比 maxUpdateTime 更新记录更晚的配置
     *
     * @param maxUpdateTime 最大更新时间
     * @return 是否存在
     */
    boolean selectExistsByUpdateTimeAfter(Date maxUpdateTime);

    /**
     * 查询配置列表
     *
     * @return 配置列表
     */
    List<InfConfigDO> getSysConfigList();

}
