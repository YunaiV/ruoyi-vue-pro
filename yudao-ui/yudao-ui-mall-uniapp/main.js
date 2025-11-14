import App from './App';
import { createSSRApp } from 'vue';
import { setupPinia } from './sheep/store';


export function createApp() {

  const app = createSSRApp(App);
  
  setupPinia(app);

  return {
    app,
  };
}
