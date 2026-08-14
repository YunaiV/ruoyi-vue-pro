package cn.iocoder.yudao.module.hrm.service.recruit.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelDeleteReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.channel.HrmRecruitChannelStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config.HrmRecruitChannelDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 招聘渠道 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitChannelService {

    /**
     * 创建招聘渠道
     *
     * @param createReqVO 招聘渠道信息
     * @return 招聘渠道编号
     */
    Long createRecruitChannel(HrmRecruitChannelSaveReqVO createReqVO);

    /**
     * 更新招聘渠道
     *
     * @param updateReqVO 招聘渠道信息
     */
    void updateRecruitChannel(HrmRecruitChannelSaveReqVO updateReqVO);

    /**
     * 更新招聘渠道状态
     *
     * @param statusReqVO 招聘渠道状态信息
     */
    void updateRecruitChannelStatus(HrmRecruitChannelStatusReqVO statusReqVO);

    /**
     * 删除招聘渠道，并迁移关联的员工和候选人
     *
     * @param deleteReqVO 招聘渠道删除信息
     */
    void deleteRecruitChannel(HrmRecruitChannelDeleteReqVO deleteReqVO);

    /**
     * 获得招聘渠道
     *
     * @param id 招聘渠道编号
     * @return 招聘渠道
     */
    HrmRecruitChannelDO getRecruitChannel(Long id);

    /**
     * 校验招聘渠道是否存在
     *
     * @param id 招聘渠道编号
     * @return 招聘渠道
     */
    HrmRecruitChannelDO validateRecruitChannelExists(Long id);

    /**
     * 获得招聘渠道列表
     *
     * @param ids 招聘渠道编号集合
     * @return 招聘渠道列表
     */
    List<HrmRecruitChannelDO> getRecruitChannelList(Collection<Long> ids);

    /**
     * 获得招聘渠道 Map
     *
     * @param ids 招聘渠道编号集合
     * @return 招聘渠道 Map
     */
    default Map<Long, HrmRecruitChannelDO> getRecruitChannelMap(Collection<Long> ids) {
        return convertMap(getRecruitChannelList(ids), HrmRecruitChannelDO::getId);
    }

    /**
     * 获得招聘渠道分页列表
     *
     * @param pageReqVO 分页查询
     * @return 招聘渠道分页列表
     */
    PageResult<HrmRecruitChannelDO> getRecruitChannelPage(HrmRecruitChannelPageReqVO pageReqVO);

    /**
     * 获得招聘渠道精简列表
     *
     * @return 招聘渠道精简列表
     */
    List<HrmRecruitChannelDO> getRecruitChannelSimpleList();

}
