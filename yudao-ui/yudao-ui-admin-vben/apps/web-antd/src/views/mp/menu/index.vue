<script lang="ts" setup>
import type { Menu, RawMenu } from './modules/types';

import { nextTick, onMounted, ref } from 'vue';

import { confirm, ContentWrap, DocAlert, Page } from '@vben/common-ui';
import { handleTree } from '@vben/utils';

import { Button, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getSimpleAccountList } from '#/api/mp/account';
import { deleteMenu, getMenuList, saveMenu } from '#/api/mp/menu';
import {
  Level,
  MENU_NOT_SELECTED,
  useGridFormSchema,
} from '#/views/mp/menu/data';
import Editor from '#/views/mp/menu/modules/editor.vue';
import Previewer from '#/views/mp/menu/modules/previewer.vue';

import iphoneBackImg from './assets/iphone_backImg.png';
import menuFootImg from './assets/menu_foot.png';
import menuHeadImg from './assets/menu_head.png';

defineOptions({ name: 'MpMenu' });

// ======================== 列表查询 ========================

const loading = ref(false); // 遮罩层
const accountId = ref(-1);
const accountName = ref<string>('');
const menuList = ref<Menu[]>([]);

const [AccountForm, accountFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-[240px]',
    },
  },
  layout: 'horizontal',
  schema: useGridFormSchema(),
  wrapperClass: 'grid-cols-1',
  showDefaultActions: false,
  handleValuesChange: async (values, changedFields) => {
    // 当 accountId 字段变化时（包括 autoSelect 自动选择），同步更新 accountId
    if (changedFields.includes('accountId') && values.accountId) {
      await onAccountChanged(values);
    }
  },
});

// ======================== 菜单操作 ========================

// 当前选中菜单编码：
//  * 一级（'x'）
//  * 二级（'x-y'）
//  * 未选中（MENU_NOT_SELECTED）
const activeIndex = ref<string>(MENU_NOT_SELECTED);
// 二级菜单显示标志: 归属的一级菜单index
// * 未初始化：-1
// * 初始化：x
const parentIndex = ref(-1);

// ======================== 菜单编辑 ========================

const showRightPanel = ref(false); // 右边配置显示默认详情还是配置详情
const isParent = ref<boolean>(true); // 是否一级菜单，控制Editor中name字段长度
const activeMenu = ref<Menu>({}); // 选中菜单，Editor的modelValue

// 一些临时值放在这里进行判断，如果放在 activeMenu，由于引用关系，menu 也会多了多余的参数
const tempSelfObj = ref<{
  grand: Level;
  x: number;
  y: number;
}>({
  grand: Level.Undefined,
  x: 0,
  y: 0,
});
const dialogNewsVisible = ref(false); // 跳转图文时的素材选择弹窗

/** 侦听公众号变化 */
async function onAccountChanged(values: Record<string, any>) {
  accountId.value = values.accountId;
  // 从 API 获取公众号列表并查找对应的公众号名称
  const accountList = await getSimpleAccountList();
  const account = accountList.find((item) => item.id === values.accountId);
  accountName.value = account?.name || '';
  await getList();
}

/** 初始化账号ID - 作为备用方案，防止 handleValuesChange 未触发 */
async function initAccountId() {
  await nextTick(); // 等待表单初始化完成
  const values = await accountFormApi.getValues();
  if (values?.accountId && accountId.value === -1) {
    // 如果表单有值但 accountId 还是初始值，则手动触发一次
    await onAccountChanged(values);
  }
}

/** 组件挂载时初始化账号 ID */
onMounted(async () => {
  await nextTick();
  await initAccountId();
});

/** 查询并转换菜单 */
async function getList() {
  loading.value = true;
  try {
    const data = await getMenuList(accountId.value);
    const menuData = menuListToFrontend(data);
    menuList.value = handleTree(menuData, 'id') as Menu[];
  } finally {
    loading.value = false;
  }
}

/** 搜索按钮操作 */
function handleQuery() {
  resetForm();
  getList();
}

/** 将后端返回的 menuList，转换成前端的 menuList */
function menuListToFrontend(list: any[]) {
  if (!list) {
    return [];
  }

  const result: RawMenu[] = [];
  list.forEach((item: RawMenu) => {
    const menu: any = {
      ...item,
      reply: {
        type: item.replyMessageType,
        accountId: item.accountId,
        content: item.replyContent,
        mediaId: item.replyMediaId,
        url: item.replyMediaUrl,
        title: item.replyTitle,
        description: item.replyDescription,
        thumbMediaId: item.replyThumbMediaId,
        thumbMediaUrl: item.replyThumbMediaUrl,
        articles: item.replyArticles,
        musicUrl: item.replyMusicUrl,
        hqMusicUrl: item.replyHqMusicUrl,
      },
    };
    result.push(menu as RawMenu);
  });
  return result;
}

/** 重置表单，清空表单数据 */
function resetForm() {
  // 菜单操作
  activeIndex.value = MENU_NOT_SELECTED;
  parentIndex.value = -1;

  // 菜单编辑
  showRightPanel.value = false;
  activeMenu.value = {};
  tempSelfObj.value = { grand: Level.Undefined, x: 0, y: 0 };
  dialogNewsVisible.value = false;
}

// ======================== 菜单操作 ========================

/** 一级菜单点击事件 */
function menuClicked(parent: Menu, x: number) {
  // 右侧的表单相关
  showRightPanel.value = true; // 右边菜单
  activeMenu.value = parent; // 这个如果放在顶部，flag 会没有。因为重新赋值了。
  tempSelfObj.value.grand = Level.Parent; // 表示一级菜单
  tempSelfObj.value.x = x; // 表示一级菜单索引
  isParent.value = true;

  // 左侧的选中
  activeIndex.value = `${x}`; // 菜单选中样式
  parentIndex.value = x; // 二级菜单显示标志
}

/** 二级菜单点击事件 */
function subMenuClicked(child: Menu, x: number, y: number) {
  // 右侧的表单相关
  showRightPanel.value = true; // 右边菜单
  activeMenu.value = child; // 将点击的数据放到临时变量，对象有引用作用
  tempSelfObj.value.grand = Level.Child; // 表示二级菜单
  tempSelfObj.value.x = x; // 表示一级菜单索引
  tempSelfObj.value.y = y; // 表示二级菜单索引
  isParent.value = false;

  // 左侧的选中
  activeIndex.value = `${x}-${y}`;
}

/** 删除当前菜单 */
async function onDeleteMenu() {
  await confirm('确定要删除吗?');
  if (tempSelfObj.value.grand === Level.Parent) {
    // 一级菜单的删除方法
    menuList.value.splice(tempSelfObj.value.x, 1);
  } else if (tempSelfObj.value.grand === Level.Child) {
    // 二级菜单的删除方法
    menuList.value[tempSelfObj.value.x]?.children?.splice(
      tempSelfObj.value.y,
      1,
    );
  }
  // 提示
  message.success('删除成功');

  // 处理菜单的选中
  activeMenu.value = {};
  showRightPanel.value = false;
  activeIndex.value = MENU_NOT_SELECTED;
}

// ======================== 菜单编辑 ========================

/** 保存菜单 */
async function onSave() {
  await confirm('确定要保存吗?');
  const hideLoading = message.loading({
    content: '保存中...',
    duration: 0,
  });
  try {
    await saveMenu(accountId.value, menuListToBackend());
    await getList();
    message.success('发布成功');
  } finally {
    hideLoading();
  }
}

/** 清空菜单 */
async function onClear() {
  await confirm('确定要删除吗?');
  const hideLoading = message.loading({
    content: '删除中...',
    duration: 0,
  });
  try {
    await deleteMenu(accountId.value);
    handleQuery();
    message.success('清空成功');
  } finally {
    hideLoading();
  }
}

/** 将前端的 menuList，转换成后端接收的 menuList */
function menuListToBackend() {
  const result: any[] = [];
  menuList.value.forEach((item) => {
    const menu = menuToBackend(item);
    result.push(menu);

    // 处理子菜单
    if (!item.children || item.children.length <= 0) {
      return;
    }
    menu.children = [];
    item.children.forEach((subItem) => {
      menu.children.push(menuToBackend(subItem));
    });
  });
  return result;
}

/** 将前端的 menu，转换成后端接收的 menu */
// TODO: @芋艿，需要根据后台 API 删除不需要的字段
function menuToBackend(menu: any) {
  return {
    ...menu,
    children: undefined, // 不处理子节点
    reply: undefined, // 稍后复制
    replyMessageType: menu.reply.type,
    replyContent: menu.reply.content,
    replyMediaId: menu.reply.mediaId,
    replyMediaUrl: menu.reply.url,
    replyTitle: menu.reply.title,
    replyDescription: menu.reply.description,
    replyThumbMediaId: menu.reply.thumbMediaId,
    replyThumbMediaUrl: menu.reply.thumbMediaUrl,
    replyArticles: menu.reply.articles,
    replyMusicUrl: menu.reply.musicUrl,
    replyHqMusicUrl: menu.reply.hqMusicUrl,
  };
}
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="公众号菜单" url="https://doc.iocoder.cn/mp/menu/" />
    </template>

    <!-- 搜索工作栏 -->
    <AccountForm class="-mb-15px w-240px" @values-change="onAccountChanged" />

    <ContentWrap>
      <div
        class="mx-auto w-[1200px] after:clear-both after:table after:content-['']"
        v-loading="loading"
      >
        <!--左边配置菜单-->
        <div
          class="relative float-left box-border block h-[715px] w-[350px] bg-[length:100%_auto] bg-no-repeat p-[518px_25px_88px]"
          :style="{ backgroundImage: `url(${iphoneBackImg})` }"
        >
          <div
            class="relative bottom-[426px] left-0 h-[64px] w-[300px] bg-[length:100%] bg-[position:0_0] bg-no-repeat text-center text-white"
            :style="{ backgroundImage: `url(${menuHeadImg})` }"
          >
            <div
              class="absolute left-0 top-[33px] w-full text-center text-sm text-white"
            >
              {{ accountName }}
            </div>
          </div>
          <div
            class="bg-[position:0_0] bg-no-repeat pl-[43px] text-xs after:clear-both after:table after:content-['']"
            :style="{ backgroundImage: `url(${menuFootImg})` }"
          >
            <Previewer
              v-model="menuList"
              :account-id="accountId"
              :active-index="activeIndex"
              :parent-index="parentIndex"
              @menu-clicked="(parent, x) => menuClicked(parent, x)"
              @submenu-clicked="(child, x, y) => subMenuClicked(child, x, y)"
            />
          </div>
          <div class="mt-[15px] flex items-center justify-center gap-[10px]">
            <Button
              type="primary"
              @click="onSave"
              v-access:code="['mp:menu:save']"
            >
              保存并发布菜单
            </Button>
            <Button danger @click="onClear" v-access:code="['mp:menu:delete']">
              清空菜单
            </Button>
          </div>
        </div>
        <!--右边配置-->
        <div
          class="float-left ml-5 box-border w-[63%] bg-[#e8e7e7] p-5"
          v-if="showRightPanel"
        >
          <Editor
            :account-id="accountId"
            :is-parent="isParent"
            v-model="activeMenu"
            @delete="onDeleteMenu"
          />
        </div>
        <!-- 一进页面就显示的默认页面，当点击左边按钮的时候，就不显示了-->
        <div v-else class="float-left ml-5 box-border w-[63%] bg-[#e8e7e7] p-5">
          <p>请选择菜单配置</p>
        </div>
      </div>
    </ContentWrap>
  </Page>
</template>
