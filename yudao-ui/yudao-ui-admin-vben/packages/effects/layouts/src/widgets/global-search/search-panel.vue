<script setup lang="ts">
import type { MenuRecordRaw } from '@vben/types';

import { nextTick, onMounted, ref, shallowRef, watch } from 'vue';
import { useRouter } from 'vue-router';

import { SearchX, X } from '@vben/icons';
import { $t } from '@vben/locales';
import { mapTree, traverseTreeValues, uniqueByField } from '@vben/utils';

import { VbenIcon, VbenScrollbar } from '@vben-core/shadcn-ui';
import { isHttpUrl } from '@vben-core/shared/utils';

import { onKeyStroke, useLocalStorage, useThrottleFn } from '@vueuse/core';

defineOptions({
  name: 'SearchPanel',
});

const props = withDefaults(
  defineProps<{ keyword?: string; menus?: MenuRecordRaw[] }>(),
  {
    keyword: '',
    menus: () => [],
  },
);
const emit = defineEmits<{ close: [] }>();

const router = useRouter();
const searchHistory = useLocalStorage<MenuRecordRaw[]>(
  `__search-history-${location.hostname}__`,
  [],
);
const activeIndex = ref(-1);
const searchItems = shallowRef<MenuRecordRaw[]>([]);
const searchResults = ref<MenuRecordRaw[]>([]);

const handleSearch = useThrottleFn(search, 200);

// 搜索函数，用于根据搜索关键词查找匹配的菜单项
function search(searchKey: string) {
  // 去除搜索关键词的前后空格
  searchKey = searchKey.trim();

  // 如果搜索关键词为空，清空搜索结果并返回
  if (!searchKey) {
    searchResults.value = [];
    return;
  }

  // 使用搜索关键词创建正则表达式
  const reg = createSearchReg(searchKey);

  // 初始化结果数组
  const results: MenuRecordRaw[] = [];

  // 遍历搜索项
  traverseTreeValues(searchItems.value, (item) => {
    // 如果菜单项的名称匹配正则表达式，将其添加到结果数组中
    if (reg.test(item.name?.toLowerCase())) {
      results.push(item);
    }
  });

  // 更新搜索结果
  searchResults.value = results;

  // 如果有搜索结果，设置索引为 0
  if (results.length > 0) {
    activeIndex.value = 0;
  }

  // 赋值索引为 0
  activeIndex.value = 0;
}

// When the keyboard up and down keys move to an invisible place
// the scroll bar needs to scroll automatically
function scrollIntoView() {
  const element = document.querySelector(
    `[data-search-item="${activeIndex.value}"]`,
  );

  if (element) {
    element.scrollIntoView({ block: 'nearest' });
  }
}

// enter keyboard event
async function handleEnter() {
  if (searchResults.value.length === 0) {
    return;
  }
  const result = searchResults.value;
  const index = activeIndex.value;
  if (result.length === 0 || index < 0) {
    return;
  }
  const to = result[index];
  if (to) {
    searchHistory.value = uniqueByField([...searchHistory.value, to], 'path');
    handleClose();
    await nextTick();
    if (isHttpUrl(to.path)) {
      window.open(to.path, '_blank');
    } else {
      router.push({ path: to.path, replace: true });
    }
  }
}

// Arrow key up
function handleUp() {
  if (searchResults.value.length === 0) {
    return;
  }
  activeIndex.value--;
  if (activeIndex.value < 0) {
    activeIndex.value = searchResults.value.length - 1;
  }
  scrollIntoView();
}

// Arrow key down
function handleDown() {
  if (searchResults.value.length === 0) {
    return;
  }
  activeIndex.value++;
  if (activeIndex.value > searchResults.value.length - 1) {
    activeIndex.value = 0;
  }
  scrollIntoView();
}

// close search modal
function handleClose() {
  searchResults.value = [];
  emit('close');
}

// Activate when the mouse moves to a certain line
function handleMouseenter(e: MouseEvent) {
  const index = (e.target as HTMLElement)?.dataset.index;
  activeIndex.value = Number(index);
}

function removeItem(index: number) {
  if (props.keyword) {
    searchResults.value.splice(index, 1);
  } else {
    searchHistory.value.splice(index, 1);
  }
  activeIndex.value = Math.max(activeIndex.value - 1, 0);
  scrollIntoView();
}

// 存储所有需要转义的特殊字符
const code = new Set([
  '$',
  '(',
  ')',
  '*',
  '+',
  '.',
  '?',
  '[',
  '\\',
  ']',
  '^',
  '{',
  '|',
  '}',
]);

// 转换函数，用于转义特殊字符
function transform(c: string) {
  // 如果字符在特殊字符列表中，返回转义后的字符
  // 如果不在，返回字符本身
  return code.has(c) ? `\\${c}` : c;
}

// 创建搜索正则表达式
function createSearchReg(key: string) {
  // 将输入的字符串拆分为单个字符
  // 对每个字符进行转义
  // 然后用'.*'连接所有字符，创建正则表达式
  const keys = [...key].map((item) => transform(item)).join('.*');
  // 返回创建的正则表达式
  return new RegExp(`.*${keys}.*`);
}

watch(
  () => props.keyword,
  (val) => {
    if (val) {
      handleSearch(val);
    } else {
      searchResults.value = [...searchHistory.value];
    }
  },
);

onMounted(() => {
  searchItems.value = mapTree(props.menus, (item) => {
    return {
      ...item,
      name: $t(item?.name),
    };
  });
  if (searchHistory.value.length > 0) {
    searchResults.value = searchHistory.value;
  }
  // enter search
  onKeyStroke('Enter', handleEnter);
  // Monitor keyboard arrow keys
  onKeyStroke('ArrowUp', handleUp);
  onKeyStroke('ArrowDown', handleDown);
  // esc close
  onKeyStroke('Escape', handleClose);
});
</script>

<template>
  <VbenScrollbar>
    <div class="!flex h-full justify-center px-2 sm:max-h-[450px]">
      <!-- 无搜索结果 -->
      <div
        v-if="keyword && searchResults.length === 0"
        class="text-muted-foreground text-center"
      >
        <SearchX class="mx-auto mt-4 size-12" />
        <p class="mb-10 mt-6 text-xs">
          {{ $t('ui.widgets.search.noResults') }}
          <span class="text-foreground text-sm font-medium">
            "{{ keyword }}"
          </span>
        </p>
      </div>
      <!-- 历史搜索记录 & 没有搜索结果 -->
      <div
        v-if="!keyword && searchResults.length === 0"
        class="text-muted-foreground text-center"
      >
        <p class="my-10 text-xs">
          {{ $t('ui.widgets.search.noRecent') }}
        </p>
      </div>

      <ul v-show="searchResults.length > 0" class="w-full">
        <li
          v-if="searchHistory.length > 0 && !keyword"
          class="text-muted-foreground mb-2 text-xs"
        >
          {{ $t('ui.widgets.search.recent') }}
        </li>
        <li
          v-for="(item, index) in uniqueByField(searchResults, 'path')"
          :key="item.path"
          :class="
            activeIndex === index
              ? 'active bg-primary text-primary-foreground'
              : ''
          "
          :data-index="index"
          :data-search-item="index"
          class="bg-accent flex-center group mb-3 w-full cursor-pointer rounded-lg px-4 py-4"
          @click="handleEnter"
          @mouseenter="handleMouseenter"
        >
          <VbenIcon
            :icon="item.icon"
            class="mr-2 size-5 flex-shrink-0"
            fallback
          />

          <span class="flex-1">{{ item.name }}</span>
          <div
            class="flex-center dark:hover:bg-accent hover:text-primary-foreground rounded-full p-1 hover:scale-110"
            @click.stop="removeItem(index)"
          >
            <X class="size-4" />
          </div>
        </li>
      </ul>
    </div>
  </VbenScrollbar>
</template>
