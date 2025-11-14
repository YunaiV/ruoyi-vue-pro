import MyProcessViewer from './ProcessViewer.vue';

MyProcessViewer.install = function (Vue: any) {
  Vue.component(MyProcessViewer.name, MyProcessViewer);
};

// 流程图的查看器，不可编辑
export default MyProcessViewer;
