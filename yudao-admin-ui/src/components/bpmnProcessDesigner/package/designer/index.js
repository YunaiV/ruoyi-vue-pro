import MyProcessDesigner from "./ProcessDesigner.vue";

MyProcessDesigner.install = function(Vue) {
  Vue.component(MyProcessDesigner.name, MyProcessDesigner);
};

export default MyProcessDesigner;
