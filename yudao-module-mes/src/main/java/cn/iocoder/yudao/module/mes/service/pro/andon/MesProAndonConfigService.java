package cn.iocoder.yudao.module.mes.service.pro.andon;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.pro.andon.vo.config.MesProAndonConfigSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.andon.MesProAndonConfigDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 安灯呼叫配置 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProAndonConfigService {

    /**
     * 创建安灯呼叫配置
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAndonConfig(@Valid MesProAndonConfigSaveReqVO createReqVO);

    /**
     * 更新安灯呼叫配置
     *
     * @param updateReqVO 更新信息
     */
    void updateAndonConfig(@Valid MesProAndonConfigSaveReqVO updateReqVO);

    /**
     * 删除安灯呼叫配置
     *
     * @param id 编号
     */
    void deleteAndonConfig(Long id);

    /**
     * 校验安灯呼叫配置存在
     *
     * @param id 编号
     * @return 安灯呼叫配置
     */
    MesProAndonConfigDO validateAndonConfigExists(Long id);

    /**
     * 获得安灯呼叫配置
     *
     * @param id 编号
     * @return 安灯呼叫配置
     */
    MesProAndonConfigDO getAndonConfig(Long id);

    /**
     * 获得安灯呼叫配置分页
     *
     * @param pageReqVO 分页查询
     * @return 安灯呼叫配置分页
     */
    PageResult<MesProAndonConfigDO> getAndonConfigPage(MesProAndonConfigPageReqVO pageReqVO);

    /**
     * 获得安灯呼叫配置列表
     *
     * @return 安灯呼叫配置列表
     */
    List<MesProAndonConfigDO> getAndonConfigList();

}
