package cn.iocoder.dashboard.modules.system.dal.mysql.sms;
import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.dashboard.modules.system.dal.dataobject.sms.SysSmsTemplateDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysSmsTemplateMapper extends BaseMapperX<SysSmsTemplateDO> {

    default SysSmsTemplateDO selectOneByCode(String code) {
        return selectOne("code", code);
    }

    /**
     * 根据短信渠道id查询短信模板集合
     *
     * @param channelId 渠道id
     * @return 模板集合
     */
    default List<SysSmsTemplateDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapper<SysSmsTemplateDO>()
                .eq(SysSmsTemplateDO::getChannelId, channelId)
                .eq(SysSmsTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SysSmsTemplateDO::getId)
        );
    }

    /**
     * 查询有效短信模板集合
     *
     * @return 有效短信模板集合
     */
    default List<SysSmsTemplateDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapper<SysSmsTemplateDO>()
                .eq(SysSmsTemplateDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(SysSmsTemplateDO::getId)
        );
    }
}
