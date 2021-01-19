package cn.iocoder.dashboard.modules.system.service.config;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigCreateReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigExportReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigPageReqVO;
import cn.iocoder.dashboard.modules.system.controller.config.vo.SysConfigUpdateReqVO;
import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config.SysConfigDO;

import java.util.List;

/**
 * 参数配置 Service 接口
 */
public interface SysConfigService {

    /**
     * 获得参数配置分页列表
     *
     * @param reqVO 分页条件
     * @return 分页列表
     */
    PageResult<SysConfigDO> getConfigPage(SysConfigPageReqVO reqVO);

    /**
     * 获得参数配置列表
     *
     * @param reqVO 列表
     * @return 列表
     */
    List<SysConfigDO> getConfigList(SysConfigExportReqVO reqVO);

    /**
     * 获得参数配置
     *
     * @param id 配置编号
     * @return 参数配置
     */
    SysConfigDO getConfig(Long id);

    /**
     * 根据参数键，获得参数配置
     *
     * @param key 配置键
     * @return 参数配置
     */
    SysConfigDO getConfigByKey(String key);

    /**
     * 创建参数配置
     *
     * @param reqVO 创建信息
     * @return 配置编号
     */
    Long createConfig(SysConfigCreateReqVO reqVO);

    /**
     * 更新参数配置
     *
     * @param reqVO 更新信息
     */
    void updateConfig(SysConfigUpdateReqVO reqVO);

    /**
     * 删除参数配置
     *
     * @param id 配置编号
     */
    void deleteConfig(Long id);

}
