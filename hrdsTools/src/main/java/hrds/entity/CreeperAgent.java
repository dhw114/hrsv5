package hrds.entity;

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import hrds.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体类中所有属性都应定义为对象，不要使用int等主类型，方便对null值的操作
 */
@Table(tableName = "creeper_agent")
public class CreeperAgent extends TableEntity {
    private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "creeper_agent";

	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("agent_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	private String agent_ip;
	private String file_path;
	private BigDecimal cmsg_id;
	private BigDecimal agent_id;
	private BigDecimal create_id;
	private String agent_name;
	private String create_time;
	private BigDecimal user_id;
	private String agent_status;
	private String agent_port;
	private String create_date;

	public String getAgent_ip() { return agent_ip; }
	public void setAgent_ip(String agent_ip) {
		if(agent_ip==null) throw new BusinessException("Entity : CreeperAgent.agent_ip must not null!");
		this.agent_ip = agent_ip;
	}

	public String getFile_path() { return file_path; }
	public void setFile_path(String file_path) {
		if(file_path==null) addNullValueField("file_path");
		this.file_path = file_path;
	}

	public BigDecimal getCmsg_id() { return cmsg_id; }
	public void setCmsg_id(BigDecimal cmsg_id) {
		if(cmsg_id==null) throw new BusinessException("Entity : CreeperAgent.cmsg_id must not null!");
		this.cmsg_id = cmsg_id;
	}

	public BigDecimal getAgent_id() { return agent_id; }
	public void setAgent_id(BigDecimal agent_id) {
		if(agent_id==null) throw new BusinessException("Entity : CreeperAgent.agent_id must not null!");
		this.agent_id = agent_id;
	}

	public BigDecimal getCreate_id() { return create_id; }
	public void setCreate_id(BigDecimal create_id) {
		if(create_id==null) throw new BusinessException("Entity : CreeperAgent.create_id must not null!");
		this.create_id = create_id;
	}

	public String getAgent_name() { return agent_name; }
	public void setAgent_name(String agent_name) {
		if(agent_name==null) throw new BusinessException("Entity : CreeperAgent.agent_name must not null!");
		this.agent_name = agent_name;
	}

	public String getCreate_time() { return create_time; }
	public void setCreate_time(String create_time) {
		if(create_time==null) throw new BusinessException("Entity : CreeperAgent.create_time must not null!");
		this.create_time = create_time;
	}

	public BigDecimal getUser_id() { return user_id; }
	public void setUser_id(BigDecimal user_id) {
		if(user_id==null) throw new BusinessException("Entity : CreeperAgent.user_id must not null!");
		this.user_id = user_id;
	}

	public String getAgent_status() { return agent_status; }
	public void setAgent_status(String agent_status) {
		if(agent_status==null) throw new BusinessException("Entity : CreeperAgent.agent_status must not null!");
		this.agent_status = agent_status;
	}

	public String getAgent_port() { return agent_port; }
	public void setAgent_port(String agent_port) {
		if(agent_port==null) addNullValueField("agent_port");
		this.agent_port = agent_port;
	}

	public String getCreate_date() { return create_date; }
	public void setCreate_date(String create_date) {
		if(create_date==null) throw new BusinessException("Entity : CreeperAgent.create_date must not null!");
		this.create_date = create_date;
	}

}