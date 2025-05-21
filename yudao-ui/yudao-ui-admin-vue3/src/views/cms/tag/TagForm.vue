<template>
  <Dialog :title="formTitle" v-model="dialogVisible" width="500px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      v-loading="formLoading"
    >
      <el-form-item label="Tag Name" prop="name">
        <el-input v-model="formData.name" placeholder="Enter tag name" />
      </el-form-item>
      <el-form-item label="Tag Slug" prop="slug">
        <el-input v-model="formData.slug" placeholder="Enter tag slug" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">Submit</el-button>
      <el-button @click="dialogVisible = false">Cancel</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { createTag, updateTag, getTag, type TagVO } from '@/api/cms/tag'

defineOptions({ name: 'CmsTagForm' })

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const formType = ref('') // 'create' or 'update'

const formData = reactive<Partial<TagVO>>({
  id: undefined,
  name: '',
  slug: ''
})

const formRules = reactive({
  name: [{ required: true, message: 'Tag name cannot be empty', trigger: 'blur' }],
  slug: [{ required: true, message: 'Tag slug cannot be empty', trigger: 'blur' }]
  // Add pattern validation for slug if needed
})

const formRef = ref() // Reference to the form

/** Open the dialog */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  formTitle.value = type === 'create' ? 'Add Tag' : 'Edit Tag'
  formType.value = type
  resetForm()

  if (id) {
    formLoading.value = true
    try {
      const data = await getTag(id)
      Object.assign(formData, data)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open }) // Expose open method

/** Reset form data */
const resetForm = () => {
  formData.id = undefined
  formData.name = ''
  formData.slug = ''
  formRef.value?.resetFields()
}

const emit = defineEmits(['success']) // Emit event on success

/** Submit form */
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      formLoading.value = true
      try {
        const dataToSubmit: TagVO = { ...formData } as TagVO // Ensure all required fields are present
        if (formType.value === 'create') {
          await createTag(dataToSubmit)
          ElMessage.success('Tag added successfully')
        } else {
          await updateTag(dataToSubmit)
          ElMessage.success('Tag updated successfully')
        }
        dialogVisible.value = false
        emit('success')
      } finally {
        formLoading.value = false
      }
    }
  })
}
</script>
