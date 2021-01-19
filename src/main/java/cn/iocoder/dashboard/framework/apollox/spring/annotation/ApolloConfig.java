package cn.iocoder.dashboard.framework.apollox.spring.annotation;

import java.lang.annotation.*;

/**
 * Use this annotation to inject Apollo Config Instance.
 *
 * <p>Usage example:</p>
 * <pre class="code">
 * //Inject the config for "someNamespace"
 * &#064;ApolloConfig("someNamespace")
 * private Config config;
 * </pre>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ApolloConfig {

}
