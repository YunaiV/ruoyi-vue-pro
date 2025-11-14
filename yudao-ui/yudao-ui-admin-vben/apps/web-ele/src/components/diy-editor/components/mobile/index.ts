import { defineAsyncComponent } from 'vue';

/*
 * 组件注册
 *
 * 组件规范：
 * 1. 每个子目录就是一个独立的组件，每个目录包括以下三个文件：
 * 2. config.ts：组件配置，必选，用于定义组件、组件默认的属性、定义属性的类型
 * 3. index.vue：组件展示，用于展示组件的渲染效果。可以不提供，如 Page（页面设置），只需要属性配置表单即可
 * 4. property.vue：组件属性表单，用于配置组件，必选，
 *
 * 注：
 * 组件ID以config.ts中配置的id为准，与组件目录的名称无关，但还是建议组件目录的名称与组件ID保持一致
 */

// 导入组件界面模块
const viewModules: Record<string, any> = import.meta.glob('./*/*.vue');
// 导入配置模块
const configModules: Record<string, any> = import.meta.glob('./*/config.ts', {
  eager: true,
});

// 界面模块
const components: Record<string, any> = {};
// 组件配置模块
const componentConfigs: Record<string, any> = {};

// 组件界面的类型
type ViewType = 'index' | 'property';

/**
 * 注册组件的界面模块
 *
 * @param componentId 组件ID
 * @param configPath 配置模块的文件路径
 * @param viewType 组件界面的类型
 */
const registerComponentViewModule = (
  componentId: string,
  configPath: string,
  viewType: ViewType,
) => {
  const viewPath = configPath.replace('config.ts', `${viewType}.vue`);
  const viewModule = viewModules[viewPath];
  if (viewModule) {
    // 定义异步组件
    components[componentId] = defineAsyncComponent(viewModule);
  }
};

// 注册
Object.keys(configModules).forEach((modulePath: string) => {
  const component = configModules[modulePath].component;
  const componentId = component?.id;
  if (componentId) {
    // 注册组件
    componentConfigs[componentId] = component;
    // 注册预览界面
    registerComponentViewModule(componentId, modulePath, 'index');
    // 注册属性配置表单
    registerComponentViewModule(
      `${componentId}Property`,
      modulePath,
      'property',
    );
  }
});

export { componentConfigs, components };
