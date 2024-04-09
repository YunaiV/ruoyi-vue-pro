package cn.iocoder.yudao.framework.ai.midjourney.constants;

public final class MidjourneyConstants {

	/**
	 * 消息 - 编号
	 */
	public static final String MSG_ID = "id";
	/**
	 * 消息 - 类型
	 * 现在已知：
	 * 0：我们发送的消息，和指令
	 * 20: mj生成图片发送过程中
	 * 19: 选择了某一张图片后的通知
	 */
	public static final String MSG_TYPE = "type";
	/**
	 * 平道id
	 */
	public static final String MSG_CHANNEL_ID = "channel_id";
	/**
	 * 内容
	 *
	 * "**南极应该是什么样子？ --v 6.0 --style raw** - <@972721304891453450> (32%) (fast, stealth)",
	 */
	public static final String MSG_CONTENT = "content";
	/**
	 * 组件(图片生成好之后才有)
	 */
	public static final String MSG_COMPONENTS = "components";
	/**
	 * 附件(生成中比较模糊的图片)
	 */
	public static final String MSG_ATTACHMENTS = "attachments";


	//
	//

	public static final String HTTP_COOKIE = "__dcfduid=6ca536c0e3fa11eeb7cbe34c31b49caf; __sdcfduid=6ca536c1e3fa11eeb7cbe34c31b49caf52cce5ffd8983d2a052cf6aba75fe5fe566f2c265902e283ce30dbf98b8c9c93; _gcl_au=1.1.245923998.1710853617; _ga=GA1.1.111061823.1710853617; __cfruid=6385bb3f48345a006b25992db7dcf984e395736d-1712124666; _cfuvid=O09la5ms0ypNptiG0iD8A6BKWlTxz1LG0WR7qRStD7o-1712124666575-0.0.1.1-604800000; locale=zh-CN; cf_clearance=l_YGod1_SUtYxpDVeZXiX7DLLPl1DYrquZe8WVltvYs-1712124668-1.0.1.1-Hl2.fToel23EpF2HCu9J20rB4D7OhhCzoajPSdo.9Up.wPxhvq22DP9RHzEBKuIUlKyH.kJLxXJfAt2N.LD5WQ; OptanonConsent=isIABGlobal=false&datestamp=Wed+Apr+03+2024+14%3A11%3A15+GMT%2B0800+(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)&version=6.33.0&hosts=&landingPath=https%3A%2F%2Fdiscord.com%2F&groups=C0001%3A1%2CC0002%3A1%2CC0003%3A1; _ga_Q149DFWHT7=GS1.1.1712124668.4.1.1712124679.0.0.0";
}
