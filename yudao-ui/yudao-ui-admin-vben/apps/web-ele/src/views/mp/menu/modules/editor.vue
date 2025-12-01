<script lang="ts" setup>
import { computed, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCol,
  ElDialog,
  ElInput,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import { WxMaterialSelect, WxNews, WxReply } from '#/views/mp/components';

import { menuOptions } from './types';

const props = defineProps<{
  accountId: number;
  isParent: boolean;
  modelValue: any;
}>();

const emit = defineEmits<{
  (e: 'delete', v: void): void;
  (e: 'update:modelValue', v: any): void;
}>();

const menu = computed({
  get() {
    return props.modelValue;
  },
  set(val) {
    emit('update:modelValue', val);
  },
});
const showNewsDialog = ref(false);
const isLeave = computed<boolean>(() => !(menu.value.children?.length > 0));

// ======================== 菜单编辑（素材选择） ========================

/** 选择素材 */
function selectMaterial(item: any) {
  const articleId = item.articleId;
  const articles = item.content.newsItem;
  // 提示，针对多图文
  if (articles.length > 1) {
    ElMessage.warning('您选择的是多图文，将默认跳转第一篇');
  }
  showNewsDialog.value = false;

  // 设置菜单的回复
  menu.value.articleId = articleId;
  menu.value.replyArticles = [];
  articles.forEach((article: any) => {
    menu.value.replyArticles.push({
      title: article.title,
      description: article.digest,
      picUrl: article.picUrl,
      url: article.url,
    });
  });
}

/** 删除素材 */
function deleteMaterial() {
  delete menu.value.articleId;
  delete menu.value.replyArticles;
}
</script>

<template>
  <div class="space-y-5">
    <div class="flex justify-end">
      <ElButton type="danger" @click="emit('delete')">
        <IconifyIcon icon="lucide:trash-2" />
        删除当前菜单
      </ElButton>
    </div>
    <div class="flex items-center gap-3">
      <span class="w-[100px] text-base">菜单名称：</span>
      <ElInput
        class="w-[40%] min-w-[220px]"
        v-model="menu.name"
        placeholder="请输入菜单名称"
        :maxlength="isParent ? 4 : 7"
        clearable
      />
    </div>
    <div v-if="isLeave" class="space-y-5">
      <div class="flex items-center gap-3">
        <span class="w-[100px] text-base">菜单标识：</span>
        <ElInput
          class="w-[40%] min-w-[220px]"
          v-model="menu.menuKey"
          placeholder="请输入菜单 KEY"
          clearable
        />
      </div>
      <div class="flex items-center gap-3">
        <span class="w-[100px] text-base">菜单内容：</span>
        <ElSelect
          v-model="menu.type"
          clearable
          placeholder="请选择"
          class="w-[40%] min-w-[220px]"
        >
          <ElOption
            v-for="item in menuOptions"
            :label="item.label"
            :value="item.value"
            :key="item.value"
          />
        </ElSelect>
      </div>
      <div
        class="rounded bg-white px-3 py-5 shadow-sm"
        v-if="menu.type === 'view'"
      >
        <div class="flex items-center gap-3">
          <span class="text-base">跳转链接：</span>
          <ElInput
            class="w-[40%] min-w-[220px]"
            v-model="menu.url"
            placeholder="请输入链接"
            clearable
          />
        </div>
      </div>
      <div
        class="space-y-5 rounded bg-white px-3 py-5 shadow-sm"
        v-if="menu.type === 'miniprogram'"
      >
        <div class="flex items-center gap-3">
          <span class="inline-block w-[25%] min-w-[120px] text-base">
            小程序的 appid ：
          </span>
          <ElInput
            class="w-[40%] min-w-[220px]"
            v-model="menu.miniProgramAppId"
            placeholder="请输入小程序的appid"
            clearable
          />
        </div>
        <div class="flex items-center gap-3">
          <span class="inline-block w-[25%] min-w-[120px] text-base">
            小程序的页面路径：
          </span>
          <ElInput
            class="w-[40%] min-w-[220px]"
            v-model="menu.miniProgramPagePath"
            placeholder="请输入小程序的页面路径，如：pages/index"
            clearable
          />
        </div>
        <div class="flex items-center gap-3">
          <span class="inline-block w-[25%] min-w-[120px] text-base">
            小程序的备用网页：
          </span>
          <ElInput
            class="w-[40%] min-w-[220px]"
            v-model="menu.url"
            placeholder="不支持小程序的老版本客户端将打开本网页"
            clearable
          />
        </div>
        <p class="text-sm text-[#29b6f6]">
          tips:需要和公众号进行关联才可以把小程序绑定带微信菜单上哟！
        </p>
      </div>
      <div
        class="rounded bg-white px-3 py-5 shadow-sm"
        v-if="menu.type === 'article_view_limited'"
      >
        <ElRow>
          <div
            class="mx-auto mb-2.5 w-[280px] border border-[#eaeaea] p-2.5"
            v-if="menu && menu.replyArticles"
          >
            <WxNews :articles="menu.replyArticles" />
            <ElRow class="pt-2.5 text-center" justify="center" align="middle">
              <ElButton type="danger" circle @click="deleteMaterial">
                <IconifyIcon icon="ep:trash-2" />
              </ElButton>
            </ElRow>
          </div>
          <div v-else class="w-full">
            <ElRow justify="center">
              <ElCol :span="24" class="text-center">
                <ElButton type="success" @click="showNewsDialog = true">
                  素材库选择
                  <IconifyIcon icon="lucide:circle-check" />
                </ElButton>
              </ElCol>
            </ElRow>
          </div>
          <ElDialog
            title="选择图文"
            v-model="showNewsDialog"
            width="80%"
            destroy-on-close
          >
            <WxMaterialSelect
              type="news"
              :account-id="props.accountId"
              @select-material="selectMaterial"
            />
          </ElDialog>
        </ElRow>
      </div>
      <div
        class="rounded bg-white px-3 py-5 shadow-sm"
        v-if="menu.type === 'click' || menu.type === 'scancode_waitmsg'"
      >
        <WxReply v-model="menu.reply" />
      </div>
    </div>
  </div>
</template>
