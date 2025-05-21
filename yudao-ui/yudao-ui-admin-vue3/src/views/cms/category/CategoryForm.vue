<template>
  <Dialog :title="formTitle" v-model="dialogVisible" width="500px">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
    >
      <el-form-item label="Category Name" prop="name">
        <el-input v-model="formData.name" placeholder="Enter category name" />
      </el-form-item>
      <el-form-item label="Category Slug" prop="slug">
        <el-input v-model="formData.slug" placeholder="Enter category slug" />
      </el-form-item>
      <el-form-item label="Parent Category" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          check-strictly
          filterable
          clearable
          placeholder="Select parent category (optional)"
          class="w-full"
        />
        <!-- Fallback to el-select if el-tree-select is not desired/available initially
        <el-select v-model="formData.parentId" placeholder="Select parent category (optional)" clearable class="w-full">
          <el-option label="Root Category" :value="0" /> // Or null, depends on backend handling
          <el-option
            v-for="item in parentCategoryOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        -->
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input type="textarea" v-model="formData.description" placeholder="Enter description" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="submitForm" type="primary" :disabled="formLoading">Submit</el-button>
      <el-button @click="dialogVisible = false">Cancel</el-button>
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { createCategory, updateCategory, getCategory, getSimpleCategoryList, type CategoryVO } from '@/api/cms/category'
import { handleTree } from '@/utils/tree' // Assuming a tree utility exists

defineOptions({ name: 'CmsCategoryForm' })

const dialogVisible = ref(false)
const formLoading = ref(false)
const formTitle = ref('')
const formType = ref('') // 'create' or 'update'

const formData = reactive<Partial<CategoryVO>>({
  id: undefined,
  name: '',
  slug: '',
  parentId: undefined, // Or null / 0 for root
  description: ''
})

const formRules = reactive({
  name: [{ required: true, message: 'Category name cannot be empty', trigger: 'blur' }],
  slug: [{ required: true, message: 'Category slug cannot be empty', trigger: 'blur' }]
  // Add more rules, e.g., pattern for slug
})

const formRef = ref() // Reference to the form

const categoryTree = ref<any[]>([]) // For el-tree-select
// const parentCategoryOptions = ref<CategoryVO[]>([]) // For el-select fallback

/** Load parent category options (simple list or tree) */
const loadParentCategories = async () => {
  try {
    const data = await getSimpleCategoryList() // Assuming this returns a flat list
    // For el-tree-select, transform flat list to tree if not already tree-structured
    // If the backend can return a tree or if parentId is sufficient to build tree on frontend:
    // Add a "Root" or "None" option for top-level categories
    const rootOption = { id: 0, name: 'Root Category', children: [] } // Or use null for parentId
    categoryTree.value = [rootOption, ...handleTree(data, 'id', 'parentId')] // Adjust handleTree if needed
    
    // For el-select fallback:
    // parentCategoryOptions.value = data
  } catch (error) {
    ElMessage.error('Failed to load parent categories')
  }
}

/** Open the dialog */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  formTitle.value = type === 'create' ? 'Add Category' : 'Edit Category'
  formType.value = type
  resetForm()
  await loadParentCategories() // Load options when dialog opens

  if (id) {
    formLoading.value = true
    try {
      const data = await getCategory(id)
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
  formData.parentId = undefined // Or null / 0
  formData.description = ''
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
        const dataToSubmit: CategoryVO = { ...formData } as CategoryVO
        if (formType.value === 'create') {
          await createCategory(dataToSubmit)
          ElMessage.success('Category added successfully')
        } else {
          await updateCategory(dataToSubmit)
          ElMessage.success('Category updated successfully')
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
