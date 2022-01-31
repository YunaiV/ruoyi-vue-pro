package cn.iocoder.yudao.module.infra.convert.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogExcelVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.log.InfJobLogRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.InfJobLogDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 定时任务日志 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface InfJobLogConvert {

    InfJobLogConvert INSTANCE = Mappers.getMapper(InfJobLogConvert.class);

    InfJobLogRespVO convert(InfJobLogDO bean);

    List<InfJobLogRespVO> convertList(List<InfJobLogDO> list);

    PageResult<InfJobLogRespVO> convertPage(PageResult<InfJobLogDO> page);

    List<InfJobLogExcelVO> convertList02(List<InfJobLogDO> list);

}
