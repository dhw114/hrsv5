package hrds.g.biz.interfaceinfo.impl;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.SqlOperator;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.RequestUtil;
import hrds.commons.base.BaseAction;
import hrds.commons.codes.CollectType;
import hrds.commons.codes.DataSourceType;
import hrds.commons.codes.IsFlag;
import hrds.commons.entity.*;
import hrds.commons.utils.DruidParseQuerySql;
import hrds.commons.utils.PropertyParaValue;
import hrds.g.biz.bean.*;
import hrds.g.biz.commons.FileDownload;
import hrds.g.biz.commons.LocalFile;
import hrds.g.biz.enumerate.AsynType;
import hrds.g.biz.enumerate.OutType;
import hrds.g.biz.enumerate.StateType;
import hrds.g.biz.init.InterfaceManager;
import hrds.g.biz.interfaceinfo.InterfaceDefine;
import hrds.g.biz.interfaceinfo.common.InterfaceCommon;
import hrds.g.biz.interfaceinfo.query.Query;
import hrds.g.biz.interfaceinfo.query.QueryByRowkey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DocClass(desc = "接口信息实现类", author = "dhw", createdate = "2020/3/30 15:39")
public class InterfaceImplAction extends BaseAction implements InterfaceDefine {

	private static final Logger logger = LogManager.getLogger();
	// 对于SQL的字段是否使用字段验证
	private static final String AUTHORITY = PropertyParaValue.getString("restAuthority", "");
	// 有效结束日期
	public static final String END_DATE = "99991231";

	@Method(desc = "获取token值",
			logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
					"2.获取token值")
	@Param(name = "user_id", desc = "用户ID", range = "新增用户时生成")
	@Param(name = "user_password", desc = "密码", range = "新增用户时生成")
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> getToken(String user_id, String user_password) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.获取token值
		return InterfaceCommon.getTokenById(user_id, user_password);
	}

//	@Override
//	public Map<String, Object> marketTablePagingQuery(MarketPagingQuery marketPagingQuery,
//	                                                  CheckParam checkParam) {
//		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
//		// 2.token，接口权限检查
//		Map<String, Object> responseMap = checkAsynAndTokenInterface(checkParam,
//				marketPagingQuery.getOutType(), marketPagingQuery.getAsynType(),
//				marketPagingQuery.getBackurl(), marketPagingQuery.getFilename(),
//				marketPagingQuery.getFilepath());
//		// 3.如果responseMap响应状态不为normal返回错误响应信息
//		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
//			return responseMap;
//		}
//		try {
//			if (!DataType.isDataType(marketPagingQuery.getDataType())) {
//				return StateType.getResponseInfo(StateType.DATA_TYPE_ERROR);
//			}
//			String sql = marketPagingQuery.getSql();
//			if (StringUtil.isBlank(sql)) {
//				return StateType.getResponseInfo(StateType.SQL_IS_INCORRECT);
//			}
//			if (StringUtil.isNotBlank(marketPagingQuery.getStartRow()) &&
//					StringUtil.isNotBlank(marketPagingQuery.getRowNum())) {
//				// hrsrow_num的过滤sql
//				StringBuilder formatSql = new StringBuilder();
//				sql = sql.replaceAll("(?i)select", "SELECT")
//						.replaceAll("(?i)from", "FROM").trim();
//				formatSql.append("select max(hrsrow_num) as hrsrow_num from (");
//				formatSql.append(sql.substring(0, 6));
//				// 获取第一个SQL的FROM的下标
//				int fromIndex = formatSql.indexOf("FROM");
//				formatSql.append(" hrsrow_num ").append(formatSql.substring(fromIndex))
//						.append(" order by hrsrow_num limit ");
//				int rowNum = Integer.parseInt(marketPagingQuery.getRowNum());
//				int startRow = Integer.parseInt(marketPagingQuery.getStartRow());
//				formatSql.append(rowNum * startRow).append(" )");
//				new ProcessingData() {
//					@Override
//					public void dealLine(Map<String, Object> map) throws Exception {
//
//					}
//				}.getSQLEngine(formatSql.toString(), Dbo.db());
//				/*
//				 * 根据sql的情况，获取该使用什么查询引擎
//				 */
//			}
////			StringBuilder sqlBuilder = new StringBuilder();
////			sqlBuilder.append(sql).append("  WHERE hrsrow_num >= ");
//			return null;
//		} catch (Exception e) {
//			if (e instanceof BusinessException) {
//				return StateType.getResponseInfo(StateType.EXCEPTION.getCode(), e.getMessage());
//			}
//			return StateType.getResponseInfo((StateType.EXCEPTION));
//		}
//

	@Method(desc = "表使用权限查询", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.校验接口是否有效,返回响应状态信息" +
			"3.如果响应状态不是normal返回错误响应信息" +
			"4.正常响应信息，返回有使用权限的表")
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> tableUsePermissionsQuery(CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.校验接口是否有效,返回响应状态信息
		Map<String, Object> responseMap = InterfaceCommon.checkTokenAndInterface(checkParam);
		// 3.如果响应状态不是normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 4.正常响应信息，返回有使用权限的表
		return StateType.getResponseInfo(StateType.NORMAL.getCode(),
				InterfaceManager.getTableList(checkParam.getUser_id()));
	}

	@Method(desc = "单表普通查询", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.token，接口权限检查" +
			"3.如果responseMap响应状态不为normal返回错误响应信息" +
			"4.检查表信息" +
			"5.返回按类型操作接口响应信息")
	@Param(name = "singleTable", desc = "单表普通查询参数实体", range = "无限制", isBean = true)
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> singleTableGeneralQuery(SingleTable singleTable, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.token，接口权限检查
		Map<String, Object> responseMap = InterfaceCommon.checkAsynAndTokenInterface(checkParam,
				singleTable.getOutType(), singleTable.getAsynType(), singleTable.getBackurl(),
				singleTable.getFilename(), singleTable.getFilepath());
		// 3.如果responseMap响应状态不为normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 4.检查表信息
		String user_id = InterfaceManager.getUserByToken(responseMap.get("token").toString()).getUser_id();
		responseMap = InterfaceCommon.checkTable(user_id, singleTable);
		// 5.返回按类型操作接口响应信息
		return InterfaceCommon.operateInterfaceByType(singleTable.getDataType(), singleTable.getOutType(),
				singleTable.getAsynType(), singleTable.getBackurl(), singleTable.getFilepath(),
				singleTable.getFilename(), responseMap);
	}

//	@Method(desc = "单表索引查询接口", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
//			"2.token、接口权限检查" +
//			"3.如果responseMap响应状态不为normal返回错误响应信息" +
//			"4.检查表信息" +
//			"5.返回按类型操作接口响应信息")
//	@Param(name = "singleTable", desc = "单表普通查询参数实体", range = "无限制", isBean = true)
//	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
//	@Return(desc = "返回接口响应信息", range = "无限制")
//	@Override
//	public Map<String, Object> singleTableIndexQuery(SingleTable singleTable, CheckParam checkParam) {
//		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
//		// 2.token、接口权限检查
//		Map<String, Object> responseMap = checkAsynAndTokenInterface(checkParam, singleTable.getOutType(),
//				singleTable.getAsynType(), singleTable.getBackurl(), singleTable.getFilename(),
//				singleTable.getFilepath());
//		// 3.如果responseMap响应状态不为normal返回错误响应信息
//		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
//			return responseMap;
//		}
//		// 4.检查表信息
//		responseMap = checkTable(checkParam.getUser_id(), singleTable.getTable(),
//				singleTable.getWhereColumn(), singleTable.getSelectColumn(), singleTable.getNum(),
//				"phoenix", new String[]{}, singleTable.getDataType(), singleTable.getOutType(),
//				"");
//		// 5.返回按类型操作接口响应信息
//		return operateInterfaceByType(singleTable.getDataType(), singleTable.getOutType(),
//				singleTable.getAsynType(), singleTable.getBackurl(), singleTable.getFilepath(),
//				singleTable.getFilename(), responseMap);
//	}


	@Method(desc = "表结构查询接口", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.检查token以及接口是否有效" +
			"3.判断表是否有效" +
			"4.有效，根据user_id与表名获取查询接口信息" +
			"5.数据源类型为集市，关联查询数据表字段信息表以及数据表查询字段中英文信息" +
			"6.数据源类型为加工" +
			"7.数据源类型为贴源层，关联查询表对应的字段、数据库对应表、源文件属性表查询字段中英文信息" +
			"8.数据源类型为其他，查询源文件属性表信息获取字段中英文信息" +
			"9.返回接口响应信息")
	@Param(name = "tableName", desc = "要查询表名", range = "无限制")
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> tableStructureQuery(String tableName, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.检查token以及接口是否有效
		Map<String, Object> responseMap = InterfaceCommon.checkTokenAndInterface(checkParam);
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		String user_id = InterfaceManager.getUserByToken(responseMap.get("token").toString()).getUser_id();
		// 3.判断表是否有效
		if (InterfaceManager.existsTable(user_id, tableName)) {
			// 4.有效，根据user_id与表名获取查询接口信息
			QueryInterfaceInfo userTableInfo = InterfaceManager.getUserTableInfo(checkParam.getUser_id(),
					tableName);
			String type = userTableInfo.getTable_blsystem();
			String sysreg_name = userTableInfo.getSysreg_name();
			Map<String, Object> res = new HashMap<>();
			res.put("table_type", type);
			if (DataSourceType.DML == DataSourceType.ofEnumByCode(type)) {
				// 5.数据源类型为集市，关联查询数据表字段信息表以及数据表查询字段中英文信息
				List<Map<String, Object>> list = Dbo.queryList("SELECT field_en_name,field_cn_name FROM "
								+ Datatable_field_info.TableName + " dfi," + Dm_datatable.TableName + " di " +
								" WHERE dfi.datatable_id=di.datatable_id AND lower(datatable_en_name)=lower(?)"
						, sysreg_name);
				res.put("field", list);
			} else if (DataSourceType.DPL == DataSourceType.ofEnumByCode(type)) {
				// 6.数据源类型为加工 fixme 待开发
			} else if (DataSourceType.DCL == DataSourceType.ofEnumByCode(type)) {
				// 7.数据源类型为贴源层，关联查询表对应的字段、数据库对应表、源文件属性表查询字段中英文信息
				// fixme 这里只是获取批量表结构，实时表结构需要考虑吗？
				List<Map<String, Object>> list = Dbo.queryList(
						"SELECT column_name as field_en_name,column_ch_name as field_cn_name FROM "
								+ Table_column.TableName + "  tc join " + Table_info.TableName + " ti ON " +
								"tc.table_id = ti.table_id join " + Data_store_reg.TableName +
								" dsr ON dsr.table_name = ti.table_name " +
								" WHERE dsr.database_id = ti.database_id and lower(ds.hyren_name)=lower(?) "
								+ " and ti.valid_e_date=? AND tc.is_get=? and is_alive=?",
						sysreg_name, END_DATE, IsFlag.Shi.getCode(), IsFlag.Shi.getCode());
				res.put("field", list);
			} else {
				// 8.数据源类型为其他，查询源文件属性表信息获取字段中英文信息  fixme 待开发
//				List<Object> metaInfoList = Dbo.queryOneColumnList("SELECT meta_info FROM "
//						+ Source_file_attribute.TableName + " WHERE lower(hbase_name) = lower(?)", sysreg_name);
//				List<Map<String, String>> list = new ArrayList<>();
//				for (Object metaInfo : metaInfoList) {
//					Map<String, String> metaInfoMap = JsonUtil.toObject(metaInfo.toString(), mapType);
//					String column = metaInfoMap.get("column");
//					String[] split = column.split(",");
//					for (String field : split) {
//						Map<String, String> map = new HashMap<>();
//						map.put("field_en_name", field);
//						list.add(map);
//					}
//				}
//				res.put("field", list);
			}
			// 9.返回接口响应信息
			return StateType.getResponseInfo(StateType.NORMAL.getCode(), res);
		} else {
			return StateType.getResponseInfo(StateType.EXCEPTION.getCode(), "没有对应的表，请确认后尝试");
		}
	}

	@Method(desc = "文件属性搜索接口", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.token、接口权限检查" +
			"3.如果responseMap响应状态不为normal返回错误响应信息" +
			"4.定义显示条数默认值，文件大小范围默认值" +
			"5.判断显示条数是否为空，不为空处理数据获取显示条数以及其范围值" +
			"6.判断文件大小值是否为空，如果不为空处理数据获取文件大小范围值" +
			"7.判断文件大小是否为空，不为空加条件查询" +
			"8.判断文件后缀名是否为空，不为空加条件查询" +
			"9.判断采集任务路径是否为空，不为空加条件查询" +
			"10.判断采集任务id是否为空，不为空加条件查询" +
			"11.判断部门ID是否为空，不为空加条件查询" +
			"12.设置分页" +
			"13.关联查询data_source、file_collect_set、data_store_reg三张表获取文件信息" +
			"14.获取摘要" +
			"15.判断文件属性信息是否为空，为空返回空集合，否则返回文件属性信息集合")
	@Param(name = "fileAttribute", desc = "文件屬性参数实体", range = "无限制", isBean = true)
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> fileAttributeSearch(FileAttribute fileAttribute, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.token、接口权限检查
		Map<String, Object> responseMap = InterfaceCommon.checkTokenAndInterface(checkParam);
		// 3.如果responseMap响应状态不为normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 4.定义显示条数默认值，文件大小范围默认值
		int num_start = 0, num_count = 10, fileSizeStart = 0, fileSizeEnd = 0;
		// 5.判断显示条数是否为空，不为空处理数据获取显示条数以及其范围值
		String num = fileAttribute.getNum();
		if (StringUtil.isNotBlank(num)) {
			if (num.contains(",")) {
				List<String> numList = StringUtil.split(num, ",");
				num_start = Integer.parseInt(numList.get(0));
				num_count = Integer.parseInt(numList.get(1));
			} else {
				num_count = Integer.parseInt(num);
			}
		}
		// 6.判断文件大小值是否为空，如果不为空处理数据获取文件大小范围值
		String fileSize = fileAttribute.getFilesize();
		if (StringUtil.isNotBlank(fileSize)) {
			if (fileSize.contains(",")) {
				List<String> fileSizeList = StringUtil.split(fileSize, ",");
				try {
					fileSizeStart = Integer.parseInt(fileSizeList.get(0));
					fileSizeEnd = Integer.parseInt(fileSizeList.get(1));
				} catch (NumberFormatException e) {
					return StateType.getResponseInfo(StateType.EXCEPTION.getCode(),
							"输入的文件大小不合法请确认");
				}
			} else {
				try {
					fileSizeStart = Integer.parseInt(fileSize);
				} catch (NumberFormatException e) {
					return StateType.getResponseInfo(StateType.EXCEPTION.getCode(),
							"输入的文件大小不合法请确认");
				}
			}
		}
		SqlOperator.Assembler assembler = SqlOperator.Assembler.newInstance();
		assembler.addSql("SELECT source_path,file_suffix,file_id,storage_time,storage_date,original_update_date,"
				+ " original_update_time,file_md5,original_name,file_size,file_avro_path,file_avro_block,"
				+ " sfa.collect_set_id,sfa.source_id,sfa.agent_id,fcs_name,datasource_name,agent_name FROM  "
				+ Data_source.TableName + "  ds JOIN agent_info ai ON ds.SOURCE_ID = ai.SOURCE_ID"
				+ " JOIN " + File_collect_set.TableName + " fcs ON fcs.agent_id = ai.agent_id"
				+ " JOIN " + Source_file_attribute.TableName + " sfa ON sfa.SOURCE_ID = ds.SOURCE_ID"
				+ " and  sfa.AGENT_ID = ai.AGENT_ID and sfa.collect_set_id = fcs.FCS_ID "
				+ " where collect_type = ? ");
		assembler.addParam(CollectType.WenJianCaiJi.getCode());
		assembler.addLikeParam("original_name", fileAttribute.getFilename());
		List<Object> sourceIdList = Dbo.queryOneColumnList("select source_id from data_source ");
		assembler.addORParam("sfa.source_id", sourceIdList.toArray());
		// 7.判断文件大小是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(fileSize)) {
			assembler.addSql(" and file_size >=").addParam(fileSizeStart);
			assembler.addSql(" and ile_size <=").addParam(fileSizeEnd);
		}
		// 8.判断文件后缀名是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(fileAttribute.getFilesuffix())) {
			String[] split = fileAttribute.getFilesuffix().split(",");
			assembler.addORParam("file_suffix", split);
		}
		// 9.判断采集任务路径是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(fileAttribute.getFilepath())) {
			String[] split = fileAttribute.getFilepath().split(",");
			assembler.addORParam("source_path", split);
		}
		// 10.判断采集任务id是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(fileAttribute.getFcs_id())) {
			String[] split = fileAttribute.getFcs_id().split(",");
			assembler.addORParam("fcs_id", split);
		}
		assembler.addSql("storage_date=?").addParam(fileAttribute.getStoragedate());
		assembler.addSql("file_md5=?").addParam(fileAttribute.getFileMD5());
		assembler.addLikeParam("datasource_name", fileAttribute.getDs_name());
		assembler.addLikeParam("agent_name", fileAttribute.getAgent_name());
		assembler.addLikeParam("fcs_name", fileAttribute.getFcs_name());
		// 11.判断部门ID是否为空，不为空加条件查询
		if (StringUtil.isNotBlank(fileAttribute.getDep_id())) {
			String[] dep_ids = fileAttribute.getDep_id().split(",");
			assembler.addSql("and  exists (select source_id from " + Source_relation_dep.TableName +
					" dep where dep.SOURCE_ID = ds.SOURCE_ID ");
			assembler.addORParam("dep_id", dep_ids);
			assembler.addSql(" ) ");
		}
		// 12.设置分页
		assembler.addSql("limit " + num_count + " offset " + num_start);
		// 13.关联查询data_source、file_collect_set、data_store_reg三张表获取文件信息
		List<Map<String, Object>> fileAttrList = Dbo.queryList(assembler.sql(), assembler.params());

		// 14.获取摘要 fixme 待开发
		// 15.判断文件属性信息是否为空，为空返回空集合，否则返回文件属性信息集合
		if (fileAttrList.isEmpty()) {
			return StateType.getResponseInfo(StateType.NORMAL.getCode(), new ArrayList<>());
		}
		return StateType.getResponseInfo(StateType.NORMAL.getCode(), fileAttrList);
	}

	@Method(desc = "sql查询接口", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.token、接口权限检查" +
			"3.如果responseMap响应状态不为normal返回错误响应信息" +
			"4.获取当前用户ID" +
			"5.检查datatype，outtype参数是否合法" +
			"6.检查sql是否正确" +
			"7.根据sql获取表名的集合" +
			"8.校验表权限" +
			"9.获取表的有效列信息" +
			"10.如果为某些特定的用户,则不做字段的检测" +
			"11.使用sql解析获取列" +
			"12.判断查询列是否存在，支持t1.*,t2.*" +
			"13.存在，遍历列集合，判断列是否包含.,包含.说明是有别名获取别名后的列名称，否则直接获取列名称" +
			"14.判断列是否有权限" +
			"15.判断sql是否是以；结尾，如果是删除" +
			"16.根据sql获取搜索引擎并根据输出数据类型处理数据")
	@Param(name = "sqlSearch", desc = "sql查询参数实体", range = "无限制", isBean = true)
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> sqlSearch(SqlSearch sqlSearch, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.token、接口权限检查
		Map<String, Object> responseMap = InterfaceCommon.checkAsynAndTokenInterface(checkParam, sqlSearch.getOutType(),
				sqlSearch.getAsynType(), sqlSearch.getBackurl(), sqlSearch.getFilename(), sqlSearch.getFilepath());
		// 3.如果responseMap响应状态不为normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 4.获取当前用户ID
		String user_id = InterfaceManager.getUserByToken(responseMap.get("token").toString()).getUser_id();
		// 5.检查datatype，outtype参数是否合法
		responseMap = InterfaceCommon.checkTypeParams(sqlSearch.getDataType(), sqlSearch.getOutType());
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 6.检查sql是否正确
		if (StringUtil.isBlank(sqlSearch.getSql())) {
			return StateType.getResponseInfo(StateType.SQL_IS_INCORRECT);
		}
		// 7.根据sql获取表名的集合
		List<String> tableList = DruidParseQuerySql.parseSqlTableToList(sqlSearch.getSql());
		List<String> columnList = new ArrayList<>();
		for (String table : tableList) {
			// 8.校验表权限
			responseMap = InterfaceCommon.verifyTable(user_id, table);
			if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
				return responseMap;
			}
			// 9.获取表的有效列信息
			QueryInterfaceInfo userTableInfo = InterfaceManager.getUserTableInfo(user_id, table);
			columnList = StringUtil.split(userTableInfo.getTable_column_name().toLowerCase(), ",");
		}
		// 10.如果为某些特定的用户,则不做字段的检测
		if (!AUTHORITY.contains(checkParam.getUser_id())) {
			// 11.使用sql解析获取列
			DruidParseQuerySql druidParseQuerySql = new DruidParseQuerySql(sqlSearch.getSql());
			List<String> sqlColumnList = druidParseQuerySql.parseSelectOriginalField();
			if (!columnList.isEmpty()) {
				// 12.判断查询列是否存在，支持t1.*,t2.*
				if (sqlColumnList.size() > 0 && !sqlColumnList.contains("*")) {
					// 13.存在，遍历列集合，判断列是否包含.,包含.说明是有别名获取别名后的列名称，否则直接获取列名称
					for (String col : sqlColumnList) {
						if (col.contains(".")) {
							col = col.substring(col.indexOf(".") + 1).toLowerCase();
						} else {
							col = col.toLowerCase();
						}
						// 14.判断列是否有权限
						if (!InterfaceCommon.colIsExist(col, columnList)) {
							return StateType.getResponseInfo(StateType.COLUMN_DOES_NOT_EXIST.getCode(),
									"请求错误,查询列名" + col + "不存在");
						}
					}
				}
			}
		}
		// 15.判断sql是否是以；结尾，如果是删除
		String sqlNew = sqlSearch.getSql().trim();
		if (sqlNew.endsWith(";")) {
			sqlNew = sqlNew.substring(0, sqlNew.length() - 1);
		}
		// 16.根据sql获取搜索引擎并根据输出数据类型处理数据
		return InterfaceCommon.getSqlData(sqlSearch.getOutType(), sqlSearch.getDataType(), sqlNew);
	}

	@Method(desc = "rowkey查询", logicStep = "1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制" +
			"2.token、接口权限检查" +
			"3.如果responseMap响应状态不为normal返回错误响应信息" +
			"" +
			"" +
			"" +
			"")
	@Param(name = "rowKeySearch", desc = "rowkey查询参数实体", range = "无限制", isBean = true)
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> rowKeySearch(RowKeySearch rowKeySearch, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.token、接口权限检查
		Map<String, Object> responseMap = InterfaceCommon.checkAsynAndTokenInterface(checkParam,
				rowKeySearch.getOutType(), rowKeySearch.getAsynType(), rowKeySearch.getBackurl(),
				rowKeySearch.getFilename(), rowKeySearch.getFilepath());
		// 3.如果responseMap响应状态不为normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		// 4.根据rowkey，表名称、数据版本号获取hbase表信息,如果返回状态信息不为normal则返回错误响应信息
		Query queryByRK = new QueryByRowkey(rowKeySearch.getEnTable(), rowKeySearch.getRowkey(),
				rowKeySearch.getEnColumn(), rowKeySearch.getVersion());
		Map<String, Object> feedback = queryByRK.query().feedback();
		if (StateType.NORMAL != StateType.ofEnumByCode(feedback.get("status").toString())) {
			return feedback;
		}
		String user_id = InterfaceManager.getUserByToken(responseMap.get("token").toString()).getUser_id();
		// 5.将数据写成对应的数据文件
		LocalFile.writeFile(feedback, rowKeySearch.getDataType(), rowKeySearch.getOutType(), user_id);
		// 异步回调、轮询
		if (OutType.FILE == OutType.ofEnumByCode(rowKeySearch.getOutType())) {
			// 判断是同步还是异步回调或者异步轮询
			if (AsynType.ASYNCALLBACK == AsynType.ofEnumByCode(rowKeySearch.getAsynType())) {
				// 异步回调
				return InterfaceCommon.checkBackUrl(responseMap, rowKeySearch.getBackurl());
			} else if (AsynType.ASYNPOLLING == AsynType.ofEnumByCode(rowKeySearch.getAsynType())) {
				// 轮询
				return InterfaceCommon.createFile(responseMap, rowKeySearch.getFilepath(),
						rowKeySearch.getFilename());
			}
		}
		responseMap.put("enTable", rowKeySearch.getEnTable());
		return responseMap;
	}


	@Method(desc = "Solr查询Hbase数据接口", logicStep = "")
	@Param(name = "hBaseSolr", desc = "HBaseSolr查询参数实体", range = "无限制", isBean = true)
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> hBaseSolrQuery(HBaseSolr hBaseSolr, CheckParam checkParam) {
		return null;
	}

	@Method(desc = "UUID数据下载", logicStep = "")
	@Param(name = "uuid", desc = "uuid", range = "无限制")
	@Param(name = "checkParam", desc = "接口检查参数实体", range = "无限制", isBean = true)
	@Return(desc = "返回接口响应信息", range = "无限制")
	@Override
	public Map<String, Object> uuidDownload(String uuid, CheckParam checkParam) {
		// 1.数据可访问权限处理方式：该方法通过user_id进行访问权限限制
		// 2.token、接口权限检查
		Map<String, Object> responseMap = InterfaceCommon.checkTokenAndInterface(checkParam);
		// 3.如果responseMap响应状态不为normal返回错误响应信息
		if (StateType.NORMAL != StateType.ofEnumByCode(responseMap.get("status").toString())) {
			return responseMap;
		}
		FileDownload fileDownload = new FileDownload();
		try {
			if (StringUtil.isNotBlank(uuid)) {
				HttpServletResponse response = fileDownload.downLoadFile(uuid, checkParam.getUser_id());
				if (response.getStatus() < 300) {
					return StateType.getResponseInfo(StateType.NORMAL.getCode(), "下载成功");
				} else {
					return StateType.getResponseInfo(StateType.EXCEPTION.getCode(), "下载失败");
				}
			} else {
				return StateType.getResponseInfo(StateType.UUID_NOT_NULL);
			}
		} catch (Exception e) {
			logger.error(e);
			return StateType.getResponseInfo(StateType.EXCEPTION.getCode(), "下载失败");
		}
	}

	@Method(desc = "获取当前用户请求ip端口、请求地址，接口代码参数",
			logicStep = "1.数据可访问权限处理方式：该方法不需要进行访问权限限制" +
					"2.查询当前用户接口使用信息" +
					"3.封装当前用户请求ip端口" +
					"4.返回当前用户请求ip端口、请求地址，接口代码参数")
	@Param(name = "interface_use_id", desc = "接口使用ID", range = "新增接口使用信息时生成")
	@Return(desc = "返回当前用户请求ip端口、请求地址，接口代码参数", range = "无限制")
	public Map<String, Object> getInterfaceUseParam(Long interface_use_id) {
		// 1.数据可访问权限处理方式：该方法不需要进行访问权限限制
		// 2.查询当前用户接口使用信息
		Map<String, Object> useMap = Dbo.queryOneObject("SELECT url,interface_code FROM "
						+ Interface_use.TableName + " WHERE interface_use_id = ? AND user_id = ?",
				interface_use_id, getUserId());
		// 3.封装当前用户请求ip端口
		useMap.put("ipAndPort", RequestUtil.getRequest().getLocalAddr() + ":"
				+ RequestUtil.getRequest().getLocalPort());
		// 4.返回当前用户请求ip端口、请求地址，接口代码参数
		return useMap;
	}


}

