<script lang="ts" setup>
import type { SystemMenuApi } from '#/api/system/menu';
import type { SystemTenantPackageApi } from '#/api/system/tenant-package';

import { computed, ref } from 'vue';

import { Tree, useVbenModal } from '@vben/common-ui';
import { handleTree } from '@vben/utils';

import { Checkbox, message, Spin } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getMenuList } from '#/api/system/menu';
import {
  createTenantPackage,
  getTenantPackage,
  updateTenantPackage,
} from '#/api/system/tenant-package';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemTenantPackageApi.TenantPackage>();
const getTitle = computed(() => {
  return formData.value
    ? $t('ui.actionTitle.edit', ['套餐'])
    : $t('ui.actionTitle.create', ['套餐']);
});
const menuTree = ref<SystemMenuApi.Menu[]>([]); // 菜单树
const menuLoading = ref(false); // 加载菜单列表
const isAllSelected = ref(false); // 全选状态
const isExpanded = ref(false); // 展开状态
const expandedKeys = ref<number[]>([]); // 展开的节点

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as SystemTenantPackageApi.TenantPackage;
    try {
      await (formData.value
        ? updateTenantPackage(data)
        : createTenantPackage(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载菜单列表
    await loadMenuTree();
    // 加载数据
    const data = modalApi.getData<SystemTenantPackageApi.TenantPackage>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getTenantPackage(data.id);
      await formApi.setValues(data);
    } finally {
      modalApi.unlock();
    }
  },
});

/** 加载菜单树 */
async function loadMenuTree() {
  menuLoading.value = true;
  try {
    const data = await getMenuList();
    menuTree.value = handleTree(data) as SystemMenuApi.Menu[];
  } finally {
    menuLoading.value = false;
  }
}

/** 全选/全不选 */
function handleSelectAll() {
  isAllSelected.value = !isAllSelected.value;
  if (isAllSelected.value) {
    const allIds = getAllNodeIds(menuTree.value);
    formApi.setFieldValue('menuIds', allIds);
  } else {
    formApi.setFieldValue('menuIds', []);
  }
}

/** 展开/折叠所有节点 */
function handleExpandAll() {
  isExpanded.value = !isExpanded.value;
  expandedKeys.value = isExpanded.value ? getAllNodeIds(menuTree.value) : [];
}

/** 递归获取所有节点 ID */
function getAllNodeIds(nodes: any[], ids: number[] = []): number[] {
  nodes.forEach((node: any) => {
    ids.push(node.id);
    if (node.children && node.children.length > 0) {
      getAllNodeIds(node.children, ids);
    }
  });
  return ids;
}
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-6">
      <template #menuIds="slotProps">
        <Spin :spinning="menuLoading" wrapper-class-name="w-full">
          <Tree
            class="max-h-96 overflow-y-auto"
            :tree-data="menuTree"
            multiple
            bordered
            :default-expanded-keys="expandedKeys"
            v-bind="slotProps"
            value-field="id"
            label-field="name"
          />
        </Spin>
      </template>
    </Form>
    <template #prepend-footer>
      <div class="flex flex-auto items-center">
        <Checkbox :checked="isAllSelected" @change="handleSelectAll">
          全选
        </Checkbox>
        <Checkbox :checked="isExpanded" @change="handleExpandAll">
          全部展开
        </Checkbox>
      </div>
    </template>
  </Modal>
</template>
