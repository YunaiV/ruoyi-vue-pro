<template>
  <div class="app-container">
    <doc-alert title="地区 & IP" url="https://doc.iocoder.cn/area-and-ip/" />
    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">IP 查询
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-if="refreshTable" v-loading="loading" :data="list"  row-key="id"
              :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column label="编号" prop="id"/>
      <el-table-column label="名字" prop="name"/>
    </el-table>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog title="IP 查询" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="IP" prop="ip">
          <el-input v-model="form.ip" placeholder="请输入 IP 地址"/>
        </el-form-item>
        <el-form-item label="地址" prop="result">
          <el-input v-model="form.result" readonly placeholder="展示查询 IP 结果" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">查 询</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {getAreaByIp, getAreaTree} from "@/api/system/area";

export default {
  name: "Area",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 地区列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 重新渲染表格状态
      refreshTable: true,
      // 表单参数
      form: {
        ip: undefined,
        result: undefined,
      },
      // 表单校验
      rules: {
        ip: [{required: true, message: "IP 地址不能为控", trigger: "blur"}],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      getAreaTree().then(response => {
        this.list = response.data;
        this.loading = false;
      })
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        ip: undefined,
        result: undefined,
      };
      this.resetForm("form");
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        getAreaByIp(this.form.ip).then(response => {
          this.$modal.msgSuccess("查询成功");
          this.form.result = response.data
        });
      });
    }
  }
};
</script>
