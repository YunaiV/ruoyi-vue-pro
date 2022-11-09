<script setup lang="ts">
import pageError from '@/assets/svgs/404.svg'
import networkError from '@/assets/svgs/500.svg'
import noPermission from '@/assets/svgs/403.svg'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
import { defineEmits } from 'vue'

interface ErrorMap {
  url: string
  message: string
  buttonText: string
}

const { t } = useI18n()

const errorMap: {
  [key: string]: ErrorMap
} = {
  '404': {
    url: pageError,
    message: t('error.pageError'),
    buttonText: t('error.returnToHome')
  },
  '500': {
    url: networkError,
    message: t('error.networkError'),
    buttonText: t('error.returnToHome')
  },
  '403': {
    url: noPermission,
    message: t('error.noPermission'),
    buttonText: t('error.returnToHome')
  }
}

const props = defineProps({
  type: propTypes.string.validate((v: string) => ['404', '500', '403'].includes(v)).def('404')
})

const emit = defineEmits(['errorClick'])

const btnClick = () => {
  emit('errorClick', props.type)
}
</script>

<template>
  <div class="flex justify-center">
    <div class="text-center">
      <img width="350" :src="errorMap[type].url" alt="" />
      <div class="text-14px text-[var(--el-color-info)]">{{ errorMap[type].message }}</div>
      <div class="mt-20px">
        <ElButton type="primary" @click="btnClick">{{ errorMap[type].buttonText }}</ElButton>
      </div>
    </div>
  </div>
</template>
