package cn.iocoder.dashboard.framework.errorcode.core;

/**
 * @author dylan
 */
public interface ErrorCodeAutoGenerator {

    /**
     * 将配置类到错误码写入数据库
     */
    void execute();
}
