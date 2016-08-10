package com.blemobi.gamification.rest;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class dsaf {

	public static void main(String[] args) {
		int a = 0;
		int b = 10;
		System.out.println(accuracy(a, b, 2));
	}

	// 方法1
	public static String accuracy(double num, double total, int scale) {
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		// 可以设置精确几位小数
		df.setMaximumFractionDigits(scale);
		// 模式 例如四舍五入
		df.setRoundingMode(RoundingMode.HALF_UP);
		double accuracy_num = num / total * 100;
		return df.format(accuracy_num) + "%";
	}

}
