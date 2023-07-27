<template>
  <div>
    <el-dialog :visible.sync="dialogVisible" :title="title" @closed="close" append-to-body width="800px">
      <el-form ref="form" :model="formData" :rules="rules" size="medium" label-width="100px" v-loading="formLoading">
        <el-form-item label-width="180px" label="渠道状态" prop="status">
          <el-radio-group v-model="formData.status" size="medium">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.COMMON_STATUS)" :key="parseInt(dict.value)"
                      :label="parseInt(dict.value)">
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label-width="180px" label="备注" prop="remark">
          <el-input v-model="formData.remark" :style="{width: '100%'}"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { createChannel, getChannel, updateChannel } from "@/api/pay/channel";
import { CommonStatusEnum } from "@/utils/constants";

export default {
  name: "mockChannelForm",
  data() {
    return {
      dialogVisible: false,
      formLoading: false,
      title:'',
      formData: {
        appId: '',
        code: '',
        status: undefined,
        feeRate: 0,
        remark: '',
        config: {
          name: 'mock-conf'
        }
      },
      rules: {
        status: [{ required: true,  message: '渠道状态不能为空',  trigger: 'blur' }]
      }
    }
  },
  methods: {
    open(appId, code) {
      this.dialogVisible = true;
      this.formLoading = true;
      this.reset(appId, code);
      getChannel(appId, code).then(response => {
        if (response.data && response.data.id) {
          this.formData = response.data;
          this.formData.config = JSON.parse(response.data.config);
        }
        this.title = !this.formData.id ? '创建支付渠道' : '编辑支付渠道'
      }).finally(() => {
        this.formLoading = false;
      });
    },
    close() {
      this.dialogVisible = false;
      this.reset(undefined, undefined);
    },
    submitForm() {
      this.$refs['form'].validate(valid => {
        if (!valid) {
          return
        }
        const data = { ...this.formData };
        data.config = JSON.stringify(this.formData.config);
        if (!data.id) {
          createChannel(data).then(response => {
            this.$modal.msgSuccess("新增成功");
            this.$emit('success')
            this.close();
          });
        } else {
          updateChannel(data).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.$emit('success')
            this.close();
          })
        }
      });
    },
    /** 重置表单 */
    reset(appId, code) {
      this.formData = {
        appId: appId,
        code: code,
        status: CommonStatusEnum.ENABLE,
        remark: '',
        feeRate: 0,
        config: {
          name: 'mock-conf'
        }
      }
      this.resetForm('form')
    },
  }
}
</script>
