import type { App, Component } from 'vue';

import FcDesigner from '@form-create/designer';
import formCreate from '@form-create/element-ui';
import install from '@form-create/element-ui/auto-import';
// 👇使用 form-create 需额外全局引入 element plus 组件
import {
  ElAlert,
  ElAside,
  ElBadge,
  ElCard,
  ElCollapse,
  ElCollapseItem,
  ElContainer,
  ElDivider,
  ElDropdown,
  ElDropdownItem,
  ElDropdownMenu,
  ElFooter,
  ElHeader,
  ElMain,
  ElMenu,
  ElMenuItem,
  ElMessage,
  ElPopconfirm,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElTag,
  ElText,
  ElTransfer,
} from 'element-plus';

// ======================= 自定义组件 =======================
import { useApiSelect } from '#/components/form-create';
import DictSelect from '#/components/form-create/components/dict-select.vue';
import { useImagesUpload } from '#/components/form-create/components/use-images-upload';
import { Tinymce } from '#/components/tinymce';
import { FileUpload, ImageUpload } from '#/components/upload';

const UserSelect = useApiSelect({
  name: 'UserSelect',
  labelField: 'nickname',
  valueField: 'id',
  url: '/system/user/simple-list',
});
const DeptSelect = useApiSelect({
  name: 'DeptSelect',
  labelField: 'name',
  valueField: 'id',
  url: '/system/dept/simple-list',
});
const ApiSelect = useApiSelect({
  name: 'ApiSelect',
});
const ImagesUpload = useImagesUpload();

const components = [
  ImageUpload,
  ImagesUpload,
  FileUpload,
  Tinymce,
  DictSelect,
  UserSelect,
  DeptSelect,
  ApiSelect,
  ElAlert,
  ElTransfer,
  ElAside,
  ElContainer,
  ElDivider,
  ElHeader,
  ElMain,
  ElPopconfirm,
  ElTable,
  ElTableColumn,
  ElTabPane,
  ElTabs,
  ElDropdown,
  ElDropdownMenu,
  ElDropdownItem,
  ElBadge,
  ElTag,
  ElText,
  ElMenu,
  ElMenuItem,
  ElFooter,
  ElMessage,
  ElCollapse,
  ElCollapseItem,
  ElCard,
];

// 参考 http://www.form-create.com/v3/element-ui/auto-import.html 文档
export const setupFormCreate = (app: App<Element>) => {
  components.forEach((component) => {
    app.component(component.name as string, component as Component);
  });
  formCreate.use(install);
  app.use(formCreate);
  app.use(FcDesigner);
};
