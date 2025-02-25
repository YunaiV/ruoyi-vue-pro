package cn.iocoder.yudao.framework.common.util.concurrent;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SimpleJoinForkTask<IN,OUT>  extends RecursiveTask<List<OUT>> {

    private static ForkJoinPool POOL() {
        return new ForkJoinPool();
    }

    public static interface TaskExecutor<IN,OUT> {
        List<OUT> doTask(List<IN> input);
    }


    private List<IN> input;
    private int batchSize;

    private int parallelism = -1 ;

    private TaskExecutor<IN,OUT> executor;

    public SimpleJoinForkTask(List<IN> input,int batchSize) {
        this(input,batchSize,-1);
    }

    public SimpleJoinForkTask(List<IN> input,int batchSize, int parallelism) {
        this.input=input;
        this.batchSize=batchSize;
        this.parallelism=parallelism;
    }

    private SimpleJoinForkTask(List<IN> input,int batchSize,TaskExecutor<IN,OUT> executor) {
        this.input=input;
        this.batchSize=batchSize;
        this.executor=executor;
    }

    private List<OUT> executeInternal(List<IN> input) {
        return executor.doTask(input);
    }

    public List<OUT> execute(final TaskExecutor<IN,OUT> executor) {
        this.executor=executor;
        if(this.parallelism<=0) {
            return POOL().invoke(this);
        } else {
            ForkJoinPool pool = new ForkJoinPool(this.parallelism);
            List<OUT> list=pool.invoke(this);
            pool.shutdown();
            return list;
        }
    }

    /**
     * 按CPU核心数放大并发数
     * */
    public void scaleParallelism(int scale) {
        if(scale<=0) this.parallelism=-1;
        else {
            this.parallelism = scale * Runtime.getRuntime().availableProcessors();
        }
    }

    @Override
    protected List<OUT> compute() {

        //实际的分解执行
        if(input.size()<=batchSize) {
            return executeInternal(input);
        }

        List<IN> lefts=input.subList(0,batchSize);
        List<IN> rights=input.subList(batchSize,input.size());


        //任务1
        SimpleJoinForkTask<IN,OUT> leftTask = new SimpleJoinForkTask<>(lefts,batchSize,executor);
        //任务2
        SimpleJoinForkTask<IN,OUT> rightTask = new SimpleJoinForkTask<>(rights,batchSize,executor);

        invokeAll(leftTask,rightTask);

        List<OUT> leftResult = leftTask.join();
        List<OUT> rightResult=rightTask.join();

        // 避免并发异常
        List<OUT> list=new ArrayList<>();
        list.addAll(leftResult);
        list.addAll(rightResult);

        return  list;
    }
}
