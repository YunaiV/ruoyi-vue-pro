<template>
  <div class="change-avatar">
    <CropperAvatar
      ref="cropperRef"
      :value="avatar"
      :showBtn="false"
      @change="handelUpload"
      :btnProps="{ preIcon: 'ant-design:cloud-upload-outlined' }"
      width="120px"
    />
  </div>
</template>
<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { uploadAvatarApi } from '@/api/system/user/profile'
const props = defineProps({
  img: propTypes.string.def('')
})
const avatar = computed(() => {
  return props.img
})

const cropperRef = ref()
const handelUpload = async ({ data }) => {
  await uploadAvatarApi({ avatarFile: data })
  cropperRef.value.close()
}
</script>

<style scoped lang="scss">
.change-avatar {
  img {
    display: block;
    margin-bottom: 15px;
    border-radius: 50%;
  }
}
</style>
