<template>
  <div class="app-container">
    <el-form ref="createForm" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="开始时间" prop="startTime">
        <el-date-picker clearable v-model="form.startTime" type="date" placeholder="选择开始时间" />
      </el-form-item>
      <el-form-item label="结束时间" prop="endTime">
        <el-date-picker clearable v-model="form.endTime" type="date" placeholder="选择结束时间" />
      </el-form-item>
      <el-form-item label="请假类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择">
          <el-option
            v-for="dict in typeDictData"
            :key="parseInt(dict.value)"
            :label="dict.label"
            :value="parseInt(dict.value)"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="原因" prop="reason">
        <el-input
          style="width: 650px"
          type="textarea"
          :rows="3"
          v-model="form.reason"
          placeholder="请输入原因"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm(createForm)">提 交</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script setup lang="ts">
import { ref, reactive } from 'vue'
import {
  ElInput,
  ElSelect,
  ElOption,
  ElForm,
  ElFormItem,
  ElMessage,
  ElDatePicker
} from 'element-plus'
import type { FormInstance } from 'element-plus'
import { createLeaveApi } from '@/api/bpm/leave'
import { getDictOptions, DICT_TYPE } from '@/utils/dict'
import { useRouter } from 'vue-router'

const { push } = useRouter()
const createForm = ref()
// 表单参数
const form = ref({
  startTime: undefined,
  endTime: undefined,
  type: undefined,
  reason: undefined
})
// 表单校验
const rules = reactive({
  startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
  endTime: [{ required: true, message: '结束时间不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '请假类型不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '请假原因不能为空', trigger: 'change' }]
})
const typeDictData = getDictOptions(DICT_TYPE.BPM_OA_LEAVE_TYPE)

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      console.log(form.value, 'submit!')
      form.value.startTime = Date.parse(form.value.startTime)
      form.value.endTime = Date.parse(form.value.endTime)
      // 添加的提交
      createLeaveApi(form.value).then((response) => {
        console.log(response, 'response')
        ElMessage({
          type: 'success',
          message: '发起成功'
        })
        push('/bpm/oa/leave')
      })
    } else {
      console.log('error submit!', fields)
    }
  })
}
</script>
