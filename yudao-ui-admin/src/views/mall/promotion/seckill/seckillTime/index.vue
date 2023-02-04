<template>
  <div class="app-container">
    <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['promotion:seckill-time:create']">新增秒杀时段</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="秒杀时段名称" align="center" prop="name" />
      <el-table-column label="开始时间点" align="center" prop="startTime" width="180">
        <template v-slot="scope">
          <span>{{ scope.row.startTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间点" align="center" prop="endTime" width="180">
        <template v-slot="scope">
          <span>{{ scope.row.endTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="秒杀活动数量" align="center" prop="seckillActivityCount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleOpenSeckillActivity(scope.row)">
            查看秒杀活动</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['promotion:seckill-time:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['promotion:seckill-time:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="600px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="140px">
        <el-form-item label="秒杀场次名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入秒杀时段名称" clearable />
        </el-form-item>
        <el-form-item label="秒杀时间段" prop="startAndEndTime">
          <el-time-picker is-range v-model="form.startAndEndTime" range-separator="至" start-placeholder="开始时间"
            end-placeholder="结束时间" placeholder="选择时间范围" value-format="HH:mm:ss">
          </el-time-picker>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createSeckillTime, updateSeckillTime, deleteSeckillTime, getSeckillTime, getSeckillTimePage, exportSeckillTimeExcel, getSeckillTimeList } from "@/api/mall/promotion/seckillTime";
import router from "@/router";
import { deepClone } from "@/utils";

export default {
  name: "SeckillTime",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      // total: 0,
      // 秒杀时段列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "秒杀时段名称不能为空", trigger: "blur" }],
        startAndEndTime: [{ required: true, message: "秒杀时间段不能为空", trigger: "blur" }],
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
      // 执行查询
      getSeckillTimeList().then(response => {
        this.list = response.data;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        startAndEndTime: undefined,
        startTime: undefined,
        endTime: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /**查看当前秒杀时段的秒杀活动 */
    handleOpenSeckillActivity(row) {
      router.push({ name: 'SeckillActivity', params: { timeId: row.id } })
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加秒杀时段";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getSeckillTime(id).then(response => {
        response.data.startAndEndTime = [response.data.startTime, response.data.endTime]
        this.form = response.data;
        this.open = true;
        this.title = "修改秒杀时段";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        console.log(valid, "是否通过");
        if (!valid) {
          return;
        }
        // 处理数据
        const data = deepClone(this.form);
        data.startTime = this.form.startAndEndTime[0];
        data.endTime = this.form.startAndEndTime[1];
        // 修改的提交
        if (this.form.id != null) {
          updateSeckillTime(data).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createSeckillTime(data).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除秒杀时段编号为"' + id + '"的数据项?').then(function () {
        return deleteSeckillTime(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => { });
    },
  }
};
</script>
