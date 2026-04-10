package cn.iocoder.yudao.module.mes.dal.mysql.md.workstation;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.mes.dal.dataobject.md.workstation.MesMdWorkstationWorkerDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * MES 人力资源 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface MesMdWorkstationWorkerMapper extends BaseMapperX<MesMdWorkstationWorkerDO> {

    default List<MesMdWorkstationWorkerDO> selectListByWorkstationId(Long workstationId) {
        return selectList(MesMdWorkstationWorkerDO::getWorkstationId, workstationId);
    }

    default MesMdWorkstationWorkerDO selectByWorkstationIdAndPostId(Long workstationId, Long postId) {
        return selectOne(new LambdaQueryWrapper<MesMdWorkstationWorkerDO>()
                .eq(MesMdWorkstationWorkerDO::getWorkstationId, workstationId)
                .eq(MesMdWorkstationWorkerDO::getPostId, postId));
    }

    default void deleteByWorkstationId(Long workstationId) {
        delete(MesMdWorkstationWorkerDO::getWorkstationId, workstationId);
    }

}
