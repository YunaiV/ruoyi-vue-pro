package cn.iocoder.yudao.module.point.service.pointconfig;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.point.controller.admin.pointconfig.vo.*;
import cn.iocoder.yudao.module.point.dal.dataobject.pointconfig.PointConfigDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 积分设置 Service 接口
 *
 * @author QingX
 */
public interface PointConfigService {

    /**
     * 创建积分设置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createConfig(@Valid PointConfigCreateReqVO createReqVO);

    /**
     * 更新积分设置
     *
     * @param updateReqVO 更新信息
     */
    void updateConfig(@Valid PointConfigUpdateReqVO updateReqVO);

    /**
     * 删除积分设置
     *
     * @param id 编号
     */
    void deleteConfig(Integer id);

    /**
     * 获得积分设置
     *
     * @param id 编号
     * @return 积分设置
     */
    PointConfigDO getConfig(Integer id);

    /**
     * 获得积分设置列表
     *
     * @param ids 编号
     * @return 积分设置列表
     */
    List<PointConfigDO> getConfigList(Collection<Integer> ids);

    /**
     * 获得积分设置分页
     *
     * @param pageReqVO 分页查询
     * @return 积分设置分页
     */
    PageResult<PointConfigDO> getConfigPage(PointConfigPageReqVO pageReqVO);

    /**
     * 获得积分设置列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 积分设置列表
     */
    List<PointConfigDO> getConfigList(PointConfigExportReqVO exportReqVO);

}
