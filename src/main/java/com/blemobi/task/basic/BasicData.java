package com.blemobi.task.basic;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

import com.blemobi.library.client.BaseHttpClient;
import com.blemobi.library.client.OssHttpClient;
import com.blemobi.sep.probuf.OssProtos.PDownload;
import com.blemobi.sep.probuf.ResultProtos.PMessage;
import com.blemobi.sep.probuf.ResultProtos.PResult;
import com.google.common.base.Strings;

import lombok.extern.log4j.Log4j;

/**
 * 读取CSV中的数据
 */
@Log4j
public class BasicData {
	private InputStream inp;
	private Workbook wb;
	private String fileUrl;

	// 全部任务类型（消息订阅）
	public static Map<Integer, TaskTypeInfo> taskTypeMap = new LinkedHashMap<Integer, TaskTypeInfo>();
	// 全部主线任务
	public static Map<Integer, TaskInfo> mainTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 全部日常任务
	public static Map<Integer, TaskInfo> dailyTaskMap = new LinkedHashMap<Integer, TaskInfo>();
	// 任务ID对应的任务Tag（主线还是日常）
	public static Map<Integer, TaskTag> taskIdtoTag = new LinkedHashMap<Integer, TaskTag>();
	// 全部等级
	public static Map<Integer, LevelInfo> levelMap = new LinkedHashMap<Integer, LevelInfo>();
	// 任务难度
	public static Map<Integer, Difficulty> difficultyMap = new LinkedHashMap<Integer, Difficulty>();

	/*
	 * 构造方法
	 */
	public BasicData(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	/*
	 * 初始化数据
	 */
	public void init() throws EncryptedDocumentException, InvalidFormatException, IOException {
		try {
			// 创建要读入的文件的输入流
			this.inp = getFileNameFromUrl(fileUrl);// new
													// FileInputStream(this.fileUrl);
			// 根据上述创建的输入流 创建工作簿对象
			this.wb = WorkbookFactory.create(inp);

			this.readTaskType(0);
			this.readMainTask(1);
			this.readDailyTask(2);
			this.readLevel(3);
			this.readDifficulty(4);

			log.debug("任务配置数据读取完成！");
		} catch (Exception e) {
			log.error("任务配置数据读取异常：" + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		} finally {
			this.close();
			this.out();
		}
	}

	/*
	 * 读取任务类型信息
	 */
	private void readTaskType(int sheetId) {
		Sheet sheet = this.wb.getSheetAt(sheetId);
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
				try {
					int type = (int) row.getCell(keyMap.get("id")).getNumericCellValue();
					TaskTypeInfo taskTypeInfo = new TaskTypeInfo();
					taskTypeInfo.setType(type);
					taskTypeInfo.setServer(row.getCell(keyMap.get("server")).toString());
					taskTypeInfo.setDesc_sc(row.getCell(keyMap.get("desc-sc")).toString());
					taskTypeInfo.setDesc_tc(row.getCell(keyMap.get("desc-tc")).toString());
					taskTypeInfo.setDesc_kr(row.getCell(keyMap.get("desc-kr")).toString());
					taskTypeInfo.setDesc_en(row.getCell(keyMap.get("desc-en")).toString());

					taskTypeMap.put(type, taskTypeInfo);
				} catch (Exception e) {

				}
			}
		}
	}

	/*
	 * 读取主线任务信息
	 */
	private void readMainTask(int sheetId) {
		Sheet sheet = this.wb.getSheetAt(sheetId);
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
				try {
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
					if (Strings.isNullOrEmpty(value)) {// 无依赖
						taskInfo.setLogic('N');
					} else {// 有依赖
						if (value.indexOf("&") >= 0) {// 依赖多个任务，必须全部完成
							getDepend(taskInfo, value, '&');
						} else if (value.indexOf("|") >= 0) {// 依赖多个任务，只需完成其中一个
							getDepend(taskInfo, value, '|');
						} else {// 只依赖一个任务
							taskInfo.setLogic('Y');
							taskInfo.addDepend(Integer.parseInt(value.substring(0, value.indexOf(".0"))));
						}
					}

					mainTaskMap.put(taskid, taskInfo);

					TaskTypeInfo taskTypeInfo = taskTypeMap.get(type);
					taskTypeInfo.addTaskidList(taskid);
					taskTypeMap.put(type, taskTypeInfo);
					taskIdtoTag.put(taskid, TaskTag.MAIN);
				} catch (Exception e) {

				}
			}
		}
	}

	/*
	 * 读取日常任务信息
	 */
	private void readDailyTask(int sheetId) {
		Sheet sheet = this.wb.getSheetAt(sheetId);
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("taskid".equals(cell.toString())) {
						keyMap.put("taskid", cell.getColumnIndex());
					} else if ("type".equals(cell.toString())) {
						keyMap.put("type", cell.getColumnIndex());
					} else if ("exp".equals(cell.toString())) {
						keyMap.put("exp", cell.getColumnIndex());
					} else if ("desc".equals(cell.toString())) {
						keyMap.put("desc", cell.getColumnIndex());
					} else if ("easy".equals(cell.toString())) {
						keyMap.put("easy", cell.getColumnIndex());
					} else if ("common".equals(cell.toString())) {
						keyMap.put("common", cell.getColumnIndex());
					} else if ("hard".equals(cell.toString())) {
						keyMap.put("hard", cell.getColumnIndex());
					} else if ("epic".equals(cell.toString())) {
						keyMap.put("epic", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				try {
					TaskInfo taskInfo = new TaskInfo();
					int taskid = (int) row.getCell(keyMap.get("taskid")).getNumericCellValue();
					int type = (int) row.getCell(keyMap.get("type")).getNumericCellValue();
					taskInfo.setTaskid(taskid);
					taskInfo.setType(type);
					taskInfo.setExp((int) row.getCell(keyMap.get("exp")).getNumericCellValue());
					taskInfo.setDesc(row.getCell(keyMap.get("desc")).toString());
					taskInfo.setEasy_num((int) row.getCell(keyMap.get("easy")).getNumericCellValue());
					taskInfo.setCommon_num((int) row.getCell(keyMap.get("common")).getNumericCellValue());
					taskInfo.setHard_num((int) row.getCell(keyMap.get("hard")).getNumericCellValue());
					taskInfo.setEpic_num((int) row.getCell(keyMap.get("epic")).getNumericCellValue());

					dailyTaskMap.put(taskid, taskInfo);

					TaskTypeInfo taskTypeInfo = taskTypeMap.get(type);
					taskTypeInfo.addTaskidList(taskInfo.getTaskid());
					taskTypeMap.put(type, taskTypeInfo);
					taskIdtoTag.put(taskid, TaskTag.DAILY);
				} catch (Exception e) {

				}
			}
		}
	}

	/*
	 * 读取等级信息
	 */
	private void readLevel(int sheetId) {
		Sheet sheet = this.wb.getSheetAt(sheetId);
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("level".equals(cell.toString())) {
						keyMap.put("level", cell.getColumnIndex());
					} else if ("exp-min".equals(cell.toString())) {
						keyMap.put("exp-min", cell.getColumnIndex());
					} else if ("exp-max".equals(cell.toString())) {
						keyMap.put("exp-max", cell.getColumnIndex());
					} else if ("max-daily".equals(cell.toString())) {
						keyMap.put("max-daily", cell.getColumnIndex());
					} else if ("title-sc".equals(cell.toString())) {
						keyMap.put("title-sc", cell.getColumnIndex());
					} else if ("title-tc".equals(cell.toString())) {
						keyMap.put("title-tc", cell.getColumnIndex());
					} else if ("title-kr".equals(cell.toString())) {
						keyMap.put("title-kr", cell.getColumnIndex());
					} else if ("title-en".equals(cell.toString())) {
						keyMap.put("title-en", cell.getColumnIndex());
					} else if ("max-h".equals(cell.toString())) {
						keyMap.put("max-h", cell.getColumnIndex());
					} else if ("simple-pro".equals(cell.toString())) {
						keyMap.put("simple-pro", cell.getColumnIndex());
					} else if ("normal-pro".equals(cell.toString())) {
						keyMap.put("normal-pro", cell.getColumnIndex());
					} else if ("hard-pro".equals(cell.toString())) {
						keyMap.put("hard-pro", cell.getColumnIndex());
					} else if ("epic-pro".equals(cell.toString())) {
						keyMap.put("epic-pro", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				LevelInfo levelInfo = new LevelInfo();
				int level = (int) row.getCell(keyMap.get("level")).getNumericCellValue();
				levelInfo.setLevel(level);
				levelInfo.setExp_min((int) row.getCell(keyMap.get("exp-min")).getNumericCellValue());
				levelInfo.setExp_max((int) row.getCell(keyMap.get("exp-max")).getNumericCellValue());
				levelInfo.setMax((int) row.getCell(keyMap.get("max-daily")).getNumericCellValue());
				levelInfo.setTitle_en(row.getCell(keyMap.get("title-en")).toString());
				levelInfo.setTitle_kr(row.getCell(keyMap.get("title-kr")).toString());
				levelInfo.setTitle_sc(row.getCell(keyMap.get("title-sc")).toString());
				levelInfo.setTitle_tc(row.getCell(keyMap.get("title-tc")).toString());
				levelInfo.setMax_h((int) row.getCell(keyMap.get("max-h")).getNumericCellValue());
				levelInfo.setSimple_pro((int) (row.getCell(keyMap.get("simple-pro")).getNumericCellValue() * 100));
				levelInfo.setNormal_pro((int) (row.getCell(keyMap.get("normal-pro")).getNumericCellValue() * 100));
				levelInfo.setHard_pro((int) (row.getCell(keyMap.get("hard-pro")).getNumericCellValue() * 100));
				levelInfo.setEpic_pro((int) (row.getCell(keyMap.get("epic-pro")).getNumericCellValue() * 100));

				levelMap.put(level, levelInfo);
			}
		}
	}

	/*
	 * 读取任务难度信息
	 */
	private void readDifficulty(int sheetId) {
		Sheet sheet = this.wb.getSheetAt(sheetId);
		Map<String, Integer> keyMap = new HashMap<String, Integer>();
		for (Row row : sheet) {
			if (row.getRowNum() == 1) {
				for (Cell cell : row) {
					if ("id".equals(cell.toString())) {
						keyMap.put("id", cell.getColumnIndex());
					} else if ("desc".equals(cell.toString())) {
						keyMap.put("desc", cell.getColumnIndex());
					} else if ("d-exp".equals(cell.toString())) {
						keyMap.put("d-exp", cell.getColumnIndex());
					}
				}
			} else if (row.getRowNum() > 1) {
				Difficulty difficulty = new Difficulty();
				int id = (int) row.getCell(keyMap.get("id")).getNumericCellValue();
				difficulty.setId(id);
				difficulty.setDesc(row.getCell(keyMap.get("desc")).toString());
				difficulty.setExp((int) row.getCell(keyMap.get("d-exp")).getNumericCellValue());
				difficultyMap.put(id, difficulty);
			}
		}
	}

	/*
	 * 任务依赖处理
	 */
	private static void getDepend(TaskInfo taskInfo, String value, char logic) {
		String[] array = value.split("\\" + logic);
		taskInfo.setLogic(logic);
		for (int n = 0; n < array.length; n++) {
			taskInfo.addDepend(Integer.parseInt(array[n]));
		}
	}

	/*
	 * 关闭输入流
	 */
	private void close() throws IOException {
		this.wb.close();
		this.inp.close();
	}

	/*
	 * 获取任务配置文件url
	 */
	public static String getTaskConfig() throws IOException {
		String url = "/oss/downloadurl?from=task&bucket=1&objectkey=config/task.xls";
		BaseHttpClient httpClient = new OssHttpClient(url, null, null);
		PMessage message = httpClient.getMethod();
		String type = message.getType();
		if ("PDownload".equals(type)) {
			PDownload download = PDownload.parseFrom(message.getData());
			return download.getUrl();
		} else {
			PResult result = PResult.parseFrom(message.getData());
			log.debug("获取任务配置文件url失败:" + result.getErrorCode());
		}
		return null;
	}

	public InputStream getFileNameFromUrl(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		// 设置超时间为3秒
		conn.setConnectTimeout(30 * 1000);
		// 防止屏蔽程序抓取而返回403错误
		conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

		// 得到输入流
		InputStream inputStream = conn.getInputStream();
		return inputStream;
	}

	/*
	 * 查看数据
	 */
	private void out() {
		System.out.println("------------主线任务信息[mainTaskMap]开始-------------");
		for (TaskInfo taskInfo : mainTaskMap.values()) {
			System.out.print("[" + taskInfo.getTaskid() + "] ");
			System.out.print("[" + taskInfo.getType() + "] ");
			System.out.print("[" + taskInfo.getNum() + "] ");
			System.out.print("[" + taskInfo.getLevel() + "] ");
			System.out.print("[" + taskInfo.getExp() + "] ");
			System.out.print("[" + taskInfo.getLogic() + "] ");
			System.out.print("[" + taskInfo.getDepend() + "] ");
			System.out.print("[" + taskInfo.getDesc() + "] ");
			System.out.println();
		}
		System.out.println("------------主线任务信息[mainTaskMap]结束-------------");

		System.out.println("------------日常务信息[dailyTaskMap]开始-------------");
		for (TaskInfo taskInfo : dailyTaskMap.values()) {
			System.out.print("[" + taskInfo.getTaskid() + "] ");
			System.out.print("[" + taskInfo.getType() + "] ");
			System.out.print("[" + taskInfo.getEasy_num() + "] ");
			System.out.print("[" + taskInfo.getCommon_num() + "] ");
			System.out.print("[" + taskInfo.getHard_num() + "] ");
			System.out.print("[" + taskInfo.getEpic_num() + "] ");
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
			System.out.print("[" + taskTypeInfo.getTaskidList() + "] ");
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

		System.out.println("------------等级信息[levelMap]开始-------------");
		for (LevelInfo levelInfo : levelMap.values()) {
			System.out.print("[" + levelInfo.getLevel() + "] ");
			System.out.print("[" + levelInfo.getExp_min() + "] ");
			System.out.print("[" + levelInfo.getExp_max() + "] ");
			System.out.print("[" + levelInfo.getMax() + "] ");
			System.out.print("[" + levelInfo.getTitle_en() + "] ");
			System.out.print("[" + levelInfo.getTitle_kr() + "] ");
			System.out.print("[" + levelInfo.getTitle_sc() + "] ");
			System.out.print("[" + levelInfo.getTitle_tc() + "] ");

			System.out.print("[" + levelInfo.getMax_h() + "] ");
			System.out.print("[" + levelInfo.getSimple_pro() + "] ");
			System.out.print("[" + levelInfo.getNormal_pro() + "] ");
			System.out.print("[" + levelInfo.getHard_pro() + "] ");
			System.out.print("[" + levelInfo.getEpic_pro() + "] ");
			System.out.println();
		}
		System.out.println("------------等级信息[levelMap]结束-------------");

		System.out.println("------------任务难度信息[difficultyMap]开始-------------");
		for (Difficulty difficulty : difficultyMap.values()) {
			System.out.print("[" + difficulty.getId() + "] ");
			System.out.print("[" + difficulty.getDesc() + "] ");
			System.out.print("[" + difficulty.getExp() + "] ");
			System.out.println();
		}
		System.out.println("------------任务难度信息[difficultyMap]结束-------------");
	}
}