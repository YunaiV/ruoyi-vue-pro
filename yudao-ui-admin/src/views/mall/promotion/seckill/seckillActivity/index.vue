<template>
    <div class="app-container">
      <doc-alert title="功能开启" url="https://doc.iocoder.cn/mall/build/" />

        <!-- 搜索工作栏 -->
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch"
            label-width="68px">
            <el-form-item label="活动名称" prop="name">
                <el-input v-model="queryParams.name" placeholder="请输入秒杀活动名称" clearable
                    @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="活动状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="请选择活动状态" clearable size="small">
                    <el-option v-for="dict in this.getDictDatas(DICT_TYPE.PROMOTION_ACTIVITY_STATUS)" :key="dict.value"
                        :label="dict.label" :value="dict.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="参与场次" prop="timeId">
                <el-select v-model="queryParams.timeId" placeholder="请选择参与场次" clearable size="small">
                    <el-option v-for="item in seckillTimeList" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="创建时间" prop="createTime">
                <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss"
                    type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"
                    :default-time="['00:00:00', '23:59:59']" />
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
                    v-hasPermi="['promotion:seckill-activity:create']">新增秒杀活动</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="primary" plain icon="el-icon-menu" size="mini" @click="openSeckillTime"
                    v-hasPermi="['promotion:seckill-activity:create']">管理参与场次</el-button>
            </el-col>
            <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
        </el-row>

        <!-- 列表 -->
        <el-table v-loading="loading" :data="list">
            <el-table-column label="活动名称" align="center" prop="name" />
            <el-table-column label="活动状态" align="center" prop="status">
                <template v-slot="scope">
                    <dict-tag :type="DICT_TYPE.PROMOTION_ACTIVITY_STATUS" :value="scope.row.status" />
                </template>
            </el-table-column>
            <el-table-column label="参与场次" prop="timeIds" width="250">
                <template v-slot="scope">
                    <span v-for="item in seckillTimeList" :key="item.id"
                        v-if="scope.row.timeIds.includes(item.id)">
                        <el-tag style="margin:4px;" size="small">{{ item.name }}</el-tag>
                    </span>
                </template>
            </el-table-column>
            <el-table-column label="活动开始时间" align="center" prop="startTime" width="190">
                <template v-slot="scope">
                    <span>{{ "开始: " + parseTime(scope.row.startTime) }}</span>
                    <span>{{ "结束: " + parseTime(scope.row.endTime) }}</span>
                </template>
            </el-table-column>

            <el-table-column label="付款订单数" align="center" prop="orderCount" />
            <el-table-column label="付款人数" align="center" prop="userCount" />
            <el-table-column label="创建时间" align="center" prop="createTime" width="180">
                <template v-slot="scope">
                    <span>{{ parseTime(scope.row.createTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template v-slot="scope">
                    <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                        v-hasPermi="['promotion:seckill-activity:update']">修改</el-button>
                    <el-button size="mini" type="text" icon="el-icon-close" @click="handleClose(scope.row)"
                        v-hasPermi="['promotion:seckill-activity:delete']">关闭</el-button>
                    <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                        v-hasPermi="['promotion:seckill-activity:delete']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>
        <!-- 分页组件 -->
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
            @pagination="getList" />

        <!-- 对话框(添加 / 修改) -->
        <el-dialog :title="title" :visible.sync="open" width="1200px" v-dialogDrag append-to-body>
            <el-form ref="form" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="活动名称" prop="name">
                    <el-input v-model="form.name" placeholder="请输入秒杀活动名称" />
                </el-form-item>
                <el-form-item label="活动时间" prop="startAndEndTime">
                    <el-date-picker clearable v-model="form.startAndEndTime" type="datetimerange"
                        value-format="timestamp" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期"
                        style="width: 1080px" />
                </el-form-item>
                <el-form-item label="排序" prop="sort">
                    <el-input-number v-model="form.sort" controls-position="right" :min="0" :max="10000">
                    </el-input-number>
                </el-form-item>
                <el-form-item label="备注" prop="remark">
                    <el-input type="textarea" v-model="form.remark" placeholder="请输入备注" />
                </el-form-item>
                <el-form-item label="场次选择">
                    <el-select v-model="form.timeIds" placeholder="请选择参与场次" clearable size="small" multiple filterable
                        style="width: 880px">
                        <el-option v-for="item in seckillTimeList" :key="item.id" :label="item.name" :value="item.id">
                            <span style="float: left">{{ item.name + ': { ' }} {{ item.startTime }} -- {{ item.endTime +
                                    ' }'
                            }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px"></span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="商品选择">
                    <el-select v-model="form.skuIds" placeholder="请选择活动商品" clearable size="small" multiple filterable
                        style="width: 880px" @change="changeFormSku">
                        <el-option v-for="item in productSkus" :key="item.id" :label="item.spuName + ' ' + item.name"
                            :value="item.id">
                            <span style="float: left">{{ item.spuName }} &nbsp; {{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">￥{{ (item.price /
                                    100.0).toFixed(2)
                            }}</span>
                        </el-option>
                    </el-select>
                    <el-row>
                        <el-button type="primary" size="mini" @click="batchEditProduct('limitBuyCount')">限购</el-button>
                        <el-button type="primary" size="mini" @click="batchEditProduct('seckillPrice')">秒杀价</el-button>
                        <el-button type="primary" size="mini" @click="batchEditProduct('seckillStock')">秒杀库存</el-button>
                    </el-row>
                    <el-table v-loading="loading" ref="productsTable" :data="form.products">
                        <el-table-column type="selection" width="55">
                        </el-table-column>
                        <el-table-column label="商品名称" align="center" width="200">
                            <template v-slot="scope">
                                {{ scope.row.spuName }} &nbsp; {{ scope.row.name }}
                            </template>
                        </el-table-column>
                        <el-table-column label="商品价格" align="center" prop="price">
                            <template v-slot="scope">
                                ￥{{ (scope.row.price / 100.0).toFixed(2) }}
                            </template>
                        </el-table-column>
                        <el-table-column label="库存" align="center" prop="productStock" />
                        <el-table-column label="限购(0为不限购)" align="center" width="150">
                            <template v-slot="scope">
                                <el-input-number v-model="scope.row.limitBuyCount" size="mini" :min="0" :max="10000">
                                </el-input-number>
                            </template>
                        </el-table-column>
                        <el-table-column label="秒杀价(元)" align="center" width="150">
                            <template v-slot="scope">
                                <el-input-number v-model="scope.row.seckillPrice" size="mini" :precision="2" :min="0"
                                    :max="10000">
                                </el-input-number>
                            </template>
                        </el-table-column>
                        <el-table-column label="秒杀库存" align="center" width="150" prop="seckillStock">
                            <template v-slot="scope">
                                <el-input-number v-model="scope.row.seckillStock" size="mini" :min="0" :max="10000">
                                </el-input-number>
                            </template>
                        </el-table-column>
                        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                            <template v-slot="scope">
                                <el-button size="mini" type="text" icon="el-icon-delete"
                                    @click="removeFormSku(scope.row.skuId)">删除
                                </el-button>
                            </template>
                        </el-table-column>
                    </el-table>
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
import { getSkuOptionList } from "@/api/mall/product/sku";
import { createSeckillActivity, updateSeckillActivity, closeSeckillActivity, deleteSeckillActivity, getSeckillActivity, getSeckillActivityPage, exportSeckillActivityExcel } from "@/api/mall/promotion/seckillActivity";
import { getSeckillTimeList } from "@/api/mall/promotion/seckillTime";
import { deepClone } from "@/utils";

export default {
    name: "SeckillActivity",
    components: {
    },
    data() {
        return {
            // 遮罩层
            loading: true,
            // 显示搜索条件
            showSearch: true,
            // 总条数
            total: 0,
            // 秒杀活动列表
            list: [],
            // 秒杀场次列表
            seckillTimeList: [],
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
                timeId: null,
                createTime: [],
            },
            // 表单参数
            form: {
                skuIds: [], // 选中的 SKU
                products: [], // 商品信息
                timeIds: [], //选中的秒杀场次id
            },
            // 商品 SKU 列表
            productSkus: [],
            // 表单校验
            rules: {
                name: [{ required: true, message: "秒杀活动名称不能为空", trigger: "blur" }],
                status: [{ required: true, message: "活动状态不能为空", trigger: "blur" }],
                startAndEndTime: [{ required: true, message: "活动时间不能为空", trigger: "blur" }],
                sort: [{ required: true, message: "排序不能为空", trigger: "blur" }],
                timeIds: [{ required: true, message: "秒杀场次不能为空", trigger: "blur" }],
                totalPrice: [{ required: true, message: "订单实付金额，单位：分不能为空", trigger: "blur" }],
            }
        };
    },
    created() {
        this.getList();
    },
    watch: {
        $route: 'getList'
    },
    methods: {
        /** 查询列表 */
        getList() {
            // 从秒杀时段跳转过来并鞋带timeId参数进行查询
            const timeId = this.$route.params && this.$route.params.timeId;
            if (timeId) {
                this.queryParams.timeId = timeId
            }
            this.loading = true;
            // 执行查询
            getSeckillActivityPage(this.queryParams).then(response => {
                console.log(response.data.list, "查询出的值");
                this.list = response.data.list;
                this.total = response.data.total;
                this.loading = false;
            });
            if (timeId) {
                //查询完成后设置为空
                this.$route.params.timeId = undefined
            }
            // 获得 SKU 商品列表
            getSkuOptionList().then(response => {
                this.productSkus = response.data;
            });
            // 获取参与场次列表
            getSeckillTimeList().then(response => {
                this.seckillTimeList = response.data;
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
                status: undefined,
                remark: undefined,
                startTime: undefined,
                endTime: undefined,
                sort: undefined,
                timeIds: [],
                totalPrice: undefined,
                skuIds: [],
                products: [],
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
        /**打开秒杀场次管理页面 */
        openSeckillTime() {
            this.$tab.openPage("秒杀场次管理", "/promotion/seckill-time");
        },
        /** 新增按钮操作 */
        handleAdd() {
            this.reset();
            this.open = true;
            this.title = "添加秒杀活动";
        },
        /** 修改按钮操作 */
        handleUpdate(row) {
            this.reset();
            const id = row.id;
            getSeckillActivity(id).then(response => {
                this.form = response.data;
                // 修改数据
                this.form.startAndEndTime = [response.data.startTime, response.data.endTime];
                this.form.skuIds = response.data.products.map(item => item.skuId);
                this.form.products.forEach(product => {
                    // 获得对应的 SKU 信息
                    const sku = this.productSkus.find(item => item.id === product.skuId);
                    if (!sku) {
                        return;
                    }
                    // 设置商品信息
                    product.name = sku.name;
                    product.spuName = sku.spuName;
                    product.price = sku.price;
                    product.productStock = sku.stock;
                    this.$set(product, 'seckillStock', product.stock);
                    product.seckillPrice = product.seckillPrice !== undefined ? product.seckillPrice / 100 : undefined;

                });
                // 打开弹窗
                this.open = true;
                this.title = "修改限时折扣活动";
            })
        },
        /** 提交按钮 */
        submitForm() {
            this.$refs["form"].validate(valid => {
                if (!valid) {
                    return;
                }
                // 处理数据
                const data = deepClone(this.form);
                data.startTime = this.form.startAndEndTime[0];
                data.endTime = this.form.startAndEndTime[1];
                data.products.forEach(product => {
                    product.stock = product.seckillStock;
                    product.seckillPrice = product.seckillPrice !== undefined ? product.seckillPrice * 100 : undefined;
                });
                // 修改的提交
                if (this.form.id != null) {
                    updateSeckillActivity(data).then(response => {
                        this.$modal.msgSuccess("修改成功");
                        this.open = false;
                        this.getList();
                    });
                    return;
                }
                // 添加的提交
                createSeckillActivity(data).then(response => {
                    this.$modal.msgSuccess("新增成功");
                    this.open = false;
                    this.getList();
                });
            });
        },
        /** 关闭按钮操作 */
        handleClose(row) {
            const id = row.id;
            this.$modal.confirm('是否确认关闭秒杀活动编号为"' + id + '"的数据项?').then(function () {
                return closeSeckillActivity(id);
            }).then(() => {
                this.getList();
                this.$modal.msgSuccess("关闭成功");
            }).catch(() => { });
        },
        /** 删除按钮操作 */
        handleDelete(row) {
            const id = row.id;
            this.$modal.confirm('是否确认删除秒杀活动编号为"' + id + '"的数据项?').then(function () {
                return deleteSeckillActivity(id);
            }).then(() => {
                this.getList();
                this.$modal.msgSuccess("删除成功");
            }).catch(() => { });
        },
        /** 批量修改商品秒杀价，秒杀库存，每人限购数量 */
        batchEditProduct(editType) {
            const selectProducts = this.$refs.productsTable.selection;
            if (selectProducts.length === 0) {
                this.$modal.msgError("请选择需要修改的商品");
                return;
            }
            let promptTitle = '请输入';
            let regularPattern = /^[\s\S]*.*[^\s][\s\S]*$/; // 判断非空，且非空格
            //限购数
            if (editType === 'limitBuyCount') {
                promptTitle = '限购数';
                regularPattern = /^[0-9]*$/; //数字
            }
            //秒杀价
            if (editType === 'seckillPrice') {
                promptTitle = '秒杀价(元)';
                regularPattern = /^[0-9]+(\.[0-9]{1,2})?$/; // 有一位或两位小数的正数
            }
            //秒杀库存
            if (editType === 'seckillStock') {
                promptTitle = '秒杀库存';
                regularPattern = /^[0-9]*$/; //数字
            }

            this.$prompt(promptTitle, '提示', {
                confirmButtonText: '保存',
                cancelButtonText: '取消',
                inputPattern: regularPattern,
                inputErrorMessage: promptTitle + '格式不正确'
            }).then(({ value }) => {
                if (editType === 'limitBuyCount') {
                    selectProducts.forEach((item) => {
                        item.limitBuyCount = value;
                    })
                }
                if (editType === 'seckillPrice') {
                    selectProducts.forEach((item) => {
                        item.seckillPrice = value;
                    })
                }
                if (editType === 'seckillStock') {
                    selectProducts.forEach((item) => {
                        item.seckillStock = value;
                    })
                }
            }).catch();
        },
        /** 当 Form 的 SKU 发生变化时 */
        changeFormSku(skuIds) {
            // 处理【新增】
            skuIds.forEach(skuId => {
                // 获得对应的 SKU 信息
                const sku = this.productSkus.find(item => item.id === skuId);
                if (!sku) {
                    return;
                }
                // 判断已存在，直接跳过
                const product = this.form.products.find(item => item.skuId === skuId);
                if (product) {
                    return;
                }
                this.form.products.push({
                    skuId: sku.id,
                    name: sku.name,
                    price: sku.price,
                    productStock: sku.stock,
                    spuId: sku.spuId,
                    spuName: sku.spuName,
                    limitBuyCount: 1,
                    seckillStock: sku.stock,
                    seckillPrice: sku.price,
                });
            });
            // 处理【移除】
            this.form.products.map((product, index) => {
                if (!skuIds.includes(product.skuId)) {
                    this.form.products.splice(index, 1);
                }
            });
        },
        /** 移除 Form 的 SKU */
        removeFormSku(skuId) {
            this.form.skuIds.map((id, index) => {
                if (skuId === id) {
                    this.form.skuIds.splice(index, 1);
                }
            });
            this.changeFormSku(this.form.skuIds);
        },
    }
};
</script>
