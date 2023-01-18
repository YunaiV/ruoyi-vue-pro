<template>
  <div class="user-info-head" @click="open()">
    <img :src="sourceValue" v-if="sourceValue" class="img-circle img-lg" alt="avatar" />
    <el-button :class="`${prefixCls}-upload-btn`" @click="open()" v-if="showBtn">
      {{ btnText ? btnText : t('cropper.selectImage') }}
    </el-button>
    <CopperModal
      ref="cropperModelRef"
      @upload-success="handleUploadSuccess"
      :srcValue="sourceValue"
    />
  </div>
</template>
<script setup lang="ts">
import { useDesign } from '@/hooks/web/useDesign'

import { propTypes } from '@/utils/propTypes'
import { useI18n } from 'vue-i18n'
import CopperModal from './CopperModal.vue'

const props = defineProps({
  width: propTypes.string.def('200px'),
  value: propTypes.string.def(''),
  showBtn: propTypes.bool.def(true),
  btnText: propTypes.string.def('')
})

const emit = defineEmits(['update:value', 'change'])
const sourceValue = ref(props.value)
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('cropper-avatar')
const message = useMessage()
const { t } = useI18n()

const cropperModelRef = ref()

watchEffect(() => {
  sourceValue.value = props.value
})

watch(
  () => sourceValue.value,
  (v: string) => {
    emit('update:value', v)
  }
)

function handleUploadSuccess({ source, data, filename }) {
  sourceValue.value = source
  emit('change', { source, data, filename })
  message.success(t('cropper.uploadSuccess'))
}

function open() {
  cropperModelRef.value.openModal()
}
function close() {
  cropperModelRef.value.closeModal()
}
defineExpose({
  open,
  close
})
</script>
<style lang="scss" scoped>
$prefix-cls: #{$namespace}--cropper-avatar;

.#{$prefix-cls} {
  display: inline-block;
  text-align: center;

  &-image-wrapper {
    overflow: hidden;
    cursor: pointer;
    border: 1px solid;
    border-radius: 50%;

    img {
      width: 100%;
    }
  }

  &-image-mask {
    opacity: 0%;
    position: absolute;
    width: inherit;
    height: inherit;
    border-radius: inherit;
    border: inherit;
    background: rgb(0 0 0 / 40%);
    cursor: pointer;
    transition: opacity 0.4s;

    ::v-deep(svg) {
      margin: auto;
    }
  }

  &-image-mask:hover {
    opacity: 4000%;
  }

  &-upload-btn {
    margin: 10px auto;
  }
}
.user-info-head {
  position: relative;
  display: inline-block;
}
.img-circle {
  border-radius: 50%;
}
.img-lg {
  width: 120px;
  height: 120px;
}
.user-info-head:hover:after {
  content: '+';
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  color: #eee;
  background: rgba(0, 0, 0, 0.5);
  font-size: 24px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  cursor: pointer;
  line-height: 110px;
  border-radius: 50%;
}
</style>
