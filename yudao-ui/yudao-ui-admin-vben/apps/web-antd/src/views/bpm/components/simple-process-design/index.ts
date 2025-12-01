import './styles/simple-process-designer.scss';

export { default as HttpRequestSetting } from './components/nodes-config/modules/http-request-setting.vue';

export { default as SimpleProcessDesigner } from './components/simple-process-designer.vue';

export { default as SimpleProcessViewer } from './components/simple-process-viewer.vue';

export type { SimpleFlowNode } from './consts';

export { parseFormFields } from './helpers';
