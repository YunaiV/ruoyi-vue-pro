import type { VxeTableInstance, VxeToolbarInstance } from 'vxe-table';

import { ref, watch } from 'vue';

import VbenVxeTableToolbar from './table-toolbar.vue';

/**
 * vxe 原生工具栏挂载封装
 * 解决每个组件使用 vxe-table 组件时都需要写一遍的问题
 */
export function useTableToolbar() {
  const hiddenSearchBar = ref(false); // 隐藏搜索栏
  const tableToolbarRef = ref<InstanceType<typeof VbenVxeTableToolbar>>();
  const tableRef = ref<VxeTableInstance>();
  const isBound = ref<boolean>(false);

  /** 挂载 toolbar 工具栏 */
  async function bindTableToolbar() {
    const table = tableRef.value;
    const tableToolbar = tableToolbarRef.value;
    if (table && tableToolbar) {
      // 延迟 1 秒，确保 toolbar 组件已经挂载
      setTimeout(async () => {
        const toolbar = tableToolbar.getToolbarRef();
        if (!toolbar) {
          console.error('[toolbar 挂载失败] Table toolbar not found');
        }
        await table.connectToolbar(toolbar as VxeToolbarInstance);
        isBound.value = true;
      }, 1000); // 延迟挂载确保 toolbar 正确挂载
    }
  }

  watch(
    () => tableRef.value,
    async (val) => {
      if (!val || isBound.value) return;
      await bindTableToolbar();
    },
    { immediate: true },
  );

  return {
    hiddenSearchBar,
    tableToolbarRef,
    tableRef,
  };
}
