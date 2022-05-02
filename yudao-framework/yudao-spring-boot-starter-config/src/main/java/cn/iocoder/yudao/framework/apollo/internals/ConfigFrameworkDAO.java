package cn.iocoder.yudao.framework.apollo.internals;

import cn.iocoder.yudao.framework.apollo.internals.dto.ConfigRespDTO;

import java.util.Date;
import java.util.List;

/**
 * 配置 Framework DAO 接口
 *
 * 注意，实现类必须提供 (String jdbcUrl, String username, String password) 构造方法
 *
 * @author 芋道源码
 */
public interface ConfigFrameworkDAO {

    /**
     * 查询是否存在比 maxUpdateTime 的更新记录数量
     *
     * @param maxUpdateTime 最大更新时间
     * @return 是否存在
     */
    int selectCountByUpdateTimeGt(Date maxUpdateTime);

    /**
     * 查询配置列表
     *
     * @return 配置列表
     */
    List<ConfigRespDTO> selectList();

}
