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

// TODO @puhui999：【有个迁移的打印】【新增】流程打印，由 [@Lesan](https://gitee.com/LesanOuO) 贡献 [#816](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/816/)、[#1418](https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1418/)、[#817](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/817/)、[#1419](https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1419/)、[#1424](https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1424)、[#819](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/819)、[#821](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/821/)
