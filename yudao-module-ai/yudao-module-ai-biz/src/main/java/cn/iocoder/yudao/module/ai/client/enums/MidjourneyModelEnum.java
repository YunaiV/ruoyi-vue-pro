package cn.iocoder.yudao.module.ai.client.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 来源于 midjourney-proxy
 */
@Getter
@AllArgsConstructor
public enum MidjourneyModelEnum {

	MIDJOURNEY("midjourney", "midjourney"),
	NIJI("Niji", "Niji"),

	;

	private String model;
	private String name;

	public static MidjourneyModelEnum valueOfModel(String model) {
		for (MidjourneyModelEnum itemEnum : MidjourneyModelEnum.values()) {
			if (itemEnum.getModel().equals(model)) {
				return itemEnum;
			}
		}
		throw new IllegalArgumentException("Invalid MessageType value: " + model);
	}
}
