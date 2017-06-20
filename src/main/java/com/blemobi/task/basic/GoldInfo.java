package com.blemobi.task.basic;

/**
 * 金币明细信息
 * 
 * @author zhaoyong
 *
 */
public class GoldInfo {
	// ID或编号
	private String ID;
	// 类型（1-新手任务，2-每日任务，3-商城消费，4-监管操作）
	private byte type;
	// 金币（大于0收入，小于0支出）
	private short gold;
	// 发生时间
	private long time;
	// 描述
	private String desc;

	/**
	 * 构造方法
	 * 
	 * @param ID
	 *            ID或编号
	 * @param type
	 *            类型（1-新手任务，2-每日任务，3-商城消费，4-监管操作）
	 * @param gold
	 *            金币（大于0收入，小于0支出）
	 * @param desc
	 *            描述
	 */
	public GoldInfo(String ID, byte type, short gold, String desc) {
		this.ID = ID;
		this.type = type;
		this.gold = gold;
		this.desc = desc;
	}

	/**
	 * 构造方法
	 * 
	 * @param ID
	 *            ID或编号
	 * @param type
	 *            类型（1-新手任务，2-每日任务，3-商城消费，4-监管操作）
	 * @param gold
	 *            金币（大于0收入，小于0支出）
	 * @param desc
	 *            备注
	 * @param desc
	 *            描述
	 * @param time
	 *            发生时间
	 */
	public GoldInfo(String ID, byte type, short gold, String desc, long time) {
		this(ID, type, gold, desc);
		this.time = time;
	}

	public String getID() {
		return ID;
	}

	public byte getType() {
		return type;
	}

	public short getGold() {
		return gold;
	}

	public long getTime() {
		return time;
	}

	public String getDesc() {
		return desc;
	}

	/**
	 * 对象的字符串形式（不可随意修改此规则）
	 */
	@Override
	public String toString() {
		return new StringBuilder().append(System.currentTimeMillis()).append("/").append(type).append("/").append(ID)
				.append("/").append(gold).append("/").append(desc).toString();
	}

	/**
	 * 将字符串转化为GoldInfo对象
	 * 
	 * @param value
	 *            要转换的GoldInfo字符串
	 * @return GoldInfo
	 */
	public static GoldInfo parse(String value) {
		String[] array = value.split("/");
		long time = Long.parseLong(array[0]);
		byte type = Byte.parseByte(array[1]);
		String ID = array[2];
		short gold = Short.parseShort(array[3]);
		String desc = array[4];
		return new GoldInfo(ID, type, gold, desc, time);
	}
}
