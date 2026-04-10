package cn.iocoder.yudao.module.mes.dal.mysql.md.workstation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationToolDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 工装夹具资源 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdWorkstationToolMapper extends BaseMapperX<MesMdWorkstationToolDO> {

    default List<MesMdWorkstationToolDO> selectListByWorkstationId(Long workstationId) {
        return selectList(MesMdWorkstationToolDO::getWorkstationId, workstationId);
    }

    default MesMdWorkstationToolDO selectByWorkstationIdAndToolTypeId(Long workstationId, Long toolTypeId) {
        return selectOne(new LambdaQueryWrapper<MesMdWorkstationToolDO>()
                .eq(MesMdWorkstationToolDO::getWorkstationId, workstationId)
                .eq(MesMdWorkstationToolDO::getToolTypeId, toolTypeId));
    }

    default void deleteByWorkstationId(Long workstationId) {
        delete(MesMdWorkstationToolDO::getWorkstationId, workstationId);
    }

}
