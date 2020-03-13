package hrds.commons.ftree.bean;

import fd.ng.core.annotation.DocClass;
import hrds.commons.codes.DataSourceType;

import java.util.HashMap;
import java.util.Map;

@DocClass(desc = "树页面来源配置类", author = "BY-HLL", createdate = "2019/12/24 0024 上午 10:05")
public class TreePageSource {
    //进HBase
    private static final String INTO_HBASE = "intoHBase";
    //二级索引
    private static final String SECOND_INDEX = "secondIndex";
    //webSQL
    private static final String WEB_SQL = "webSQL";
    //集市
    private static final String MARKET = "market";
    //加工
    private static final String MACHINING = "machining";
    //接口
    private static final String INTERFACE = "interface";
    //报表
    private static final String REPORT = "report";
    //机器学习
    private static final String MACHINE_LEARNING = "machineLearning";
    //外部
    private static final String EXTERNAL = "external";
    //数据管控
    private static final String DATA_MANAGEMENT = "dataManagement";
    //数据对标
    private static final String DATA_BENCHMARKING = "dataBenchmarking";

    //集市,加工,数据管控 树菜单列表
    private static DataSourceType[] DATA_MANAGEMENT_ARRAY = new DataSourceType[]{DataSourceType.DCL,
            DataSourceType.DPL, DataSourceType.DML, DataSourceType.DQC,
            DataSourceType.UDL};
    //webSQL树菜单列表
    private static DataSourceType[] WEB_SQL_ARRAY = new DataSourceType[]{DataSourceType.DCL};
    //数据进HBase,接口,报表,外部 树菜单列表
    private static DataSourceType[] HIRE_ARRAY = new DataSourceType[]{DataSourceType.DCL, DataSourceType.DPL,
            DataSourceType.DML, DataSourceType.UDL};
    //二级索引
    private static DataSourceType[] SECOND_INDEX_ARRAY = new DataSourceType[]{DataSourceType.DCL,
            DataSourceType.DPL, DataSourceType.DML, DataSourceType.UDL};
    //机器学习
    private static DataSourceType[] MACHINE_LEARNING_ARRAY = new DataSourceType[]{DataSourceType.DCL,
            DataSourceType.DPL, DataSourceType.DML, DataSourceType.UDL};
    //数据对标
    private static DataSourceType[] DATA_BENCHMARKING_ARRAY = new DataSourceType[]{DataSourceType.DCL,
            DataSourceType.SFL};

    public static Map<String, DataSourceType[]> TREE_SOURCE = new HashMap<>();

    //初始化树页面类型
    static {
        //进HBase
        TREE_SOURCE.put(INTO_HBASE, HIRE_ARRAY);
        //二级索引
        TREE_SOURCE.put(SECOND_INDEX, SECOND_INDEX_ARRAY);
        //WEB SQL
        TREE_SOURCE.put(WEB_SQL, WEB_SQL_ARRAY);
        //集市
        TREE_SOURCE.put(MARKET, DATA_MANAGEMENT_ARRAY);
        //加工
        TREE_SOURCE.put(MACHINING, DATA_MANAGEMENT_ARRAY);
        //接口
        TREE_SOURCE.put(INTERFACE, HIRE_ARRAY);
        //报表
        TREE_SOURCE.put(REPORT, HIRE_ARRAY);
        //机器学习
        TREE_SOURCE.put(MACHINE_LEARNING, MACHINE_LEARNING_ARRAY);
        //外部
        TREE_SOURCE.put(EXTERNAL, HIRE_ARRAY);
        //数据管控
        TREE_SOURCE.put(DATA_MANAGEMENT, DATA_MANAGEMENT_ARRAY);
        //数据对标
        TREE_SOURCE.put(DATA_BENCHMARKING, DATA_BENCHMARKING_ARRAY);
    }
}