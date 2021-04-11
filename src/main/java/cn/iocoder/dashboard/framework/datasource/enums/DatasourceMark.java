package cn.iocoder.dashboard.framework.datasource.enums;

/**
 * 对应于多数据源中不同数据源配置
 *
 * 在方法上使用注解{@code  @DS(DatasourceMark.slave)}可以指定slave数据源，默认是master数据源
 */
public interface DatasourceMark {
    String master = "master";
    String slave = "slave";
}
