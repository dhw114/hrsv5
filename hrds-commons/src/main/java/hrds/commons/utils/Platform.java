package hrds.commons.utils;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.meta.ColumnMeta;
import fd.ng.db.meta.MetaOperator;
import fd.ng.db.meta.TableMeta;
import hrds.commons.codes.DatabaseType;
import hrds.commons.codes.IsFlag;
import hrds.commons.codes.StorageTypeKey;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.xlstoxml.XmlCreater;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

public class Platform {

	private static final Log logger = LogFactory.getLog(Platform.class);
	private static XmlCreater xmlCreater = null;
	public static Element table = null;
	public static Element root = null;
	public static Element column = null;

	private static void createXml(String path, String name) {
		xmlCreater = new XmlCreater(path);
		root = xmlCreater.createRootElement("database");
		xmlCreater.createAttribute(root, "xmlns", "http://db.apache.org/ddlutils/schema/1.1");
		xmlCreater.createAttribute(root, "name", name);
	}

	private static void openXml(String path) {

		xmlCreater = new XmlCreater(path);
		xmlCreater.open();
		root = xmlCreater.getElement();
	}

	private static void addTable(String en_table_name, String cn_table_name, TableMeta tableMeta) {

		table = xmlCreater.createElement(root, "table");
		xmlCreater.createAttribute(table, "table_name", en_table_name);
		xmlCreater.createAttribute(
				table, "table_ch_name", StringUtil.isEmpty(cn_table_name) ? en_table_name : cn_table_name);
//    xmlCreater.createAttribute(table, "unload_type", UnloadType.QuanLiangXieShu.getCode());
		Map<String, ColumnMeta> columnMetas = tableMeta.getColumnMetas();
		Set<String> primaryKeys = tableMeta.getPrimaryKeys();
		for (String key : columnMetas.keySet()) {
			ColumnMeta columnMeta = columnMetas.get(key);
			String colName = columnMeta.getName(); // 列名
			String typeName = columnMeta.getTypeName(); // 类型名称
			int precision = columnMeta.getLength(); // 长度
			boolean isNull = columnMeta.isNullable(); // 是否为空
			int dataType = columnMeta.getTypeOfSQL(); // 类型
			int scale = columnMeta.getScale(); // 小数的位数
			String column_type = getColType(dataType, typeName, precision, scale);
			boolean primaryKey = primaryKeys.contains(colName);
			addColumn(colName, primaryKey, column_type, typeName, precision, isNull, dataType, scale);
		}
	}

	private static void addColumn(
			String colName,
			boolean primaryKey,
			String column_type,
			String typeName,
			int precision,
			boolean isNull,
			int dataType,
			int scale) {

		column = xmlCreater.createElement(table, "column");
		xmlCreater.createAttribute(column, "column_name", colName);
		xmlCreater.createAttribute(column, "column_type", column_type);
		xmlCreater.createAttribute(column, "column_ch_name", colName);
		xmlCreater.createAttribute(
				column, "is_primary_key", primaryKey ? IsFlag.Shi.getCode() : IsFlag.Fou.getCode());
		xmlCreater.createAttribute(column, "is_get", IsFlag.Shi.getCode());
		xmlCreater.createAttribute(column, "is_alive", IsFlag.Shi.getCode());
		xmlCreater.createAttribute(column, "is_new", IsFlag.Fou.getCode());
		xmlCreater.createAttribute(column, "column_remark", "");
	}

	public static void readModelFromDatabase(DatabaseWrapper db, String xmlName) {
		try {
			// 调用方法生成xml文件
			String userName = db.getName();
			createXml(xmlName, userName);

			// 获取所有表
			List<TableMeta> tableMetas = MetaOperator.getTablesWithColumns(db);
			for (TableMeta tableMeta : tableMetas) {
				String tableName = tableMeta.getTableName();
				String remarks = tableMeta.getRemarks();
				// 添加表名
				addTable(tableName, remarks, tableMeta);
        /*
        * TODO 1、第二个参数，DB2添加%
        2、第二个参数，oracle 是否可以为  OPS$AIMSADM，也可以是getUserName().toUpperCase()都待测试。
        	oracle 貌似需要大写
        3、第二个参数，mysql可以加上面所有的都是ok的
        */
			}
			// 生成xml文档
			xmlCreater.buildXmlFile();
			logger.info("写xml信息完成");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException("获取表信息失败");
		}
	}

	public static void readModelFromDatabase(
			DatabaseWrapper db, String xmlName, String tableNameList) {
		try {
			// 调用方法生成xml文件
			String userName = db.getName();
			File file = FileUtils.getFile(xmlName);
			if (!file.exists()) {
				createXml(xmlName, userName);
			} else {
				// 写文件之前先删除以前的文件
				if (!file.delete()) {
					throw new AppSystemException("删除文件失败");
				}
				createXml(xmlName, userName);
			}
			// table是多张表
			List<String> names = StringUtil.split(tableNameList, "|");
			List<String> table = new ArrayList<>();
			for (String name : names) {
				// 获取所有表
				List<TableMeta> tableMetas = MetaOperator.getTablesWithColumns(db, "%" + name + "%");
				String tableNameTemp = "";
				for (TableMeta tableMeta : tableMetas) {
					String tableName = tableMeta.getTableName();
					String remarks = tableMeta.getRemarks();
					if (!table.contains(tableName)) {
						table.add(tableName);
						addTable(tableName, remarks, tableMeta);
					}
				}
			}
			// 生成xml文档
			xmlCreater.buildXmlFile();
			logger.info("写xml信息完成");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new BusinessException("获取表信息失败");
		}
	}

	/**
	 * 获取数据库直连的数据类型
	 *
	 * @param dataType  {@link Integer} sql.Types的数据类型
	 * @param typeName  {@link String} 数据类型
	 * @param precision {@link String} 数据长度
	 * @param scale     {@link String} 数据精度
	 */
	public static String getColType(int dataType, String typeName, int precision, int scale) {
		if (Types.CLOB == dataType || Types.BLOB == dataType) {
			return typeName;
		} else {
			// TODO 各数据库不同遇到在修改
			typeName = StringUtil.replace(typeName, "UNSIGNED", "");
			// 考虑到有些类型在数据库中获取到就会带有(),同时还能获取到数据的长度，修改方式为如果本事类型带有长度，去掉长度，使用数据库读取的长度
			if (precision != 0) {
				int ic = typeName.indexOf("(");
				if (ic != -1) {
					typeName = typeName.substring(0, ic);
				}
			}
			String column_type;
			if (Types.INTEGER == dataType
					|| Types.TINYINT == dataType
					|| Types.SMALLINT == dataType
					|| Types.BIGINT == dataType) {
				column_type = typeName;
			} else if (Types.NUMERIC == dataType
					|| Types.FLOAT == dataType
					|| Types.DOUBLE == dataType
					|| Types.DECIMAL == dataType) {
				// 1、当一个数的整数部分的长度 > p-s 时，Oracle就会报错
				// 2、当一个数的小数部分的长度 > s 时，Oracle就会舍入。
				// 3、当s(scale)为负数时，Oracle就对小数点左边的s个数字进行舍入。
				// 4、当s > p 时, p表示小数点后第s位向左最多可以有多少位数字，如果大于p则Oracle报错，小数点后s位向右的数字被舍入
				if (precision > precision - Math.abs(scale) || scale > precision || precision == 0) {
					precision = 38;
					scale = 15;
				}
				column_type = typeName + "(" + precision + "," + scale + ")";
			} else {
				if ("char".equalsIgnoreCase(typeName) && precision > 255) {
					typeName = "varchar";
				}
				column_type = typeName + "(" + precision + ")";
			}
			return column_type;
		}
	}

	public static List<Map<String, String>> getSqlColumnMeta(String sql, DatabaseWrapper db) {
		Map<String, ColumnMeta> sqlColumnMeta = MetaOperator.getSqlColumnMeta(db, sql);
		List<Map<String, String>> columnList = new ArrayList<>();
		for (String key : sqlColumnMeta.keySet()) {
			ColumnMeta metaData = sqlColumnMeta.get(key);
			Map<String, String> hashMap = new HashMap<>();
			hashMap.put("column_name", key);
			hashMap.put(
					"type",
					getColType(
							metaData.getTypeOfSQL(),
							metaData.getName(),
							metaData.getLength(),
							metaData.getScale()));
			columnList.add(hashMap);
		}
		return columnList;
	}

	public static void main(String[] args) {
		Map map = new HashMap<>();
		// 2、从想要的数据库中获取连接信息
		/*map.put(StorageTypeKey.database_type, DatabaseType.Postgresql.getCode());
		map.put(StorageTypeKey.database_driver, "org.postgresql.Driver");
		map.put(StorageTypeKey.jdbc_url,"jdbc:postgresql://10.71.4.52:31001/hrsdxgtest");
		map.put(StorageTypeKey.user_name, "hrsdxg");
		map.put(StorageTypeKey.database_pwd, "hrsdxg");*/


		map.put(StorageTypeKey.database_type, DatabaseType.Oracle10g.getCode());
		map.put(StorageTypeKey.database_driver, "oracle.jdbc.OracleDriver");
		map.put(StorageTypeKey.jdbc_url, "jdbc:oracle:thin:@47.103.83.1:1521:HYSHF");
		map.put(StorageTypeKey.user_name, "HYSHF");
		map.put(StorageTypeKey.database_pwd, "hyshf");

		DatabaseWrapper db = ConnectionTool.getDBWrapper(map);

		List<TableMeta> tableMetas = MetaOperator.getTablesWithColumns(db);
		for (TableMeta tableMeta : tableMetas) {
			String tableName = tableMeta.getTableName();
			String remarks = tableMeta.getRemarks();
		}

		//readModelFromDatabase(db,"D:\\aa.xml");

		//readModelFromDatabase(db, "D:\\aa.xml", "agent_down_info");
	}
}
