package com.blemobi.task.basic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.base.Strings;

/**
 * 读取CSV中的数据
 */
public class CsvFileUtil {
	private InputStream inp;
	private Workbook wb;

	// 全部任务类型（消息订阅）
	private Map<Integer, TaskTypeInfo> taskTypeMap = new LinkedHashMap<Integer, TaskTypeInfo>();
	// 全部主线任务
	private Map<Integer, TaskInfo> mainTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 全部日常任务
	private Map<Integer, TaskInfo> dailyTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 任务ID对应的任务Tag（主线还是日常）
	private Map<Integer, TaskTag> taskIdtoTag = new LinkedHashMap<Integer, TaskTag>();

	/*
	 * 构造方法
	 */
	public CsvFileUtil(String fileUrl, Map<Integer, TaskTypeInfo> taskTypeMap, Map<Integer, TaskInfo> mainTaskMap,
			Map<Integer, TaskTag> taskIdtoTag, Map<Integer, TaskInfo> dailyTaskMap)
			throws EncryptedDocumentException, InvalidFormatException, IOException {
		this.taskTypeMap = taskTypeMap;
		this.mainTaskMap = mainTaskMap;
		this.dailyTaskMap = dailyTaskMap;
		this.taskIdtoTag = taskIdtoTag;

		// 创建要读入的文件的输入流
		inp = new FileInputStream(fileUrl);
		// 根据上述创建的输入流 创建工作簿对象
		wb = WorkbookFactory.create(inp);
	}

	/*
	 * 读取任务类型信息
	 */
	public void readTaskType(int sheetId) {
		Sheet sheet = wb.getSheetAt(sheetId);
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("id".equals(cell.toString())) {
						keyMap.put("id", cell.getColumnIndex());
					} else if ("id".equals(cell.toString())) {
						keyMap.put("id", cell.getColumnIndex());
					} else if ("server".equals(cell.toString())) {
						keyMap.put("server", cell.getColumnIndex());
					} else if ("desc-sc".equals(cell.toString())) {
						keyMap.put("desc-sc", cell.getColumnIndex());
					} else if ("desc-tc".equals(cell.toString())) {
						keyMap.put("desc-tc", cell.getColumnIndex());
					} else if ("desc-kr".equals(cell.toString())) {
						keyMap.put("desc-kr", cell.getColumnIndex());
					} else if ("desc-en".equals(cell.toString())) {
						keyMap.put("desc-en", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				int type = (int) row.getCell(keyMap.get("id")).getNumericCellValue();
				TaskTypeInfo taskTypeInfo = new TaskTypeInfo();
				taskTypeInfo.setType(type);
				taskTypeInfo.setServer(row.getCell(keyMap.get("server")).toString());
				taskTypeInfo.setDesc_sc(row.getCell(keyMap.get("desc-sc")).toString());
				taskTypeInfo.setDesc_tc(row.getCell(keyMap.get("desc-tc")).toString());
				taskTypeInfo.setDesc_kr(row.getCell(keyMap.get("desc-kr")).toString());
				taskTypeInfo.setDesc_en(row.getCell(keyMap.get("desc-en")).toString());

				taskTypeMap.put(type, taskTypeInfo);
			}
		}
	}

	/*
	 * 读取主线任务信息
	 */
	public void readMainTask(int sheetId) {
		Sheet sheet = wb.getSheetAt(sheetId);
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("taskid".equals(cell.toString())) {
						keyMap.put("taskid", cell.getColumnIndex());
					} else if ("type".equals(cell.toString())) {
						keyMap.put("type", cell.getColumnIndex());
					} else if ("num".equals(cell.toString())) {
						keyMap.put("num", cell.getColumnIndex());
					} else if ("level".equals(cell.toString())) {
						keyMap.put("level", cell.getColumnIndex());
					} else if ("depend".equals(cell.toString())) {
						keyMap.put("depend", cell.getColumnIndex());
					} else if ("exp".equals(cell.toString())) {
						keyMap.put("exp", cell.getColumnIndex());
					} else if ("desc".equals(cell.toString())) {
						keyMap.put("desc", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				TaskInfo taskInfo = new TaskInfo();
				int taskid = (int) row.getCell(keyMap.get("taskid")).getNumericCellValue();
				int type = (int) row.getCell(keyMap.get("type")).getNumericCellValue();
				taskInfo.setTaskid(taskid);
				taskInfo.setType(type);
				taskInfo.setNum((int) row.getCell(keyMap.get("num")).getNumericCellValue());
				taskInfo.setLevel((int) row.getCell(keyMap.get("level")).getNumericCellValue());
				taskInfo.setExp((int) row.getCell(keyMap.get("exp")).getNumericCellValue());
				taskInfo.setDesc(row.getCell(keyMap.get("desc")).toString());
				String value = row.getCell(keyMap.get("depend")).toString();
				if (value.indexOf("&") >= 0) {
					getDepend(taskInfo, value, '&');
				} else if (value.indexOf("|") >= 0) {
					getDepend(taskInfo, value, '|');
				} else if (!Strings.isNullOrEmpty(value)) {
					taskInfo.setLogic('Y');
					taskInfo.setDepend(new int[] { Integer.parseInt(value.substring(0, value.indexOf(".0"))) });
				} else {
					taskInfo.setLogic('N');
				}
				mainTaskMap.put(taskid, taskInfo);

				TaskTypeInfo taskTypeInfo = taskTypeMap.get(type);
				taskTypeInfo.addTaskidMap(taskid, TaskTag.MAIN);
				taskTypeMap.put(type, taskTypeInfo);
				taskIdtoTag.put(taskid, TaskTag.MAIN);
			}
		}
	}

	/*
	 * 读取日常任务信息
	 */
	public void readDailyTask(int sheetId) {
		Sheet sheet = wb.getSheetAt(sheetId);

		Map<String, Integer> keyMap = new HashMap<String, Integer>();

		// 利用foreach循环 遍历sheet中的所有行
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("taskid".equals(cell.toString())) {
						keyMap.put("taskid", cell.getColumnIndex());
					} else if ("type".equals(cell.toString())) {
						keyMap.put("type", cell.getColumnIndex());
					} else if ("num".equals(cell.toString())) {
						keyMap.put("num", cell.getColumnIndex());
					} else if ("exp".equals(cell.toString())) {
						keyMap.put("exp", cell.getColumnIndex());
					} else if ("desc".equals(cell.toString())) {
						keyMap.put("desc", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				TaskInfo taskInfo = new TaskInfo();
				int taskid = (int) row.getCell(keyMap.get("taskid")).getNumericCellValue();
				int type = (int) row.getCell(keyMap.get("type")).getNumericCellValue();
				taskInfo.setTaskid(taskid);
				taskInfo.setType(type);
				taskInfo.setNum((int) row.getCell(keyMap.get("num")).getNumericCellValue());
				taskInfo.setExp((int) row.getCell(keyMap.get("exp")).getNumericCellValue());
				taskInfo.setDesc(row.getCell(keyMap.get("desc")).toString());

				dailyTaskMap.put(taskid, taskInfo);

				TaskTypeInfo taskTypeInfo = taskTypeMap.get(type);
				taskTypeInfo.addTaskidMap(taskInfo.getTaskid(), TaskTag.DAILY);
				taskTypeMap.put(type, taskTypeInfo);
				taskIdtoTag.put(taskid, TaskTag.DAILY);
			}
		}
	}

	/*
	 * 任务依赖处理
	 */
	private static void getDepend(TaskInfo taskInfo, String value, char logic) {
		String[] array = value.split("\\" + logic);
		int[] depend = new int[array.length];
		for (int n = 0; n < array.length; n++) {
			depend[n] = Integer.parseInt(array[n]);
		}
		taskInfo.setLogic(logic);
		taskInfo.setDepend(depend);
	}

	/*
	 * 关闭输入流
	 */
	public void close() throws IOException {
		wb.close();
		inp.close();
	}

	/*
	 * 查看数据
	 */
	public void out() {
		System.out.println("------------主线任务信息[mainTaskMap]开始-------------");
		for (TaskInfo taskInfo : mainTaskMap.values()) {
			System.out.print("[" + taskInfo.getTaskid() + "] ");
			System.out.print("[" + taskInfo.getType() + "] ");
			System.out.print("[" + taskInfo.getNum() + "] ");
			System.out.print("[" + taskInfo.getLevel() + "] ");
			System.out.print("[" + taskInfo.getExp() + "] ");
			System.out.print("[" + taskInfo.getLogic() + "] ");
			int[] depend = taskInfo.getDepend();
			String depends = "";
			if (depend != null) {
				for (int d : depend) {
					if (depends.length() > 0) {
						depends += ", ";
					}
					depends += d;
				}
			}
			System.out.print("[" + depends + "] ");
			System.out.print("[" + taskInfo.getDesc() + "] ");
			System.out.println();
		}
		System.out.println("------------主线任务信息[mainTaskMap]结束-------------");

		System.out.println("------------日常务信息[dailyTaskMap]开始-------------");
		for (TaskInfo taskInfo : dailyTaskMap.values()) {
			System.out.print("[" + taskInfo.getTaskid() + "] ");
			System.out.print("[" + taskInfo.getType() + "] ");
			System.out.print("[" + taskInfo.getNum() + "] ");
			System.out.print("[" + taskInfo.getExp() + "] ");
			System.out.print("[" + taskInfo.getDesc() + "] ");
			System.out.println();
		}
		System.out.println("------------日常任务信息[dailyTaskMap]结束-------------");

		System.out.println("------------任务类型信息[taskTypeInfo]开始-------------");
		for (TaskTypeInfo taskTypeInfo : taskTypeMap.values()) {
			System.out.print("[" + taskTypeInfo.getType() + "] ");
			System.out.print("[" + taskTypeInfo.getServer() + "] ");
			System.out.print("[" + taskTypeInfo.getDesc_sc() + "] ");
			System.out.print("[" + taskTypeInfo.getDesc_tc() + "] ");
			System.out.print("[" + taskTypeInfo.getDesc_en() + "] ");
			System.out.print("[" + taskTypeInfo.getDesc_kr() + "] ");
			System.out.print("[" + taskTypeInfo.getTaskidMap().keySet() + "] ");
			System.out.println();
		}
		System.out.println("------------任务类型信息[taskTypeInfo]结束-------------");

		System.out.println("------------任务Tag信息[taskIdtoTag]开始-------------");
		for (int taskid : taskIdtoTag.keySet()) {
			System.out.print("[" + taskid + "] ");
			System.out.print("[" + taskIdtoTag.get(taskid) + "] ");
			System.out.println();
		}
		System.out.println("------------任务Tag信息[taskIdtoTag]结束-------------");
	}
}