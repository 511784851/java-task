package com.blemobi.task.basic;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * 读取任务配置文件中的内容
 * 
 * @author zhaoyong
 *
 */
public class TaskFile {
	private Map<Short, MsgInfo> msg_DATA;
	private Map<Short, TaskInfo> task_data;

	private InputStream inp;
	private Workbook wb;

	/**
	 * 构造方法
	 * 
	 * @param fileUrl
	 *            文件路径
	 * @param msg_DATA
	 *            全部消息信息
	 * @param task_data
	 *            全部任务信息
	 */
	public TaskFile(Map<Short, MsgInfo> msg_DATA, Map<Short, TaskInfo> task_data) {
		this.msg_DATA = msg_DATA;
		this.task_data = task_data;
	}

	/**
	 * 读取文件
	 * 
	 * @throws Exception
	 */
	public void load() throws Exception {
		try {
			// 创建要读入的文件的输入流
			inp = TaskFile.class.getClassLoader().getResourceAsStream("task.xls");
			// 根据上述创建的输入流 创建工作簿对象
			wb = WorkbookFactory.create(inp);
			readMsgID();
			readTask();
		} finally {
			inp.close();
			wb.close();
		}
	}

	/**
	 * 处理消息数据
	 */
	private void readMsgID() {
		Sheet sheet = wb.getSheetAt(0);
		sheet.forEach(row -> {
			if (row.getRowNum() > 0) {
				short ID = (short) row.getCell(0).getNumericCellValue();
				if (ID > 0) {
					MsgInfo msgInfo = new MsgInfo();
					msgInfo.setMsgID(ID);
					msgInfo.setServer(row.getCell(1).getStringCellValue());
					msg_DATA.put(ID, msgInfo);
				}
			}
		});
	}

	/**
	 * 处理任务数据
	 */
	private void readTask() {
		Sheet sheet = wb.getSheetAt(1);
		sheet.forEach(row -> {
			if (row.getRowNum() > 0) {
				short ID = (short) row.getCell(0).getNumericCellValue();
				if (ID > 0) {
					List<Short> list = new ArrayList<>();
					list.add((short) row.getCell(1).getNumericCellValue());
					short msgID1 = (short) row.getCell(2).getNumericCellValue();
					if (msgID1 > 0)
						list.add(msgID1);
					TaskInfo taskInfo = new TaskInfo();
					taskInfo.setID(ID);
					taskInfo.setMsgIDs(list);
					taskInfo.setTarg((byte) row.getCell(3).getNumericCellValue());
					taskInfo.setGold((short) row.getCell(4).getNumericCellValue());
					taskInfo.setLoop((byte) row.getCell(5).getNumericCellValue());
					taskInfo.setDesc_sc(row.getCell(6).getStringCellValue());
					taskInfo.setDesc_tc(row.getCell(7).getStringCellValue());
					taskInfo.setDesc_en(row.getCell(8).getStringCellValue());
					taskInfo.setDesc_kr(row.getCell(9).getStringCellValue());
					task_data.put(ID, taskInfo);
				}
			}
		});
	}
}