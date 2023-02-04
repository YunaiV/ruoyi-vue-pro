import { App } from 'vue'
import MyProcessDesigner from './designer'
import MyProcessPenal from './penal'
import MyProcessViewer from './designer/index2'

const components = [MyProcessDesigner, MyProcessPenal, MyProcessViewer]

// const install = function (Vue) {
//   components.forEach(component => {
//     Vue.component(component.name, component)
//   })
// }

// if (typeof window !== "undefined" && window.Vue) {
//   install(window.Vue)
// }
//   components.forEach(component => {
//     Vue.component(component.name, component)
//   })
const componentss = {
  install: (Vue: App): void => {
    components.forEach((component) => {
      Vue.component(component.name, component)
    })
  }
}
// let version = "0.0.1"
export const MyPD = (app) => {
  // export default {
  // app.use(version)
  // app.use(install)
  // app.use(MyProcessDesigner)
  // app.use(MyProcessPenal)
  // app.use(MyProcessViewer)
  // app.use(components)
  app.use(componentss)
}
