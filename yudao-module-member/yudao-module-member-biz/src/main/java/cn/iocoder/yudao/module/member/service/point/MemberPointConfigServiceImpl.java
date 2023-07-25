package cn.iocoder.yudao.module.member.service.point;

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
        // TODO @xiaqing：直接 getConfig() 查询，如果不存在，则插入；存在，则进行更新；
        long total = pointConfigMapper.selectCount();
        MemberPointConfigDO pointConfigDO = MemberPointConfigConvert.INSTANCE.convert(saveReqVO);
        //大于0存在记录，则更新，否则插入
        if (total > 0) {
            pointConfigMapper.updateById(pointConfigDO);
        } else {
            pointConfigMapper.insert(pointConfigDO);
        }
    }

    @Override
    public MemberPointConfigDO getConfig() {
        List <MemberPointConfigDO> list = pointConfigMapper.selectList();
        // TODO @xiaqing：可以使用 CollUtil.getFirst()
        return list == null ? null : list.get(0);
    }

}
