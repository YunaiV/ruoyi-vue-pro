package cn.iocoder.yudao.module.promotion.service.seckill;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigCreateReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.admin.seckill.vo.config.SeckillConfigUpdateReqVO;
import cn.iocoder.yudao.module.promotion.dal.dataobject.seckill.SeckillConfigDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 秒杀时段 Service 接口
 *
 * @author halfninety
 */
public interface SeckillConfigService {

    /**
     * 创建秒杀时段
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSeckillConfig(@Valid SeckillConfigCreateReqVO createReqVO);

    /**
     * 更新秒杀时段
     *
     * @param updateReqVO 更新信息
     */
    void updateSeckillConfig(@Valid SeckillConfigUpdateReqVO updateReqVO);

    /**
     * 删除秒杀时段
     *
     * @param id 编号
     */
    void deleteSeckillConfig(Long id);

    /**
     * 获得秒杀时段
     *
     * @param id 编号
     * @return 秒杀时段
     */
    SeckillConfigDO getSeckillConfig(Long id);

    /**
     * 获得所有秒杀时段列表
     *
     * @return 所有秒杀时段列表
     */
    List<SeckillConfigDO> getSeckillConfigList();

    /**
     * 校验秒杀时段是否存在
     *
     * @param ids 秒杀时段 id 集合
     */
    void validateSeckillConfigExists(Collection<Long> ids);

    /**
     * 获得秒杀时间段配置分页数据
     *
     * @param pageVO 分页请求参数
     * @return 秒杀时段分页列表
     */
    PageResult<SeckillConfigDO> getSeckillConfigPage(SeckillConfigPageReqVO pageVO);

    /**
     * 获得所有正常状态的时段配置列表
     *
     * @param status 状态
     * @return 秒杀时段列表
     */
    List<SeckillConfigDO> getSeckillConfigListByStatus(Integer status);

    /**
     * 更新秒杀时段配置状态
     *
     * @param id     id
     * @param status 状态
     */
    void updateSeckillConfigStatus(Long id, Integer status);

    /**
     * 获得当前的秒杀时段
     *
     * 要求必须处于开启状态、且在当前时间段内
     *
     * @return 时段
     */
    SeckillConfigDO getCurrentSeckillConfig();

}
