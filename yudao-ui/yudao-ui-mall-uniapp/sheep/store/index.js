import { createPinia } from 'pinia';
import piniaPersist from 'pinia-plugin-persist-uni';

// 自动注入所有pinia模块
const files = import.meta.glob('./*.js', { eager: true });
const modules = {};
Object.keys(files).forEach((key) => {
  modules[key.replace(/(.*\/)*([^.]+).*/gi, '$2')] = files[key].default;
});

export const setupPinia = (app) => {
  const pinia = createPinia();
  pinia.use(piniaPersist);

  app.use(pinia);
};

export default (name) => {
  return modules[name]();
};
