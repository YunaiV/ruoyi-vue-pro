<script lang="ts" setup>
import type { Menu } from './types';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import draggable from 'vuedraggable';

const props = defineProps<{
  accountId: number;
  activeIndex: string;
  modelValue: Menu[];
  parentIndex: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: Menu[]): void;
  (e: 'menuClicked', parent: Menu, x: number): void;
  (e: 'submenuClicked', child: Menu, x: number, y: number): void;
}>();

const menuList = computed<Menu[]>({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

/** 添加横向一级菜单 */
function addMenu() {
  const index = menuList.value.length;
  const menu = {
    name: '菜单名称',
    children: [],
    reply: {
      // 用于存储回复内容
      type: 'text',
      accountId: props.accountId, // 保证组件里，可以使用到对应的公众号
    },
  };
  menuList.value[index] = menu;
  menuClicked(menu, index - 1);
}

/** 添加横向二级菜单；parent 表示要操作的父菜单 */
function addSubMenu(i: number, parent: any) {
  parent.children[parent.children.length] = {
    name: '子菜单名称',
    reply: {
      // 用于存储回复内容
      type: 'text',
      accountId: props.accountId, // 保证组件里，可以使用到对应的公众号
    },
  };
  subMenuClicked(
    parent.children[parent.children.length - 1],
    i,
    parent.children.length - 1,
  );
}

/** 一级菜单点击 */
function menuClicked(parent: Menu, x: number) {
  emit('menuClicked', parent, x);
}

/** 二级菜单点击 */
function subMenuClicked(child: Menu, x: number, y: number) {
  emit('submenuClicked', child, x, y);
}

/**
 * 处理一级菜单展开后被拖动，激活(展开)原来活动的一级菜单
 *
 * @param options - 拖动参数对象
 * @param options.oldIndex - 一级菜单拖动前的位置
 * @param options.newIndex - 一级菜单拖动后的位置
 */
function onParentDragEnd({
  oldIndex,
  newIndex,
}: {
  newIndex: number;
  oldIndex: number;
}) {
  // 二级菜单没有展开，直接返回
  if (props.activeIndex === '__MENU_NOT_SELECTED__') {
    return;
  }

  // 使用一个辅助数组来模拟菜单移动，然后找到展开的二级菜单的新下标`newParent`
  const positions = Array.from({ length: menuList.value.length }).fill(false);
  positions[props.parentIndex] = true;
  const [out] = positions.splice(oldIndex, 1); // 移出菜单，保存到变量out
  positions.splice(newIndex, 0, out ?? false); // 把out变量插入被移出的菜单
  const newParentIndex = positions.indexOf(true);

  // 找到菜单元素，触发一级菜单点击
  const parent = menuList.value[newParentIndex];
  if (parent && newParentIndex !== -1) {
    emit('menuClicked', parent, newParentIndex);
  }
}

/**
 * 处理二级菜单展开后被拖动，激活被拖动的菜单
 *
 * @param options - 拖动参数对象
 * @param options.newIndex - 二级菜单拖动后的位置
 */
function onChildDragEnd({ newIndex }: { newIndex: number }) {
  const x = props.parentIndex;
  const y = newIndex;
  const children = menuList.value[x]?.children;
  if (children && children?.length > 0) {
    const child = children[y];
    if (child) {
      emit('submenuClicked', child, x, y);
    }
  }
}
</script>

<template>
  <draggable
    v-model="menuList"
    item-key="id"
    ghost-class="draggable-ghost"
    :animation="400"
    @end="onParentDragEnd"
  >
    <template #item="{ element: parent, index: x }">
      <div
        class="relative float-left box-border block w-[85.5px] cursor-pointer border border-[#ebedee] bg-white text-center"
      >
        <!-- 一级菜单 -->
        <div
          @click="menuClicked(parent, x)"
          class="box-border flex h-[44px] w-full items-center justify-center leading-[44px]"
          :class="{ 'border border-[#2bb673]': props.activeIndex === `${x}` }"
        >
          <IconifyIcon icon="lucide:panel-right-open" color="black" />
          {{ parent.name }}
        </div>
        <!-- 以下为二级菜单-->
        <div
          class="absolute bottom-[45px] left-0 w-[85.5px]"
          v-if="props.parentIndex === x && parent.children"
        >
          <draggable
            v-model="parent.children"
            item-key="id"
            ghost-class="draggable-ghost"
            :animation="400"
            @end="onChildDragEnd"
          >
            <template #item="{ element: child, index: y }">
              <div
                class="relative float-left box-border block w-[85.5px] cursor-pointer border border-[#ebedee] bg-white text-center"
              >
                <div
                  class="box-border h-[44px] text-center leading-[44px]"
                  :class="{
                    'border border-[#2bb673]':
                      props.activeIndex === `${x}-${y}`,
                  }"
                  @click="subMenuClicked(child, x, y)"
                >
                  {{ child.name }}
                </div>
              </div>
            </template>
          </draggable>
          <!-- 二级菜单加号， 当长度 小于 5 才显示二级菜单的加号  -->
          <div
            class="relative float-left box-border block flex h-[46px] w-[85.5px] cursor-pointer items-center justify-center border border-[#ebedee] bg-white text-center leading-[46px]"
            v-if="!parent.children || parent.children.length < 5"
            @click="addSubMenu(x, parent)"
          >
            <IconifyIcon icon="lucide:plus" class="text-[#2bb673]" />
          </div>
        </div>
      </div>
    </template>
  </draggable>

  <!-- 一级菜单加号 -->
  <div
    class="relative float-left box-border block flex h-[46px] w-[85.5px] cursor-pointer items-center justify-center border border-[#ebedee] bg-white text-center leading-[46px]"
    v-if="menuList.length < 3"
    @click="addMenu"
  >
    <IconifyIcon icon="lucide:plus" class="text-[#2bb673]" />
  </div>
</template>

<style lang="scss" scoped>
.draggable-ghost {
  background: #f7fafc;
  border: 1px solid #4299e1;
  opacity: 0.5;
}
</style>
