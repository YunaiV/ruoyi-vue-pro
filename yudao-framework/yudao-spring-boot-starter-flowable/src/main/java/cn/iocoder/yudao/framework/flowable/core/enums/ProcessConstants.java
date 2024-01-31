package cn.iocoder.yudao.framework.flowable.core.enums;

/**
 * 流程常量信息
 */
public interface ProcessConstants {

    String BPMN_FILE_SUFFIX = ".bpmn";

    /**
     * nameapace
     * bpmn中的命名空间
     * 这个东西有可能导致无法切换工作流程的实现
     */
    String NAMESPASE = "http://flowable.org/bpmn";

    /**
     * 自定义属性 dataType
     */
    String PROCESS_CUSTOM_DATA_TYPE = "dataType";

}
