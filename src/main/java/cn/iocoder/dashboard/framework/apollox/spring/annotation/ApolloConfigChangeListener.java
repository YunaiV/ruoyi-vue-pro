package cn.iocoder.dashboard.framework.apollox.spring.annotation;

import java.lang.annotation.*;

/**
 * Use this annotation to register Apollo ConfigChangeListener.
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApolloConfigChangeListener {

}
