package cn.iocoder.yudao.framework.pay.core.enums;

// TODO @芋艿：感觉情况有点多，得讨论下
/**
 * 统一的渠道返回结果
 * @author  jason
 */
public enum PayChannelRespEnum {

    /**
     * 接口通讯正常返回, 并明确处理成功 ，不需要通过查询或者回调接口 进行下一步处理
     */
    SYNC_SUCCESS,

    /**
     * 接口通讯正常返回， 但返回错误，并且不能通过重试解决的错误，
     * 如提交失败， 业务错误（余额不足）， 或者参数错误， 签名错误， 需要干预后才能处理
     */
    CAN_NOT_RETRY_FAILURE,


    /**
     * 接口通讯正常返回，
     * 可以通过重试解决的错误. 如系统超时， 系统繁忙。状态未知 不能改变请求参数，如退款单请求号，重发请求
     */
    RETRY_FAILURE,


    /**
     * 接口通讯正常返回，但是处理结果 需要渠道回调进行下一步处理
     */
    PROCESSING_NOTIFY,


    /**
     * 接口通讯正常返回， 但是处理结果,需要调用查询接口 进行查询
     */
    PROCESSING_QUERY,


    /**
     * 本系统调用渠道接口异常， 渠道接口请求未正常发送， 本系统不可预知的异常，较少发生， 可认为失败。  不用重试.
     */
    CALL_EXCEPTION,


    /**
     * 本系统调用渠道接口成功， 但是未接受到请求结果，较少发生（需合理设置read time out )  结果未知。 需要调用查询接口进行查询
     */
    READ_TIME_OUT_EXCEPTION;
}
