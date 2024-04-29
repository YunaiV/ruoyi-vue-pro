package cn.iocoder.yudao.framework.ai.midjourney;

import cn.iocoder.yudao.framework.ai.midjourney.constants.MidjourneyGennerateStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MidjourneyMessage {

	/**
	 * id是一个重要的字段，在同时生成多个的时候，可以区分生成信息
	 */
	private String id;
	/**
	 * 提交id(nonce 可能会不存在，系统提示的时候，这个为空)
	 */
	private String nonce;
	/**
	 * 现在已知：
	 * 0：我们发送的消息，和指令
	 * 20: mj生成图片发送过程中
	 * 19: 选择了某一张图片后的通知
	 */
	private Integer type;
	/**
	 * content
	 */
	private Content content;
	/**
	 * 图片生成完成才有
	 */
	private List<ComponentType> components;
	/**
	 * 生成过程中如果有，预展示图片，这里会有
	 */
	private List<Attachment> attachments;
	/**
	 * 原始数据(discard 返回的原始数据)
	 */
	private String rawData;
	/**
	 * 生成状态(用于区分生成状态)
	 * 1、等待
	 * 2、进行中
	 * 3、完成
	 * {@link MidjourneyGennerateStatusEnum}
	 */
	private String generateStatus;
	/**
	 * 一般用于提示信息
	 * - 错误
	 * - 并发队列满了
	 * - 账号违规了、敏感词
	 * - 账号被封
	 */
	private List<Embed> embeds;

	@Data
	@Accessors(chain = true)
	public static class ComponentType {

		private int type;

		private List<Component> components;
	}

	@Data
	@Accessors(chain = true)
	public static class Component {
		/**
		 * 自定义ID，用于唯一标识特定交互动作及其上下文信息。
		 */
		private String customId;

		/**
		 * 样式编号，用于确定按钮的样式外观。
		 * 在某些应用中，例如Discord，2可能表示一种特定的颜色或形状的按钮。
		 */
		private int style;

		/**
		 * 按钮的标签文本，用户可见的内容。
		 */
		private String label;

		/**
		 * 组件类型，此处为2可能表示这是一种特定类型的交互组件，
		 * 如在Discord API中，类型2对应的是一个可点击的按钮组件。
		 */
		private int type;

	}

	@Data
	@Accessors(chain = true)
	public static class Attachment {
		// 文件名
		private String filename;

		// 附件大小（字节）
		private int size;

		// 内容类型（例如：image/webp）
		private String contentType;

		// 图像宽度（像素）
		private int width;

		// 占位符版本号
		private int placeholderVersion;

		// 代理URL，用于访问附件资源
		private String proxyUrl;

		// 占位符标识符
		private String placeholder;

		// 附件ID
		private String id;

		// 直接访问附件资源的URL
		private String url;

		// 图像高度（像素）
		private int height;
	}

	@Data
	@Accessors(chain = true)
	public static class Content {
		private String prompt;
		private String progress;
		private String status;
	}

	/**
	 * embed 用于警告、提示、错误
	 */
	@Data
	@Accessors(chain = true)
	public static class Embed {

		// 内容扫描版本号
		private int contentScanVersion;

		// 颜色值，这里用Java的Color类来表示，注意实际使用中可能需要自定义方法来从int转换为Color对象
		private String color;

		// 页脚信息，包含文本
		private Footer footer;

		// 描述信息
		private String description;

		// 消息类型，这里是富文本类型(这个区分不同提示类型)
		private String type;

		// 标题
		private String title;

		// Footer类，作为嵌套类存在，用来表示footer部分的JSON对象
		@Data
		@Accessors(chain = true)
		public static class Footer {
			// 页脚文本
			private String text;
		}

	}
}
