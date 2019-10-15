package hrds.agent.job.biz.core.dbstage.dbdialect.strategy;

import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.annotation.Return;
import fd.ng.db.jdbc.nature.MySQL;

/**
 * ClassName: MySQLDialectStrategy <br/>
 * Function: 数据库方言策略接口MySQL数据库实现类. <br/>
 * Reason:
 * Date: 2019/8/1 15:24 <br/>
 * <p>
 * Author WangZhengcheng
 * Version 1.0
 * Since JDK 1.8
 **/
public class MySQLDialectStrategy implements DataBaseDialectStrategy {
	@Method(desc = "MySQL根据对应的数据库分页类型，获取分页SQL", logicStep = "1、调用封装好的已有实现获得分页SQL")
	@Param(name = "strSql", desc = "采集SQL", range = "不为空")
	@Param(name = "start", desc = "当前页开始条数", range = "不限")
	@Param(name = "limit", desc = "当前页结束条数", range = "不限")
	@Return(desc = "根据参数组装成的用于分页查询的SQL", range = "不会为null")
	@Override
	public String createPageSql(String strSql, int start, int limit) {
		return MySQL.toPagedSql(strSql, start, limit).getSql();
	}
}
