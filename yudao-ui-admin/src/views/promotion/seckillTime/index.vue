<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <!-- <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="秒杀时段名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入秒杀时段名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>

      <el-form-item label="开始时间点" prop="startTime">
        <el-time-picker v-model="queryParams.startTime" placeholder="选择开始时间" value-format="HH:mm:ss" />
      </el-form-item>

      <el-form-item label="结束时间点" prop="endTime">
        <el-time-picker v-model="queryParams.endTime" placeholder="选择结束时间" value-format="HH:mm:ss" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form> -->

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['promotion:seckill-time:create']">新增</el-button>
      </el-col>
      <!-- <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
          :loading="exportLoading" v-hasPermi="['promotion:seckill-time:export']">导出</el-button>
      </el-col> -->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="秒杀时段名称" align="center" prop="name" />
      <el-table-column label="开始时间点" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.startTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间点" align="center" prop="endTime" width="180">
        <template slot-scope="scope">
          <span>{{ scope.row.endTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="秒杀活动数量" align="center" prop="seckillActivityCount" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['promotion:seckill-time:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['promotion:seckill-time:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <!-- <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
      @pagination="getList" /> -->

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
import { createSeckillTime, updateSeckillTime, deleteSeckillTime, getSeckillTime, getSeckillTimePage, exportSeckillTimeExcel, getSeckillTimeList } from "@/api/promotion/seckillTime";
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
      // 查询参数
      // queryParams: {
      //   pageNo: 1,
      //   pageSize: 10,
      //   name: null,
      //   startTime: null,
      //   endTime: null,
      // },
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
        console.log(response, "返回的数据")
        this.list = response.data;
        // this.total = response.data.total;
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
      // this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      console.log(this.form, "点击新增时的form");
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
    /** 导出按钮操作 */
    // handleExport() {
    //   // 处理查询参数
    //   let params = { ...this.queryParams };
    //   params.pageNo = undefined;
    //   params.pageSize = undefined;
    //   this.$modal.confirm('是否确认导出所有秒杀时段数据项?').then(() => {
    //     this.exportLoading = true;
    //     return exportSeckillTimeExcel(params);
    //   }).then(response => {
    //     this.$download.excel(response, '秒杀时段.xls');
    //     this.exportLoading = false;
    //   }).catch(() => { });
    // }
  }
};
</script>
