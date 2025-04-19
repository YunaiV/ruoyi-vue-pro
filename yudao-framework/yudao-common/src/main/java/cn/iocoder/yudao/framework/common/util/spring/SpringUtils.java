package cn.iocoder.yudao.framework.common.util.spring;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Spring 工具类
 *
 * @author 芋道源码
 */
@Slf4j
public class SpringUtils extends SpringUtil  {

//    public static final String APPLICATION_YAML_NAME = "application.yaml";
//    public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";

    public static final String PROFILE_PROD = "prod";

    /**
     * 根据精确类型获取 Bean（忽略子类）。
     *
     * @param clazz 要检索的 Bean 的类
     * @param <T>   Bean 的类型
     * @return 精确类型的 Bean 实例
     * @throws IllegalStateException 如果未找到或找到多个精确类型的 Bean
     */
    public static <T> T getBeanByExactType(Class<T> clazz) {
        // 获取所有类型为 clazz 的 Bean（包括子类）
        Map<String, T> beansOfType = getApplicationContext().getBeansOfType(clazz);
        // 过滤 Bean 以保留仅精确匹配的 Bean
        Map<String, T> exactTypeBeans = beansOfType.entrySet().stream()
                .filter(entry -> AopUtils.getTargetClass(entry.getValue()).equals(clazz))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (exactTypeBeans.isEmpty()) {
            throw new IllegalStateException("未找到精确类型为 " + clazz.getName() + " 的 Bean");
        }
        if (exactTypeBeans.size() > 1) {
            throw new IllegalStateException("找到多个精确类型为 " + clazz.getName() + " 的 Bean");
        }
        // 返回单个 Bean
        return exactTypeBeans.values().iterator().next();
    }

    /**
     * 是否为生产环境
     *
     * @return 是否生产环境
     */
    public static boolean isProd() {
        String activeProfile = getActiveProfile();
        return Objects.equals(PROFILE_PROD, activeProfile);
    }

    /**
     * 通过class获取Bean.
     * @param superType
     * @return
     */
    public static <T> List<T> getBeans(Class<T> superType) {
        try {
            String[] names=getApplicationContext().getBeanNamesForType(superType);
            List<T> list=new ArrayList<>();
            for (String name : names) {
                T bean=(T)getApplicationContext().getBean(name);
                list.add(bean);
            }
            return list;
        } catch (BeansException e) {
            return null;
        }
    }

//    /**
//     * 获得当前环境类型
//     **/
//    public static EnvEnum getEnv() {
//        bootInitialize();
//        EnvEnum current = EnvEnum.current();
//        if(current==null || current==EnvEnum.UNKNOWN) {
//            String activeProfile = getActiveProfile();
//            current=EnvEnum.parse(activeProfile);
//            EnvEnum.current(current);
//            return current;
//        } else {
//            return EnvEnum.current();
//        }
//    }


//    private static YMLProperties APPLICATION_YML = null;
//    private static void initBootstrapYML() {
//
//        if(APPLICATION_YML!=null) return;
//        try {
//            String content = null;
//            File ymlFile=new File(APPLICATION_YAML_NAME);
//            if(ymlFile.exists()) {
//                log.info("read config from file "+ymlFile.getAbsolutePath());
//                content = FileUtil.readString(ymlFile,"UTF-8");
//            } else {
//                InputStream inputStream = SpringUtils.class.getClassLoader().getResourceAsStream(APPLICATION_YAML_NAME);
//                content = StreamUtil.input2string(inputStream, "UTF-8");
//                inputStream.close();
//                log.info("read config from resource /bootstrap.yml");
//            }
//            APPLICATION_YML = new YMLProperties();
//            APPLICATION_YML.load(content);
//            String activeProfile=APPLICATION_YML.getProperty(SPRING_PROFILES_ACTIVE).stringValue();
//            EnvEnum current = EnvEnum.parse(activeProfile);
//            EnvEnum.current(current);
//
//        } catch (Exception e) {
//            log.error("配置信息初始化失败",e);
//        }
//    }


//    private static Class STARTUP_CLASS = null;
//    private static Boolean IS_IN_IDE = null ;
//    /**
//     * 是否从IDE启动
//     * */
//    public static Boolean isBootInIDE() {
//        if(IS_IN_IDE!=null) return IS_IN_IDE;
//        Throwable ta=new Throwable();
//        StackTraceElement[] tas= ta.getStackTrace();
//        String clsName=tas[tas.length-1].getClassName();
//        STARTUP_CLASS= ClassUtil.loadClass(clsName);
//        try {
//            MavenProject project = new MavenProject(STARTUP_CLASS);
//            if(project.hasPomFile() && project.hasMainSourceDir() && project.hasTargetDir()) {
//                IS_IN_IDE = true;
//            } else {
//                IS_IN_IDE = false;
//            }} catch (Exception e) {
//            IS_IN_IDE = false;
//        }
//        return IS_IN_IDE;
//    }

//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) {
//       super.setApplicationContext(applicationContext);
//    }
//
//    /**
//     * 启动前初始化相关常量
//     **/
//    public static void bootInitialize() {
//        initBootstrapYML();
//        System.out.println("Env="+EnvEnum.current().name()+" "+EnvEnum.current().text());
//        // System.out.println("Env="+EnvEnum.current().name()+" "+EnvEnum.current().text()+" ; BOOT_IN_IDE="+isBootInIDE());
//
//    }

}
