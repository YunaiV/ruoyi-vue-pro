package cn.iocoder.yudao.module.system.service.sms;

import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.sms.vo.log.SmsLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.dal.dataobject.sms.SmsTemplateDO;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 短信日志 Service 接口
 *
 * @author zzf
 * @date 13:48 2021/3/2
 */
public interface SmsLogService {

    /**
     * 创建短信日志
     *
     * @param mobile 手机号
     * @param userId 用户编号
     * @param userType 用户类型
     * @param isSend 是否发送
     * @param template 短信模板
     * @param templateContent 短信内容
     * @param templateParams 短信参数
     * @return 发送日志编号
     */
    Long createSmsLog(String mobile, Long userId, Integer userType, Boolean isSend,
                      SmsTemplateDO template, String templateContent, Map<String, Object> templateParams);

    /**
     * 更新日志的发送结果
     *
     * @param id 日志编号
     * @param sendCode 发送结果的编码
     * @param sendMsg 发送结果的提示
     * @param apiSendCode 短信 API 发送结果的编码
     * @param apiSendMsg 短信 API 发送失败的提示
     * @param apiRequestId 短信 API 发送返回的唯一请求 ID
     * @param apiSerialNo 短信 API 发送返回的序号
     */
    void updateSmsSendResult(Long id, Integer sendCode, String sendMsg,
                             String apiSendCode, String apiSendMsg, String apiRequestId, String apiSerialNo);

    /**
     * 更新日志的接收结果
     *
     * @param id 日志编号
     * @param success 是否接收成功
     * @param receiveTime 用户接收时间
     * @param apiReceiveCode API 接收结果的编码
     * @param apiReceiveMsg API 接收结果的说明
     */
    void updateSmsReceiveResult(Long id, Boolean success, Date receiveTime, String apiReceiveCode, String apiReceiveMsg);

    /**
     * 获得短信日志分页
     *
     * @param pageReqVO 分页查询
     * @return 短信日志分页
     */
    PageResult<SmsLogDO> getSmsLogPage(SmsLogPageReqVO pageReqVO);

    /**
     * 获得短信日志列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 短信日志列表
     */
    List<SmsLogDO> getSmsLogList(SmsLogExportReqVO exportReqVO);

}
