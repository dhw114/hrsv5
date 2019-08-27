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
@Table(tableName = "ml_gbrtmodel")
public class MlGbrtmodel extends TableEntity {
    private static final long serialVersionUID = 321566870187324L;
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "ml_gbrtmodel";

	static {
		Set<String> __tmpPKS = new HashSet<>();
		__tmpPKS.add("model_id");
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
	}
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

	private String model_runstate;
	private String create_time;
	private BigDecimal max_depth;
	private String remark;
	private BigDecimal dtable_info_id;
	private BigDecimal model_id;
	private BigDecimal n_estimators;
	private BigDecimal random_state;
	private String model_path;
	private String model_name;
	private BigDecimal max_leaf_nodes;
	private String dv_column;
	private String create_date;
	private BigDecimal learning_rate;

	public String getModel_runstate() { return model_runstate; }
	public void setModel_runstate(String model_runstate) {
		if(model_runstate==null) throw new BusinessException("Entity : MlGbrtmodel.model_runstate must not null!");
		this.model_runstate = model_runstate;
	}

	public String getCreate_time() { return create_time; }
	public void setCreate_time(String create_time) {
		if(create_time==null) throw new BusinessException("Entity : MlGbrtmodel.create_time must not null!");
		this.create_time = create_time;
	}

	public BigDecimal getMax_depth() { return max_depth; }
	public void setMax_depth(BigDecimal max_depth) {
		if(max_depth==null) throw new BusinessException("Entity : MlGbrtmodel.max_depth must not null!");
		this.max_depth = max_depth;
	}

	public String getRemark() { return remark; }
	public void setRemark(String remark) {
		if(remark==null) addNullValueField("remark");
		this.remark = remark;
	}

	public BigDecimal getDtable_info_id() { return dtable_info_id; }
	public void setDtable_info_id(BigDecimal dtable_info_id) {
		if(dtable_info_id==null) throw new BusinessException("Entity : MlGbrtmodel.dtable_info_id must not null!");
		this.dtable_info_id = dtable_info_id;
	}

	public BigDecimal getModel_id() { return model_id; }
	public void setModel_id(BigDecimal model_id) {
		if(model_id==null) throw new BusinessException("Entity : MlGbrtmodel.model_id must not null!");
		this.model_id = model_id;
	}

	public BigDecimal getN_estimators() { return n_estimators; }
	public void setN_estimators(BigDecimal n_estimators) {
		if(n_estimators==null) throw new BusinessException("Entity : MlGbrtmodel.n_estimators must not null!");
		this.n_estimators = n_estimators;
	}

	public BigDecimal getRandom_state() { return random_state; }
	public void setRandom_state(BigDecimal random_state) {
		if(random_state==null) addNullValueField("random_state");
		this.random_state = random_state;
	}

	public String getModel_path() { return model_path; }
	public void setModel_path(String model_path) {
		if(model_path==null) addNullValueField("model_path");
		this.model_path = model_path;
	}

	public String getModel_name() { return model_name; }
	public void setModel_name(String model_name) {
		if(model_name==null) throw new BusinessException("Entity : MlGbrtmodel.model_name must not null!");
		this.model_name = model_name;
	}

	public BigDecimal getMax_leaf_nodes() { return max_leaf_nodes; }
	public void setMax_leaf_nodes(BigDecimal max_leaf_nodes) {
		if(max_leaf_nodes==null) addNullValueField("max_leaf_nodes");
		this.max_leaf_nodes = max_leaf_nodes;
	}

	public String getDv_column() { return dv_column; }
	public void setDv_column(String dv_column) {
		if(dv_column==null) throw new BusinessException("Entity : MlGbrtmodel.dv_column must not null!");
		this.dv_column = dv_column;
	}

	public String getCreate_date() { return create_date; }
	public void setCreate_date(String create_date) {
		if(create_date==null) throw new BusinessException("Entity : MlGbrtmodel.create_date must not null!");
		this.create_date = create_date;
	}

	public BigDecimal getLearning_rate() { return learning_rate; }
	public void setLearning_rate(BigDecimal learning_rate) {
		if(learning_rate==null) throw new BusinessException("Entity : MlGbrtmodel.learning_rate must not null!");
		this.learning_rate = learning_rate;
	}

}