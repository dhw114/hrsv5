package hrds.agent.job.biz.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.utils.StringUtil;
import hrds.agent.job.biz.core.service.AbstractCollectTableHandle;
import hrds.commons.codes.FileFormat;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.Data_extraction_def;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class DataExtractUtil {

	private static final Log log = LogFactory.getLog(DataExtractUtil.class);
	private static final String DATADICTIONARY = "dd_data.json";

	/**
	 * 生成数据字典
	 */
	public static synchronized void writeDataDictionary(String dictionaryPath, String tableName, String allColumns, String
			allType, String storage_type, List<Data_extraction_def> data_extraction_defList, String unload_type, String primaryKeyInfo) {
		BufferedWriter bufferOutputWriter = null;
		OutputStreamWriter outputFileWriter = null;
		String dataDictionaryFile = dictionaryPath + DATADICTIONARY;
		try {
			File file = new File(dataDictionaryFile);
			String dd_data = "";
			if (file.exists()) {
				dd_data = FileUtil.readFile2String(file);
			}
			JSONArray jsonArray = new JSONArray();
			if (!StringUtil.isEmpty(dd_data)) {
				jsonArray = JSONArray.parseArray(dd_data);
			}
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if (jsonObject.getString("table_name").equals(tableName)) {
					jsonArray.remove(jsonObject);
				}
			}
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("table_name", tableName);
			jsonObject.put("table_cn_name", tableName);
			jsonObject.put("storage_type", storage_type);
			jsonObject.put("unload_type", unload_type);
			JSONArray storageArray = new JSONArray();
			for (Data_extraction_def data_extraction_def : data_extraction_defList) {
				JSONObject object = new JSONObject();
				object.put("is_header", data_extraction_def.getIs_header());
				object.put("file_format", data_extraction_def.getDbfile_format());
				object.put("file_code", data_extraction_def.getDatabase_code());
				object.put("root_path", data_extraction_def.getPlane_url());
				object.put("row_separator", StringUtil.string2Unicode(data_extraction_def.getRow_separator()));
				object.put("column_separator", StringUtil.string2Unicode(data_extraction_def.getDatabase_separatorr()));
				storageArray.add(object);
			}
			jsonObject.put("storage", storageArray);
			List<String> columnList = StringUtil.split(allColumns, AbstractCollectTableHandle.STRSPLIT);
			List<String> typeList = StringUtil.split(allType, AbstractCollectTableHandle.STRSPLIT);
			List<String> primaryKeyList = StringUtil.split(primaryKeyInfo, AbstractCollectTableHandle.STRSPLIT);
			List<JSONObject> array = new ArrayList<>();
			for (int i = 0; i < columnList.size(); i++) {
				JSONObject object = new JSONObject();
				object.put("column_type", typeList.get(i));
				object.put("column_null", "Y");
				object.put("column_remark", "");
				object.put("column_id", i);
				object.put("column_cn_name", "");
				object.put("column_name", columnList.get(i));
				object.put("column_key", IsFlag.Shi.getCode().equals(primaryKeyList.get(i)) ? "Y" : "N");
				array.add(object);
			}
			jsonObject.put("columns", array);
			jsonArray.add(jsonObject);
			//数据字典的编码默认直接使用utf-8
			outputFileWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
			bufferOutputWriter = new BufferedWriter(outputFileWriter, 4096);
			bufferOutputWriter.write(jsonArray.toJSONString());
			bufferOutputWriter.flush();
		} catch (Exception e) {
			log.error("写数据字典失败", e);
		} finally {
			try {
				if (bufferOutputWriter != null)
					bufferOutputWriter.close();
				if (outputFileWriter != null)
					outputFileWriter.close();
			} catch (IOException e) {
				log.error("关闭流失败", e);
			}
		}
	}

	/**
	 * 写信号文件
	 */
	public static synchronized void writeSignalFile(String midName, String tableName, String sqlQuery, StringBuilder
			allColumns, StringBuilder allType, StringBuilder lengths, String is_fixed_extract, String fixed_separator,
	                                                long lineCounter, long collect_database_size,
	                                                String eltDate, String charset) {
		BufferedWriter bufferOutputWriter = null;
		OutputStreamWriter outputFileWriter = null;
		String create_date = DateUtil.getSysDate();
		String create_time = DateUtil.getSysTime();
		String signalFile = midName + ".flg";
		String fileName = tableName + "_" + eltDate + ".flg";
		try {
			File file = new File(signalFile);
			outputFileWriter = new OutputStreamWriter(new FileOutputStream(file), charset);
			bufferOutputWriter = new BufferedWriter(outputFileWriter, 4096);
			StringBuilder sb = new StringBuilder();
			sb.append(fileName).append(" ").append(collect_database_size).append(" ").append(lineCounter)
					.append(" ").append(create_date).append(" ")
					.append(create_time).append("\n\n");
			sb.append("FILENAME=").append(fileName).append("\n\n");
			sb.append("FILESIZE=").append(collect_database_size).append("\n\n");
			sb.append("ROWCOUNT=").append(lineCounter).append("\n\n");
			sb.append("CREATEDATETIME=").append(create_date).append(" ").append(create_time).append("\n\n");
			if (FileFormat.DingChang.getCode().equals(is_fixed_extract)) {
				sb.append("IS_FIXED_LENGTH=").append("YES").append("\n\n");
			} else {
				sb.append("IS_FIXED_LENGTH=").append("NO").append("\n\n");
			}
			sb.append("SEPARATOR=").append(fixed_separator).append("\n\n");
			sb.append("SQL=").append(sqlQuery).append("\n\n");
			int RowLength = 0;
			List<String> cols_length = StringUtil.split(lengths.toString(), "^");
			for (String length : cols_length) {
				RowLength += Integer.parseInt(length);
			}
			sb.append("ROWLENGTH=").append(RowLength).append("\n\n");
			sb.append("COLUMNCOUNT=").append(cols_length.size()).append("\n\n");
			sb.append("COLUMNDESCRIPTION=").append("\n");
			for (int i = 0; i < cols_length.size(); i++) {
				List<String> columns = StringUtil.split(allColumns.toString(), "^");
				List<String> types = StringUtil.split(allType.toString(), "^");
				if (StringUtil.isEmpty(fixed_separator)) {
					int start = 0;
					int end;
					if (i > 0) {
						for (int j = 0; j < i; j++) {
							start += Integer.parseInt(cols_length.get(j));
						}
					}
					start = start + 1;
					end = start + Integer.parseInt(cols_length.get(i)) - 1;
					sb.append(i + 1).append("$$").append(columns.get(i)).append("$$").append(types.get(i)).
							append("$$").append("(").append(start).append(",").append(end).append(")").append("\n");
				} else {
					sb.append(i + 1).append("$$").append(columns.get(i)).append("$$").append(types.get(i)).append("\n");
				}
			}
			bufferOutputWriter.write(sb.toString() + "\n");
			bufferOutputWriter.flush();
		} catch (Exception e) {
			log.error("写信号文件失败", e);
		} finally {
			try {
				if (bufferOutputWriter != null)
					bufferOutputWriter.close();
				if (outputFileWriter != null)
					outputFileWriter.close();
			} catch (IOException e) {
				log.error("关闭流失败", e);
			}
		}
	}
}
