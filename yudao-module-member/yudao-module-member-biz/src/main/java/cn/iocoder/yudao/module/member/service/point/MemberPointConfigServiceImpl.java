package cn.iocoder.yudao.module.member.service.point;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.member.controller.admin.point.vo.config.MemberPointConfigSaveReqVO;
import cn.iocoder.yudao.module.member.convert.point.MemberPointConfigConvert;
import cn.iocoder.yudao.module.member.dal.dataobject.point.MemberPointConfigDO;
import cn.iocoder.yudao.module.member.dal.mysql.point.MemberPointConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

/**
 * 会员积分配置 Service 实现类
 *
 * @author QingX
 */
@Service
@Validated
public class MemberPointConfigServiceImpl implements MemberPointConfigService {

    @Resource
    private MemberPointConfigMapper pointConfigMapper;

    @Override
    public void saveConfig(MemberPointConfigSaveReqVO saveReqVO) {
        //获取当前记录
        MemberPointConfigDO configDO = getConfig();
        MemberPointConfigDO pointConfigDO = MemberPointConfigConvert.INSTANCE.convert(saveReqVO);
        //当前存在记录，则更新，否则插入
        if (configDO != null) {
            pointConfigDO.setId(configDO.getId());
            pointConfigMapper.updateById(pointConfigDO);
        } else {
            pointConfigDO.setId(null);
            pointConfigMapper.insert(pointConfigDO);
        }
    }

    @Override
    public MemberPointConfigDO getConfig() {
        List <MemberPointConfigDO> list = pointConfigMapper.selectList();
        return CollectionUtils.getFirst(list);
    }

}
