<template>
  <div class="app-container">

    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志编号" align="center" prop="id" />
      <el-table-column label="操作模块" align="center" prop="module" />
      <el-table-column label="操作名" align="center" prop="name" width="180" />
      <el-table-column label="操作类型" align="center" prop="type">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="userNickname" />
      <el-table-column label="操作结果" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ scope.row.resultCode === 0 ? '成功' : '失败' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作日期" align="center" prop="startTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时长" align="center" prop="startTime">
        <template slot-scope="scope">
          <span>{{ scope.row.duration }}  ms</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row,scope.index)"
                     v-hasPermi="['system:operate-log:query']">详细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList" />

    <!-- 操作日志详细 -->
    <el-dialog title="访问日志详细" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志主键：">{{ form.id }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="链路追踪：">{{ form.traceId }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="用户信息：">{{ form.userId }} | {{ form.userNickname }} | {{ form.userIp }} | {{ form.userAgent}} </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="操作信息：">
              {{ form.module }} | {{ form.name }}
              <dict-tag :type="DICT_TYPE.SYSTEM_OPERATE_TYPE" :value="form.type"/>
              <br /> {{ form.content }}
              <br /> {{ form.exts }}
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="请求信息：">{{ form.requestMethod }} | {{ form.requestUrl }} </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方法名：">{{ form.javaMethod }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="方法参数：">{{ form.javaMethodArgs }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="开始时间：">
              {{ parseTime(form.startTime) }} | {{ form.duration }} ms
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作结果：">
              <div v-if="form.resultCode === 0">正常 | {{ form.resultData}} </div>
              <div v-else-if="form.resultCode > 0">失败 | {{ form.resultCode }} || {{ form.resultMsg}}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOperateLog, exportOperateLog } from "@/api/system/operatelog";

export default {
  name: "Operlog",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 是否显示弹出层
      open: false,
      // 类型数据字典
      typeOptions: [],
      // 类型数据字典
      statusOptions: [],
      // 日期范围
      dateRange: [],
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
        businessType: undefined,
        status: undefined
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志 */
    getList() {
      this.loading = true;
      listOperateLog(this.addDateRange(this.queryParams, [
        this.dateRange[0] ? this.dateRange[0] + ' 00:00:00' : undefined,
        this.dateRange[1] ? this.dateRange[1] + ' 23:59:59' : undefined,
      ])).then( response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.addDateRange(this.queryParams, [
        this.dateRange[0] ? this.dateRange[0] + ' 00:00:00' : undefined,
        this.dateRange[1] ? this.dateRange[1] + ' 23:59:59' : undefined,
      ])
      this.$modal.confirm('是否确认导出所有操作日志数据项?').then(() => {
          this.exportLoading = true;
          return exportOperateLog(queryParams);
        }).then(response => {
          this.$download.excel(response, '操作日志.xls');
          this.exportLoading = false;
      }).catch(() => {});
    }
  }
};
</script>

