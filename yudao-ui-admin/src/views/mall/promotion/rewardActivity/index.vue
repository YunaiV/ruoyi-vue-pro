<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="活动名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入活动名称" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="活动状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择活动状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_ACTIVITY_STATUS)"
                       :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['promotion:reward-activity:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="活动名称" align="center" prop="name" />
      <el-table-column label="活动时间" align="center" prop="startTime" width="240">
        <template slot-scope="scope">
          <div>开始：{{ parseTime(scope.row.startTime) }}</div>
          <div>结束：{{ parseTime(scope.row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.PROMOTION_ACTIVITY_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-if="scope.row.status !== PromotionActivityStatusEnum.CLOSE.type"
                     v-hasPermi="['promotion:reward-activity:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleClose(scope.row)"
                     v-if="scope.row.status !== PromotionActivityStatusEnum.CLOSE.type &&
                            scope.row.status !== PromotionActivityStatusEnum.END.type"
                     v-hasPermi="['promotion:reward-activity:close']">关闭</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-if="scope.row.status === PromotionActivityStatusEnum.CLOSE.type"
                     v-hasPermi="['promotion:reward-activity:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="600px" v-dialogDrag append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动时间" prop="startAndEndTime">
          <el-date-picker clearable v-model="form.startAndEndTime" type="datetimerange" :default-time="['00:00:00', '23:59:59']"
                          value-format="timestamp" placeholder="选择开始时间" style="width: 480px" />
        </el-form-item>
        <el-form-item label="条件类型" prop="conditionType">
          <el-radio-group v-model="form.conditionType">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_CONDITION_TYPE)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠设置" prop="conditionType">
          <!-- TODO 芋艿：待实现！ -->
        </el-form-item>
        <el-form-item label="活动商品" prop="productScope">
          <el-radio-group v-model="form.productScope">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_PRODUCT_SCOPE)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.productScope === PromotionProductScopeEnum.SPU.scope" prop="productSpuIds">
          <el-select v-model="form.productSpuIds" placeholder="请选择活动商品" clearable size="small"
                     multiple filterable style="width: 400px">
            <el-option v-for="item in productSpus" :key="item.id" :label="item.name" :value="item.id">
              <span style="float: left">{{ item.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">￥{{ (item.minPrice / 100.0).toFixed(2) }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
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
import {
  createRewardActivity,
  updateRewardActivity,
  deleteRewardActivity,
  getRewardActivity,
  getRewardActivityPage,
  closeRewardActivity
} from "@/api/mall/promotion/rewardActivity";
import {
  PromotionConditionTypeEnum,
  PromotionProductScopeEnum,
  PromotionActivityStatusEnum
} from "@/utils/constants";
import {getSpuSimpleList} from "@/api/mall/product/spu";

export default {
  name: "RewardActivity",
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
      total: 0,
      // 满减送活动列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        name: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        name: [{ required: true, message: "活动名称不能为空", trigger: "blur" }],
        startAndEndTime: [{ required: true, message: "活动时间不能为空", trigger: "blur" }],
        conditionType: [{ required: true, message: "条件类型不能为空", trigger: "change" }],
        productScope: [{ required: true, message: "商品范围不能为空", trigger: "blur" }],
        productSpuIds: [{ required: true, message: "商品范围不能为空", trigger: "blur" }],
      },
      // 商品列表
      productSpus: [],
      // 如下的变量，主要为了 v-if 判断可以使用到
      PromotionProductScopeEnum: PromotionProductScopeEnum,
      PromotionActivityStatusEnum: PromotionActivityStatusEnum,
    };
  },
  created() {
    this.getList();
    // 查询商品列表
    getSpuSimpleList().then(response => {
      this.productSpus = response.data
    })
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getRewardActivityPage(this.queryParams).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
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
        conditionType: PromotionConditionTypeEnum.PRICE.type,
        remark: undefined,
        productScope: PromotionProductScopeEnum.ALL.scope,
        productSpuIds: undefined,
        rules: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
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
      this.open = true;
      this.title = "添加满减送活动";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getRewardActivity(id).then(response => {
        this.form = response.data;
        this.form.startAndEndTime = [response.data.startTime, response.data.endTime];
        this.open = true;
        this.title = "修改满减送活动";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        this.form.startTime = this.form.startAndEndTime[0];
        this.form.endTime = this.form.startAndEndTime[1];
        // TODO 芋艿：临时实现
        this.form.rules = [
          {
            limit: 1,
            discountPrice: 10,
            freeDelivery: true,
            point: 10,
            couponIds: [10, 20],
            couponCounts: [1, 2]
          }, {
            limit: 2,
            discountPrice: 20,
            freeDelivery: false,
            point: 20,
            couponIds: [30, 40],
            couponCounts: [3, 4]
          }
        ];
        // 修改的提交
        if (this.form.id != null) {
          updateRewardActivity(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createRewardActivity(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除满减送活动编号为"' + id + '"的数据项?').then(function() {
          return deleteRewardActivity(id);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => {});
    },
    /** 关闭按钮操作 */
    handleClose(row) {
      const id = row.id;
      this.$modal.confirm('是否确认关闭满减送活动编号为"' + id + '"的数据项?').then(function() {
        return closeRewardActivity(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("关闭成功");
      }).catch(() => {});
    }
  }
};
</script>
