<script lang="ts" setup>
import type { Reply } from './types';

import { computed, ref } from 'vue';

import { NewsType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button, Col, Modal, Row } from 'ant-design-vue';

import { WxMaterialSelect, WxNews } from '#/views/mp/components';

defineOptions({ name: 'TabNews' });

const props = defineProps<{
  modelValue: Reply;
  newsType: NewsType;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: Reply): void;
}>();

const reply = computed<Reply>({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
});

const showDialog = ref(false);

/** 选择素材 */
function selectMaterial(item: any) {
  showDialog.value = false;
  reply.value.articles = item.content.newsItem;
}

/** 删除图文 */
function onDelete() {
  reply.value.articles = [];
}
</script>

<template>
  <div>
    <Row>
      <div
        v-if="reply.articles && reply.articles.length > 0"
        class="mx-auto mb-[10px] w-[280px] border border-[#eaeaea] p-[10px]"
      >
        <WxNews :articles="reply.articles" />
        <Col class="pt-[10px] text-center">
          <Button danger shape="circle" @click="onDelete">
            <template #icon>
              <IconifyIcon icon="lucide:trash-2" />
            </template>
          </Button>
        </Col>
      </div>

      <!-- 选择素材 -->
      <Col v-if="!reply.content" :span="24">
        <Row class="text-center" align="middle">
          <Col :span="24">
            <Button type="primary" @click="showDialog = true">
              {{
                newsType === NewsType.Published
                  ? '选择已发布图文'
                  : '选择草稿箱图文'
              }}
              <template #icon>
                <IconifyIcon icon="lucide:circle-check" />
              </template>
            </Button>
          </Col>
        </Row>
      </Col>
      <Modal
        v-model:open="showDialog"
        title="选择图文"
        :width="1200"
        :footer="null"
        destroy-on-close
      >
        <WxMaterialSelect
          type="news"
          :account-id="reply.accountId"
          :news-type="newsType"
          @select-material="selectMaterial"
        />
      </Modal>
    </Row>
  </div>
</template>
