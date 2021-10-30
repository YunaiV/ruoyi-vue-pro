<template>
  <div class="app-container">
    <!-- 对话框(添加 / 修改) -->
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker clearable size="small" v-model="form.startTime" type="date" value-format="timestamp" placeholder="选择开始时间" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker clearable size="small" v-model="form.endTime" type="date" value-format="timestamp" placeholder="选择结束时间" />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="form.leaveType" placeholder="请选择">
            <el-option
              v-for="dict in leaveTypeDictData"
              :key="parseInt(dict.value)"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-col :span="10">
            <el-input
              type="textarea"
              :rows="3"
              v-model="form.reason"
              placeholder="请输入原因" />
          </el-col>
        </el-form-item>
        <el-form-item label="申请时间" prop="applyTime">
          <el-date-picker clearable size="small" v-model="form.applyTime" type="date" value-format="timestamp" placeholder="选择申请时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </el-form-item>
      </el-form>
  </div>
</template>

<script>
import { createFormKeyLeave } from "@/api/oa/leave"
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'
export default {
  name: "ApplyLeave",
  components: {
  },
  data() {
    return {
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        startTime: [{ required: true, message: "开始时间不能为空", trigger: "blur" }],
        endTime: [{ required: true, message: "结束时间不能为空", trigger: "blur" }],
        applyTime: [{ required: true, message: "申请时间不能为空", trigger: "blur" }],
      },
      statusFormat(row, column) {
        return getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, row.status)
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.OA_LEAVE_TYPE),
      leaveStatusData: getDictDatas(DICT_TYPE.OA_LEAVE_STATUS)
    };
  },
  created() {
  },
  methods: {
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        // if (this.form.id != null) {
        //   updateLeave(this.form).then(response => {
        //     this.msgSuccess("修改成功");
        //   });
        //   return;
        // }
        // 添加的提交
        createFormKeyLeave(this.form).then(response => {
          this.msgSuccess("新增成功");
          this.$store.dispatch('tagsView/delView', this.$route).then(({ visitedViews }) => {
            //if (this.isActive(this.$route)) {
            this.$router.push({path: '/oa/todo'})
            //}
          })
        });
      });
    }
  }
};
</script>
