package cn.iocoder.yudao.module.system.convert.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogExcelVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SysSmsLogRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 短信日志 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SmsLogConvert {

    SmsLogConvert INSTANCE = Mappers.getMapper(SmsLogConvert.class);

    SysSmsLogRespVO convert(SysSmsLogDO bean);

    List<SysSmsLogRespVO> convertList(List<SysSmsLogDO> list);

    PageResult<SysSmsLogRespVO> convertPage(PageResult<SysSmsLogDO> page);

    List<SmsLogExcelVO> convertList02(List<SysSmsLogDO> list);

}
