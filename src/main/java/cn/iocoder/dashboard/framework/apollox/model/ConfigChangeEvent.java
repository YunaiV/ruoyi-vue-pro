package cn.iocoder.dashboard.framework.apollox.model;

import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * A change event when a namespace's config is changed.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@AllArgsConstructor
public class ConfigChangeEvent {

    /**
     * 变化属性的集合
     *
     * KEY：属性名
     * VALUE：配置变化
     */
    private final Map<String, ConfigChange> m_changes;

    /**
     * Get the keys changed.
     *
     * @return the list of the keys
     */
    public Set<String> changedKeys() {
        return m_changes.keySet();
    }

    /**
     * Get a specific change instance for the key specified.
     *
     * @param key the changed key
     * @return the change instance
     */
    public ConfigChange getChange(String key) {
        return m_changes.get(key);
    }

    /**
     * Check whether the specified key is changed
     *
     * @param key the key
     * @return true if the key is changed, false otherwise.
     */
    public boolean isChanged(String key) {
        return m_changes.containsKey(key);
    }

}
