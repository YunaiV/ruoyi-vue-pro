package cn.iocoder.dashboard.modules.system.dal.mysql.dao.notice;

import cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.notice.SysNoticeDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysNoticeMapper extends BaseMapper<SysNoticeDO> {
}
