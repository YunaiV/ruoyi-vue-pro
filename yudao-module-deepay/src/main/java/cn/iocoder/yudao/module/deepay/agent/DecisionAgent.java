package cn.iocoder.yudao.module.deepay.agent;

/**
 * DecisionAgent — 从 designImages 中选第一张（ChainOrchestrator 用）。
 */
public class DecisionAgent implements Agent {

    @Override
    public Context run(Context ctx) {
        if (ctx.designImages != null && !ctx.designImages.isEmpty()) {
            ctx.selectedImage = ctx.designImages.get(0);
        } else if (ctx.images != null && !ctx.images.isEmpty()) {
            ctx.selectedImage = ctx.images.get(0);
        } else {
            throw new IllegalStateException("DecisionAgent: 图片列表为空");
        }
        return ctx;
    }

}

