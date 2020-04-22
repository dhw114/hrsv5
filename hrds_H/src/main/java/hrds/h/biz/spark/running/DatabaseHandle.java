package hrds.h.biz.spark.running;

import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DatabaseWrapper;
import hrds.commons.collection.ConnectionTool;
import hrds.commons.collection.bean.DbConfBean;
import org.apache.spark.sql.*;

import java.util.Properties;

import static hrds.commons.utils.Constant.*;

/**
 * @Author: Mick Yuan
 * @Date:
 * @Since jdk1.8
 */
public class DatabaseHandle extends Handle {

    private SparkHandleArgument.DatabaseArgs databaseArgs;
    private Properties connProperties = new Properties();

    DatabaseHandle(SparkSession spark, Dataset<Row> dataset,
                   SparkHandleArgument.DatabaseArgs databaseArgs) {
        super(spark, dataset, databaseArgs.getTableName());
        this.databaseArgs = databaseArgs;
        initProperties();
    }

    private void initProperties() {
        connProperties.setProperty("user", databaseArgs.getUser());
        connProperties.setProperty("password", databaseArgs.getPassword());
    }

    public void insert() {
        DataFrameWriter<Row> dataFrameWriter;
        if (databaseArgs.isOverWrite()) {
            dataFrameWriter = dataset.write().mode(SaveMode.Overwrite);
        } else {
            dataFrameWriter = dataset.write().mode(SaveMode.Append);
        }
        if (StringUtil.isNotBlank(databaseArgs.createTableColumnTypes)) {
            dataFrameWriter = dataFrameWriter.option("createTableColumnTypes",
                    databaseArgs.getCreateTableColumnTypes());
        }
        dataFrameWriter.jdbc(databaseArgs.getUrl(), tableName, connProperties);
    }

    @Override
    public void increment() {
        String currentTable = "current_hyren_" + tableName;

        Dataset<Row> finalDF = spark.read().jdbc(databaseArgs.getUrl(), tableName, connProperties)
                .filter(EDATENAME + "=" + MAXDATE);
        dataset.createOrReplaceTempView(currentTable);
        finalDF.createOrReplaceTempView(tableName);

        /**
         * 有效数据的df，原样插入即可
         */
        Dataset<Row> validDF = spark.sql("SELECT * FROM " + currentTable +
                " WHERE NOT EXISTS(SELECT 1 FROM " + tableName + ")");
        validDF.write().jdbc(databaseArgs.getUrl(), tableName, connProperties);

        /**
         * 需要关链的数据df，只有一列，就是hyren的MD5值
         * 把该 df 写到最终表同数据库中,表名为 delta_hyren_${tableName}
         * 然后把最终表中的这些MD5记录的edate更新成调度日期
         */
        Dataset<Row> invalidDF = spark.sql("SELECT " + MD5NAME +
                " FROM " + tableName + " WHERE NOT EXISTS(SELECT 1 FROM " + currentTable + ")");

        String deltaTable = "delta_hyren_" + tableName;
        invalidDF.write().mode(SaveMode.Overwrite).
                jdbc(databaseArgs.getUrl(), deltaTable, connProperties);

        //jdbc连接数据库进行更新
        DbConfBean dbConfBean = new DbConfBean();
        dbConfBean.setDatabase_drive(databaseArgs.getDriver());
        dbConfBean.setJdbc_url(databaseArgs.getUrl());
        dbConfBean.setUser_name(databaseArgs.getUser());
        dbConfBean.setDatabase_pad(databaseArgs.getPassword());
        dbConfBean.setDatabase_type(databaseArgs.getDatabaseType());
        try (DatabaseWrapper db = ConnectionTool.getDBWrapper(dbConfBean)) {
            db.execute("UPDATE ? SET ? = '?' WHERE EXISTS (SELECT 1 FROM ?)",
                    tableName, EDATENAME, MAXDATE, deltaTable);
            db.execute("DROP TABLE " + deltaTable);
        }

    }
}
