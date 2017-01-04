package com.blemobi.task.util;

import java.util.Comparator;
import com.blemobi.sep.probuf.DataPublishingProtos.PGuy;

/**
 * 根据经验值排序
 * 
 * @author zhaoyong
 *
 */
public class ExpComparator implements Comparator<PGuy> {
	public int compare(PGuy o1, PGuy o2) {
		return o2.getRankValue() - o1.getRankValue();
	}
}