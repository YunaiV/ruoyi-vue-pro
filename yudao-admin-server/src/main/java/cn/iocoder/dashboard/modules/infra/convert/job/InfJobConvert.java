package cn.iocoder.dashboard.modules.infra.convert.job;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobExcelVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobRespVO;
import cn.iocoder.dashboard.modules.infra.controller.job.vo.job.InfJobUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.job.InfJobDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 定时任务 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfJobConvert {

    InfJobConvert INSTANCE = Mappers.getMapper(InfJobConvert.class);

    InfJobDO convert(InfJobCreateReqVO bean);

    InfJobDO convert(InfJobUpdateReqVO bean);

    InfJobRespVO convert(InfJobDO bean);

    List<InfJobRespVO> convertList(List<InfJobDO> list);

    PageResult<InfJobRespVO> convertPage(PageResult<InfJobDO> page);

    List<InfJobExcelVO> convertList02(List<InfJobDO> list);

}
