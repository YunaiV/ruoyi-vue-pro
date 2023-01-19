import type { App } from 'vue'
// 👇使用 form-create 需额外全局引入 element plus 组件
import {
  ElAside,
  ElPopconfirm,
  ElHeader,
  ElMain,
  ElContainer,
  ElDivider,
  ElTransfer,
  ElAlert,
  ElTabs,
  ElTabPane
} from 'element-plus'

import formCreate from '@form-create/element-ui'
import install from '@form-create/element-ui/auto-import'
import FcDesigner from '@form-create/designer'

const components = [
  ElAside,
  ElPopconfirm,
  ElHeader,
  ElMain,
  ElContainer,
  ElDivider,
  ElTransfer,
  ElAlert,
  ElTabs,
  ElTabPane
]

export const setupFormCreate = (app: App<Element>) => {
  components.forEach((component) => {
    app.component(component.name, component)
  })

  formCreate.use(install)

  app.use(formCreate)

  app.use(FcDesigner)
}
