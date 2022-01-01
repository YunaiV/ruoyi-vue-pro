import MyProcessDesigner from "./designer";
import MyProcessPenal from "./penal";

const components = [MyProcessDesigner, MyProcessPenal];

const install = function(Vue) {
  components.forEach(component => {
    Vue.component(component.name, component);
  });
};

if (typeof window !== "undefined" && window.Vue) {
  install(window.Vue);
}

export default {
  version: "0.0.1",
  install,
  ...components
};
