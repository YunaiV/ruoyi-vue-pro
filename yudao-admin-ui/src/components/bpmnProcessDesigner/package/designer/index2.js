import MyProcessViewer from "./ProcessViewer.vue";

MyProcessViewer.install = function(Vue) {
  Vue.component(MyProcessViewer.name, MyProcessViewer);
};

export default MyProcessViewer;
