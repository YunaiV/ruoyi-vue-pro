package cn.iocoder.yudao.module.infra.convert.demo02;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentExcelVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentRespVO;
import cn.iocoder.yudao.module.infra.controller.admin.demo02.vo.InfraDemoStudentUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.demo02.InfraDemoStudentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 学生 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface InfraDemoStudentConvert {

    InfraDemoStudentConvert INSTANCE = Mappers.getMapper(InfraDemoStudentConvert.class);

    InfraDemoStudentDO convert(InfraDemoStudentCreateReqVO bean);

    InfraDemoStudentDO convert(InfraDemoStudentUpdateReqVO bean);

    InfraDemoStudentRespVO convert(InfraDemoStudentDO bean);

    List<InfraDemoStudentRespVO> convertList(List<InfraDemoStudentDO> list);

    PageResult<InfraDemoStudentRespVO> convertPage(PageResult<InfraDemoStudentDO> page);

    List<InfraDemoStudentExcelVO> convertList02(List<InfraDemoStudentDO> list);

}