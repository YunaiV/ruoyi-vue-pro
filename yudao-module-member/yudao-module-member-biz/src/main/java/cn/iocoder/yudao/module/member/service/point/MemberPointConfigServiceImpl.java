package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigSaveReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

/**
 * 会员积分配置 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberPointConfigServiceImpl implements MemberPointConfigService {

    @Resource
    private MemberPointConfigMapper memberPointConfigMapper;

    @Override
    public void saveConfig(MemberPointConfigSaveReqVO saveReqVO) {
        // TODO qingx：配置存在，则 update；不存在则 insert
    }

    @Override
    public MemberPointConfigDO getConfig() {
        // TODO qingx：直接查询到一条；
        return null;
    }
}
