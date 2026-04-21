package cn.iocoder.yudao.module.deepay.controller;

import cn.iocoder.yudao.module.deepay.dal.dataobject.DeepayStyleChainDO;
import cn.iocoder.yudao.module.deepay.dal.mysql.DeepayStyleChainMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Deepay 商品展示页。
 *
 * <p>GET /product/{chainCode} —— 根据链码查询数据库，直接返回基础 HTML 商品页，无需前端框架。</p>
 */
@RestController
@RequestMapping("/product")
public class DeepayProductPageController {

    @Resource
    private DeepayStyleChainMapper deepayStyleChainMapper;

    @GetMapping(value = "/{chainCode}", produces = "text/html;charset=UTF-8")
    public String productPage(@PathVariable String chainCode) {
        DeepayStyleChainDO chain = deepayStyleChainMapper.selectOne(
                new LambdaQueryWrapper<DeepayStyleChainDO>()
                        .eq(DeepayStyleChainDO::getChainCode, chainCode));

        if (chain == null) {
            return "<html><body><h2>商品不存在（" + escapeHtml(chainCode) + "）</h2></body></html>";
        }

        String imageUrl = chain.getImageUrl() != null ? chain.getImageUrl() : "";
        String imgTag = imageUrl.isEmpty() ? "" :
                "<img src=\"" + escapeHtml(imageUrl) + "\" width=\"300\" style=\"border-radius:8px;\"/><br/><br/>";

        return "<!DOCTYPE html>\n"
                + "<html lang=\"zh\">\n"
                + "<head>\n"
                + "  <meta charset=\"UTF-8\"/>\n"
                + "  <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>\n"
                + "  <title>意式极简羊绒大衣</title>\n"
                + "</head>\n"
                + "<body style=\"font-family:sans-serif;max-width:400px;margin:40px auto;text-align:center;\">\n"
                + "  <h1>意式极简羊绒大衣</h1>\n"
                + "  " + imgTag + "\n"
                + "  <p>高端双面羊绒面料，极简设计</p>\n"
                + "  <h2>€299</h2>\n"
                + "  <p style=\"color:#888;font-size:12px;\">商品编号：" + escapeHtml(chainCode) + "</p>\n"
                + "</body>\n"
                + "</html>";
    }

    /** 对用户输入/数据库内容做最基本的 HTML 转义，防止 XSS。 */
    private static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }

}
