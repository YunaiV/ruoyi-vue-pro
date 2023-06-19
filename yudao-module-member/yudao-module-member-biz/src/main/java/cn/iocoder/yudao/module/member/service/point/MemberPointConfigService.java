package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigSaveReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;

import javax.validation.Valid;

/**
 * 会员积分配置 Service 接口
 *
 * @author QingX
 */
public interface MemberPointConfigService {

    /**
     * 保存会员积分配置
     *
     * @param saveReqVO 更新信息
     */
    void saveConfig(@Valid MemberPointConfigSaveReqVO saveReqVO);

    /**
     * 获得会员积分配置
     *
     * @return 积分配置
     */
    MemberPointConfigDO getConfig();

}
