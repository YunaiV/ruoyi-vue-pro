package cn.iocoder.dashboard.framework.msg.sms.intercepter;

import java.util.Comparator;
import java.util.List;

/**
 * 消息父接口
 *
 * @author zzf
 * @date 2021/1/22 15:46
 */
public class DefaultSmsIntercepterChain extends AbstractSmsIntercepterChain {

    @Override
    public void addSmsIntercepter(List<SmsIntercepter> smsIntercepterList) {
        intercepterList.addAll(smsIntercepterList);
        //排序
        intercepterList.sort(Comparator.comparingInt(SmsIntercepter::getOrder));
    }
}
