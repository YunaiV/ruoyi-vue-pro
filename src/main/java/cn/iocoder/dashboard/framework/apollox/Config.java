package cn.iocoder.dashboard.framework.apollox;

import java.util.Set;

/**
 * 配置接口
 *
 * @author Jason Song(song_s@ctrip.com)
 */
public interface Config {

    /**
     * Return the property value with the given key, or {@code defaultValue} if the key doesn't exist.
     *
     * @param key          the property name
     * @param defaultValue the default value when key is not found or any error occurred
     * @return the property value
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return a set of the property names
     *
     * @return the property names
     */
    Set<String> getPropertyNames();

}
