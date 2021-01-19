package cn.iocoder.dashboard.framework.apollox.model;


import cn.iocoder.dashboard.framework.apollox.enums.PropertyChangeType;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Holds the information for a config change.
 * 配置每个属性变化的信息
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@AllArgsConstructor
public class ConfigChange {

    /**
     * 属性名
     */
    private final String propertyName;
    /**
     * 老值
     */
    private String oldValue;
    /**
     * 新值
     */
    private String newValue;
    /**
     * 变化类型
     */
    private PropertyChangeType changeType;

}
