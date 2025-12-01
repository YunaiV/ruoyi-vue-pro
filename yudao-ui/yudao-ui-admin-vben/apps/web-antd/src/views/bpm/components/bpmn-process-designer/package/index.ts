import './theme/index.scss';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-codes.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
// TODO @puhui999：样式问题：设计器那，位置不太对；

export { default as MyProcessDesigner } from './designer';
// TODO @puhui999：流程发起时，预览相关的，需要使用；
export { default as MyProcessViewer } from './designer/index2';
export { default as MyProcessPenal } from './penal';
