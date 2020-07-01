package hrds.testbase;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.conf.ConfFileLoader;
import fd.ng.core.exception.BusinessProcessException;
import fd.ng.core.utils.DateUtil;
import fd.ng.core.yaml.YamlArray;
import fd.ng.core.yaml.YamlFactory;
import fd.ng.core.yaml.YamlMap;
import fd.ng.db.jdbc.DatabaseWrapper;
import fd.ng.db.jdbc.SqlOperator;
import hrds.commons.codes.*;
import hrds.commons.entity.*;
import hrds.commons.utils.StorageTypeKey;
import hrds.commons.utils.key.PrimayKeyGener;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@DocClass(desc = "加载通用测试数据", author = "BY-HLL", createdate = "2020/6/29 0029 上午 10:29")
public class LoadGeneralTestData {

    //设置当前运行线程id
    private long THREAD_ID;
    //获取定义的测试配置
    private static final YamlMap testInitConfig = YamlFactory.load(ConfFileLoader.getConfFile("testinfo")).asMap();
    //获取定义的测试存储层配置
    private static final YamlArray TEST_STORAGE_LAYER_INFO_S = testInitConfig.getArray("test_storage_layer_info_s");
    //获取定义的tpcds测试数据表信息
    private static final YamlArray TPCDS_TABLE_INFO_S = testInitConfig.getArray("tpcds_table_info_s");
    //管理员用户id
    private long GENERAL_MN_USER_ID;
    //业务员用户id
    private long GENERAL_OPER_USER_ID;
    //部门id
    private long GENERAL_DEP_ID;
    //数据源id
    private long GENERAL_SOURCE_ID;
    //agent_id
    private long GENERAL_AGENT_ID;
    //采集分类id
    private long GENERAL_CLASSIFY_ID;
    //数据库设置id
    private long GENERAL_DATABASE_ID;

    //获取通用部门信息
    private Department_info DEPARTMENT_INFO = null;
    //获取通用数据源和部门关系信息
    private Source_relation_dep SOURCE_RELATION_DEP = null;
    //获取存储层表实体数据
    private List<Data_store_layer> DATA_STORE_LAYER_S = null;
    //获取初始化存储层配置信息
    private List<Data_store_layer_attr> DATA_STORE_LAYER_ATTR_S = null;
    //获取通用数据源信息
    private Data_source DATA_SOURCE = null;
    //获取通用Agent信息
    private Agent_info AGENT_INFO = null;
    //获取通用数据库设置信息
    private Database_set DATABASE_SET = null;
    //获取通用分类设置信息
    private Collect_job_classify COLLECT_JOB_CLASSIFY = null;
    //获取通用TPCDS信息
    private List<Table_info> TABLE_INFO_S = null;
    //获取通用数据登记信息
    private List<Data_store_reg> DATA_STORE_REG_S = null;
    //获取通用TPCDS表存储信息列表
    private List<Table_storage_info> TABLE_STORAGE_INFO_S = null;
    //获取通用数据存储信息
    private List<Dtab_relation_store> DTAB_RELATION_STORE_S = null;

    public LoadGeneralTestData(long THREAD_ID) {
        this.THREAD_ID = THREAD_ID;
        //根据线程id初始化通用数据
        init();
    }

    @Method(desc = "初始化通用数据", logicStep = "初始化通用数据")
    private void init() {
        //管理员用户id
        GENERAL_MN_USER_ID = testInitConfig.getLong("general_mn_user_id");
        //业务员用户id
        GENERAL_OPER_USER_ID = testInitConfig.getLong("general_oper_user_id");
        //部门id
        GENERAL_DEP_ID = testInitConfig.getLong("general_dep_id") + THREAD_ID;
        //数据源id
        GENERAL_SOURCE_ID = testInitConfig.getLong("general_source_id") + THREAD_ID;
        //agent_id
        GENERAL_AGENT_ID = testInitConfig.getLong("general_agent_id") + THREAD_ID;
        //采集分类id
        GENERAL_CLASSIFY_ID = testInitConfig.getLong("general_classify_id") + THREAD_ID;
        //数据库设置id
        GENERAL_DATABASE_ID = testInitConfig.getLong("general_database_id") + THREAD_ID;
        //获取通用部门信息
        DEPARTMENT_INFO = getDepartmentInfo();
        //获取通用数据源和部门关系信息
        SOURCE_RELATION_DEP = getSourceRelationDep();
        //获取存储层表实体数据
        DATA_STORE_LAYER_S = getDataStoreLayers();
        //获取初始化存储层配置信息
        DATA_STORE_LAYER_ATTR_S = getDataStoreLayerAttrs();
        //获取通用数据源信息
        DATA_SOURCE = getDataSource();
        //获取通用Agent信息
        AGENT_INFO = getAgentInfo();
        //获取通用数据库设置信息
        DATABASE_SET = getDatabaseSet();
        //获取通用分类设置信息
        COLLECT_JOB_CLASSIFY = getCollectJobClassify();
        //获取通用TPCDS信息
        TABLE_INFO_S = getTableInfos();
        //获取通用数据登记信息
        DATA_STORE_REG_S = getDataStoreRegs();
        //获取通用TPCDS表存储信息列表
        TABLE_STORAGE_INFO_S = getTableStorageInfos();
        //获取通用数据存储信息
        DTAB_RELATION_STORE_S = getDtabRelationStores();
    }

    /**
     * main
     *
     * @param args args
     */
    public static void main(String[] args) {
        DatabaseWrapper db = null;
        try {
            db = new DatabaseWrapper();
            //获取当前线程id
            long thread_id = Thread.currentThread().getId() * 1000000;
            LoadGeneralTestData loadGeneralTestData = new LoadGeneralTestData(thread_id);
            //初始化通用数据
            loadGeneralTestData.execute(db);
            //提交数据库操作
            db.commit();
            //清理通用数据
            loadGeneralTestData.cleanUp(db);
            //提交数据库操作
            db.commit();
        } catch (RuntimeException e) {
            if (null != db) {
                db.rollback();
            }
            e.printStackTrace();
            throw new BusinessProcessException("初始化通用数据失败!");
        } finally {
            if (null != db) {
                db.close();
            }
        }
    }

    @Method(desc = "加载通用数据", logicStep = "加载通用数据")
    public void execute(DatabaseWrapper db) {
        //加载 Department_info
        if (null != DEPARTMENT_INFO) {
            DEPARTMENT_INFO.add(db);
        }
        //加载 Source_relation_dep
        if (null != SOURCE_RELATION_DEP) {
            SOURCE_RELATION_DEP.add(db);
        }
        //加载 Data_store_layer
        if (!DATA_STORE_LAYER_S.isEmpty()) {
            DATA_STORE_LAYER_S.forEach(data_store_layer -> data_store_layer.add(db));
        }
        //加载 Data_store_layer_attr
        if (!DATA_STORE_LAYER_ATTR_S.isEmpty()) {
            DATA_STORE_LAYER_ATTR_S.forEach(data_store_layer_attr -> data_store_layer_attr.add(db));
        }
        //加载 Data_source
        if (null != DATA_SOURCE) {
            DATA_SOURCE.add(db);
        }
        //加载 Agent_info
        if (null != AGENT_INFO) {
            AGENT_INFO.add(db);
        }
        //加载 Database_set
        if (null != DATABASE_SET) {
            DATABASE_SET.add(db);
        }
        //加载 Collect_job_classify
        if (null != COLLECT_JOB_CLASSIFY) {
            COLLECT_JOB_CLASSIFY.add(db);
        }
        //加载 Table_info
        if (!TABLE_INFO_S.isEmpty()) {
            TABLE_INFO_S.forEach(table_info -> table_info.add(db));
        }
        //加载 Data_store_reg
        if (!DATA_STORE_REG_S.isEmpty()) {
            DATA_STORE_REG_S.forEach(data_store_reg -> data_store_reg.add(db));
        }
        //加载 Table_storage_info
        if (!TABLE_STORAGE_INFO_S.isEmpty()) {
            TABLE_STORAGE_INFO_S.forEach(table_storage_info -> table_storage_info.add(db));
        }
        //加载 Dtab_relation_store
        if (!DTAB_RELATION_STORE_S.isEmpty()) {
            DTAB_RELATION_STORE_S.forEach(dtab_relation_store -> dtab_relation_store.add(db));
        }
    }

    @Method(desc = "清理通用数据", logicStep = "清理通用数据")
    public void cleanUp(DatabaseWrapper db) {
        //清理 Department_info
        if (null != DEPARTMENT_INFO) {
            SqlOperator.execute(db, "delete from " + Department_info.TableName + " where dep_id=?",
                    DEPARTMENT_INFO.getDep_id());
            long dinum = SqlOperator.queryNumber(db, "select count(1) from " + Department_info.TableName +
                    " where dep_id =?", DEPARTMENT_INFO.getDep_id()).orElseThrow(()
                    -> new RuntimeException("count fail!"));
            assertThat("Department_info 表此条数据删除后,记录数应该为0", dinum, is(0L));
        }
        //清理 Source_relation_dep
        if (null != SOURCE_RELATION_DEP) {
            SqlOperator.execute(db, "delete from " + Source_relation_dep.TableName + " where dep_id=? and source_id=?",
                    SOURCE_RELATION_DEP.getDep_id(), SOURCE_RELATION_DEP.getSource_id());
            long srdnum = SqlOperator.queryNumber(db, "select count(1) from " + Source_relation_dep.TableName +
                    " where dep_id=? and source_id=?", DEPARTMENT_INFO.getDep_id(), SOURCE_RELATION_DEP.getSource_id())
                    .orElseThrow(() -> new RuntimeException("count fail!"));
            assertThat("Source_relation_dep 表此条数据删除后,记录数应该为0", srdnum, is(0L));
        }
        //清理 Data_store_layer
        if (!DATA_STORE_LAYER_S.isEmpty()) {
            DATA_STORE_LAYER_S.forEach(data_store_layer -> {
                SqlOperator.execute(db, "delete from " + Data_store_layer.TableName + " where dsl_id=?",
                        data_store_layer.getDsl_id());
                long num = SqlOperator.queryNumber(db, "select count(1) from " + Data_store_layer.TableName +
                        " where dsl_id =?", data_store_layer.getDsl_id()).orElseThrow(()
                        -> new RuntimeException("count fail!"));
                assertThat("Data_store_layer 表此条数据删除后,记录数应该为0", num, is(0L));
            });
        }
        //清理 Data_store_layer_attr
        if (!DATA_STORE_LAYER_ATTR_S.isEmpty()) {
            DATA_STORE_LAYER_ATTR_S.forEach(data_store_layer_attr -> {
                SqlOperator.execute(db, "delete from " + Data_store_layer_attr.TableName + " where dsl_id=?",
                        data_store_layer_attr.getDsl_id());
                long num = SqlOperator.queryNumber(db, "select count(1) from " + Data_store_layer_attr.TableName +
                        " where dsl_id =?", data_store_layer_attr.getDsl_id()).orElseThrow(()
                        -> new RuntimeException("count fail!"));
                assertThat("Data_store_layer_attr 表此条数据删除后,记录数应该为0", num, is(0L));
            });
        }
        //清理 Data_source
        if (null != DATA_SOURCE) {
            SqlOperator.execute(db, "delete from " + Data_source.TableName + " where source_id=?", DATA_SOURCE.getSource_id());
            long dsnum = SqlOperator.queryNumber(db, "select count(1) from " + Data_source.TableName +
                    " where source_id =?", DATA_SOURCE.getSource_id()).orElseThrow(() -> new RuntimeException("count fail!"));
            assertThat("Data_source 表此条数据删除后,记录数应该为0", dsnum, is(0L));
        }
        //清理 Agent_info
        if (null != AGENT_INFO) {
            SqlOperator.execute(db, "delete from " + Agent_info.TableName + " where agent_id=?", AGENT_INFO.getAgent_id());
            long ainum = SqlOperator.queryNumber(db, "select count(1) from " + Agent_info.TableName +
                    " where agent_id =?", AGENT_INFO.getAgent_id()).orElseThrow(() -> new RuntimeException("count fail!"));
            assertThat("Agent_info 表此条数据删除后,记录数应该为0", ainum, is(0L));
        }
        //清理 Database_set
        if (null != DATABASE_SET) {
            SqlOperator.execute(db, "delete from " + Database_set.TableName + " where database_id=?", DATABASE_SET.getDatabase_id());
            long dbsnum = SqlOperator.queryNumber(db, "select count(1) from " + Database_set.TableName +
                    " where database_id =?", DATABASE_SET.getDatabase_id()).orElseThrow(() -> new RuntimeException("count fail!"));
            assertThat("Database_set 表此条数据删除后,记录数应该为0", dbsnum, is(0L));
        }
        //清理 Collect_job_classify
        if (null != COLLECT_JOB_CLASSIFY) {
            SqlOperator.execute(db, "delete from " + Collect_job_classify.TableName + " where classify_id=?",
                    COLLECT_JOB_CLASSIFY.getClassify_id());
            long cjcnum = SqlOperator.queryNumber(db, "select count(1) from " + Collect_job_classify.TableName +
                    " where classify_id =?", COLLECT_JOB_CLASSIFY.getClassify_id())
                    .orElseThrow(() -> new RuntimeException("count fail!"));
            assertThat("Collect_job_classify 表此条数据删除后,记录数应该为0", cjcnum, is(0L));
        }
        //清理 Table_info
        if (!TABLE_INFO_S.isEmpty()) {
            TABLE_INFO_S.forEach(table_info -> {
                SqlOperator.execute(db, "delete from " + Table_info.TableName + " where table_id=?", table_info.getTable_id());
                long tinum = SqlOperator.queryNumber(db, "select count(1) from " + Table_info.TableName +
                        " where table_id =?", table_info.getTable_id()).orElseThrow(() -> new RuntimeException("count fail!"));
                assertThat("Table_info 表此条数据删除后,记录数应该为0", tinum, is(0L));
            });
        }
        //清理 Data_store_reg
        if (!DATA_STORE_REG_S.isEmpty()) {
            DATA_STORE_REG_S.forEach(data_store_reg -> {
                SqlOperator.execute(db, "delete from " + Data_store_reg.TableName + " where table_id=?",
                        data_store_reg.getTable_id());
                long num = SqlOperator.queryNumber(db, "select count(1) from " + Data_store_reg.TableName +
                        " where file_id =?", data_store_reg.getFile_id()).orElseThrow(()
                        -> new RuntimeException("count fail!"));
                assertThat("Data_store_reg 表此条数据删除后,记录数应该为0", num, is(0L));
            });
        }
        //清理 Table_storage_info
        if (!TABLE_STORAGE_INFO_S.isEmpty()) {
            TABLE_STORAGE_INFO_S.forEach(table_storage_info -> {
                SqlOperator.execute(db, "delete from " + Table_storage_info.TableName + " where storage_id=?",
                        table_storage_info.getStorage_id());
                long num = SqlOperator.queryNumber(db, "select count(1) from " + Table_storage_info.TableName +
                        " where storage_id =?", table_storage_info.getStorage_id()).orElseThrow(()
                        -> new RuntimeException("count fail!"));
                assertThat("Table_storage_info 表此条数据删除后,记录数应该为0", num, is(0L));
            });
        }
        //清理 Dtab_relation_store
        if (!DTAB_RELATION_STORE_S.isEmpty()) {
            DTAB_RELATION_STORE_S.forEach(dtab_relation_store -> {
                SqlOperator.execute(db, "delete from " + Dtab_relation_store.TableName + " where tab_id=? and dsl_id=?",
                        dtab_relation_store.getTab_id(), dtab_relation_store.getDsl_id());
                long num = SqlOperator.queryNumber(db, "select count(1) from " + Dtab_relation_store.TableName +
                                " where tab_id=? and dsl_id=?", dtab_relation_store.getTab_id(),
                        dtab_relation_store.getDsl_id()).orElseThrow(() -> new RuntimeException("count fail!"));
                assertThat("Dtab_relation_store 表此条数据删除后,记录数应该为0", num, is(0L));
            });
        }

    }

    @Method(desc = "获取部门表实体数据", logicStep = "获取部门表实体数据")
    public Department_info getDepartmentInfo() {
        //Department_info
        Department_info dep = new Department_info();
        dep.setDep_id(GENERAL_DEP_ID);
        dep.setDep_name("通用部门" + GENERAL_DEP_ID);
        dep.setCreate_date(DateUtil.getSysDate());
        dep.setCreate_time(DateUtil.getSysTime());
        return dep;
    }

    @Method(desc = "获取数据源部门关系实体数据", logicStep = "获取数据源部门关系实体数据")
    public Source_relation_dep getSourceRelationDep() {
        //Source_relation_dep
        Source_relation_dep srd = new Source_relation_dep();
        srd.setSource_id(GENERAL_SOURCE_ID);
        srd.setDep_id(GENERAL_DEP_ID);
        return srd;
    }

    @Method(desc = "获取初始化存储层表实体数据", logicStep = "获取初始化存储层表实体数据")
    public List<Data_store_layer> getDataStoreLayers() {
        //初始化存储层信息列表
        List<Data_store_layer> dsls = new ArrayList<>();
        //Data_store_layer
        Data_store_layer dsl;
        for (int i = 0; i < TEST_STORAGE_LAYER_INFO_S.size(); i++) {
            //测试数据库信息
            YamlMap test_database = TEST_STORAGE_LAYER_INFO_S.getMap(i);
            //存储层设置id
            long DSL_ID = test_database.getLong("dsl_id") + THREAD_ID;
            dsl = new Data_store_layer();
            dsl.setDsl_id(DSL_ID);
            dsl.setDsl_name("通用测试存储层-" + test_database.getString(StorageTypeKey.database_type));
            dsl.setStore_type(Store_type.DATABASE.getCode());
            dsl.setIs_hadoopclient(IsFlag.Fou.getCode());
            dsl.setDsl_remark("通用测试存储层-" + test_database.getString(StorageTypeKey.database_type));
            dsls.add(dsl);
        }
        return dsls;
    }

    @Method(desc = "获取初始化存储层配置信息", logicStep = "获取初始化存储层配置信息")
    public List<Data_store_layer_attr> getDataStoreLayerAttrs() {
        //初始化存储层配置信息列表
        List<Data_store_layer_attr> dslas = new ArrayList<>();
        //Data_store_layer_attr
        Data_store_layer_attr dsla;
        for (int i = 0; i < TEST_STORAGE_LAYER_INFO_S.size(); i++) {
            //测试数据库信息
            YamlMap test_database = TEST_STORAGE_LAYER_INFO_S.getMap(i);
            //存储层设置id
            long DSL_ID = test_database.getLong("dsl_id") + THREAD_ID;
            //设置数据库类型
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.database_type);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.database_code));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
            //设置驱动名称
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.database_driver);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.database_driver));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
            //设置URL
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.jdbc_url);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.jdbc_url));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
            //设置用户名
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.user_name);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.user_name));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
            //设置用户密码
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.database_pwd);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.database_pwd));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
            //设置数据库名称
            dsla = new Data_store_layer_attr();
            dsla.setDsla_id(PrimayKeyGener.getNextId());
            dsla.setStorage_property_key(StorageTypeKey.database_name);
            dsla.setStorage_property_val(test_database.getString(StorageTypeKey.database_name));
            dsla.setIs_file(IsFlag.Fou.getCode());
            dsla.setDsl_id(DSL_ID);
            dslas.add(dsla);
        }
        return dslas;
    }

    @Method(desc = "获取通用数据源信息", logicStep = "获取通用数据源信息")
    public Data_source getDataSource() {
        //Data_source
        Data_source dataSource = new Data_source();
        dataSource.setSource_id(GENERAL_SOURCE_ID);
        dataSource.setDatasource_number("GENERAL_SOURCE_ID");
        dataSource.setDatasource_name("测试通用数据源名称");
        dataSource.setSource_remark("测试通用数据源");
        dataSource.setCreate_date(DateUtil.getSysDate());
        dataSource.setCreate_time(DateUtil.getSysTime());
        dataSource.setCreate_user_id(GENERAL_MN_USER_ID);
        return dataSource;
    }

    @Method(desc = "获取通用Agent信息", logicStep = "获取通用Agent信息")
    public Agent_info getAgentInfo() {
        //Agent_info
        Agent_info agentInfo = new Agent_info();
        agentInfo.setAgent_id(GENERAL_AGENT_ID);
        agentInfo.setAgent_name("测试通用Agent");
        agentInfo.setAgent_type(AgentType.ShuJuKu.getCode());
        agentInfo.setAgent_ip("127.0.0.1");
        agentInfo.setAgent_port("8888");
        agentInfo.setAgent_status("1");
        agentInfo.setCreate_date(DateUtil.getSysDate());
        agentInfo.setCreate_time(DateUtil.getSysTime());
        agentInfo.setSource_id(GENERAL_SOURCE_ID);
        agentInfo.setUser_id(GENERAL_OPER_USER_ID);
        return agentInfo;
    }

    @Method(desc = "获取通用数据库设置信息", logicStep = "获取通用数据库设置信息")
    public Database_set getDatabaseSet() {
        //Database_set
        Database_set databaseSet = new Database_set();
        databaseSet.setDatabase_id(GENERAL_DATABASE_ID);
        databaseSet.setAgent_id(GENERAL_AGENT_ID);
        databaseSet.setDatabase_number("GENERAL_DB");
        databaseSet.setTask_name("GENERAL_TASK_NAME");
        databaseSet.setDb_agent(IsFlag.Shi.getCode());
        databaseSet.setIs_sendok(IsFlag.Shi.getCode());
        databaseSet.setClassify_id(GENERAL_CLASSIFY_ID);
        return databaseSet;
    }

    @Method(desc = "获取通用分类设置信息", logicStep = "获取通用分类设置信息")
    public Collect_job_classify getCollectJobClassify() {
        //Collect_job_classify
        Collect_job_classify collect_job_classify = new Collect_job_classify();
        collect_job_classify.setClassify_id(GENERAL_CLASSIFY_ID);
        collect_job_classify.setClassify_num("num" + GENERAL_CLASSIFY_ID);
        collect_job_classify.setClassify_name("name" + GENERAL_CLASSIFY_ID);
        collect_job_classify.setUser_id(GENERAL_OPER_USER_ID);
        collect_job_classify.setAgent_id(GENERAL_AGENT_ID);
        return collect_job_classify;
    }

    @Method(desc = "获取通用TPCDS表信息", logicStep = "获取通用TPCDS表信息")
    public List<Table_info> getTableInfos() {
        //初始化表信息列表
        List<Table_info> table_info_s = new ArrayList<>();
        Table_info table_info;
        for (int i = 0; i < TPCDS_TABLE_INFO_S.size(); i++) {
            YamlMap table_info_map = TPCDS_TABLE_INFO_S.getMap(i);
            table_info = new Table_info();
            table_info.setTable_id(table_info_map.getString("table_id"));
            table_info.setTable_name(table_info_map.getString("table_name"));
            table_info.setTable_ch_name(table_info_map.getString("table_name") + "_zh");
            table_info.setRec_num_date(DateUtil.getSysDate());
            table_info.setDatabase_id(GENERAL_DATABASE_ID);
            table_info.setValid_s_date(DateUtil.getSysDate());
            table_info.setValid_e_date("99991231");
            table_info.setIs_md5(IsFlag.Shi.getCode());
            table_info.setIs_register(IsFlag.Fou.getCode());
            table_info.setIs_customize_sql(IsFlag.Fou.getCode());
            table_info.setIs_parallel(IsFlag.Fou.getCode());
            table_info.setIs_user_defined(IsFlag.Fou.getCode());
            table_info.setUnload_type(UnloadType.QuanLiangXieShu.getCode());
            table_info_s.add(table_info);
        }
        return table_info_s;
    }

    @Method(desc = "获取通用TPCDS表登记信息列表", logicStep = "获取通用TPCDS表登记信息列表")
    public List<Data_store_reg> getDataStoreRegs() {
        //初始化表存登记信息列表
        List<Data_store_reg> dsrs = new ArrayList<>();
        //Data_store_reg
        Data_store_reg dsr;
        for (Table_info tableInfo : TABLE_INFO_S) {
            dsr = new Data_store_reg();
            dsr.setFile_id(String.valueOf(PrimayKeyGener.getNextId()));
            dsr.setCollect_type(AgentType.DBWenJian.getCode());
            dsr.setOriginal_update_date(DateUtil.getSysDate());
            dsr.setOriginal_update_time(DateUtil.getSysTime());
            dsr.setOriginal_name(tableInfo.getTable_ch_name());
            dsr.setTable_name(tableInfo.getTable_name());
            dsr.setHyren_name(tableInfo.getTable_name());
            dsr.setStorage_date(DateUtil.getSysDate());
            dsr.setStorage_time(DateUtil.getSysTime());
            dsr.setFile_size(10000L);
            dsr.setAgent_id(GENERAL_AGENT_ID);
            dsr.setSource_id(GENERAL_SOURCE_ID);
            dsr.setDatabase_id(GENERAL_DATABASE_ID);
            dsr.setTable_id(tableInfo.getTable_id());
            dsrs.add(dsr);
        }
        return dsrs;
    }

    @Method(desc = "获取通用TPCDS表存储信息列表", logicStep = "获取通用TPCDS表存储信息列表")
    public List<Table_storage_info> getTableStorageInfos() {
        //初始化表存储信息列表
        List<Table_storage_info> tsis = new ArrayList<>();
        //Table_storage_info
        Table_storage_info tsi;
        for (Data_store_reg dsr : DATA_STORE_REG_S) {
            tsi = new Table_storage_info();
            tsi.setStorage_id(PrimayKeyGener.getNextId());
            tsi.setFile_format(FileFormat.CSV.getCode());
            tsi.setStorage_type(StorageType.TiHuan.getCode());
            tsi.setIs_zipper(IsFlag.Fou.getCode());
            tsi.setStorage_time(7L);
            tsi.setHyren_name(dsr.getHyren_name());
            tsi.setTable_id(dsr.getTable_id());
            tsis.add(tsi);
        }
        return tsis;
    }

    @Method(desc = "获取通用TPCDS表存储关系列表", logicStep = "获取通用TPCDS表存储关系列表")
    public List<Dtab_relation_store> getDtabRelationStores() {
        //初始化存储关系列表
        List<Dtab_relation_store> drss = new ArrayList<>();
        //Dtab_relation_store
        //循环根据存储层插入数据表存储关系信息
        DATA_STORE_LAYER_S.forEach(dsl -> {
            Dtab_relation_store drs;
            //DCL
            for (Table_storage_info tsi : TABLE_STORAGE_INFO_S) {
                drs = new Dtab_relation_store();
                drs.setDsl_id(dsl.getDsl_id());
                drs.setTab_id(tsi.getStorage_id());
                drs.setData_source(StoreLayerDataSource.DB.getCode());
                drs.setIs_successful(JobExecuteState.WanCheng.getCode());
                drss.add(drs);
            }
        });
        return drss;
    }
}
