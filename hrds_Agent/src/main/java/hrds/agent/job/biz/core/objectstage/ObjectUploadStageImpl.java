package hrds.agent.job.biz.core.objectstage;

import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Return;
import hrds.agent.job.biz.bean.DataStoreConfBean;
import hrds.agent.job.biz.bean.ObjectTableBean;
import hrds.agent.job.biz.bean.StageParamInfo;
import hrds.agent.job.biz.bean.StageStatusInfo;
import hrds.agent.job.biz.constant.RunStatusConstant;
import hrds.agent.job.biz.constant.StageConstant;
import hrds.agent.job.biz.core.AbstractJobStage;
import hrds.agent.job.biz.core.objectstage.service.ObjectProcessInterface;
import hrds.agent.job.biz.core.objectstage.service.impl.MppTableProcessImpl;
import hrds.agent.job.biz.utils.JobStatusInfoUtil;
import hrds.commons.codes.AgentType;
import hrds.commons.codes.Store_type;
import hrds.commons.exception.AppSystemException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@DocClass(desc = "半结构化对象采集数据上传阶段", author = "zxz")
public class ObjectUploadStageImpl extends AbstractJobStage {
	//打印日志
	private static final Logger LOGGER = LogManager.getLogger();
	//卸数到本地的文件绝对路径
	//数据采集表对应的存储的所有信息
	private final ObjectTableBean objectTableBean;

	public ObjectUploadStageImpl(ObjectTableBean objectTableBean) {
		this.objectTableBean = objectTableBean;
	}

	@Method(desc = "半结构化对象采集数据上传阶段处理逻辑，处理完成后，无论成功还是失败，" +
			"将相关状态信息封装到StageStatusInfo对象中返回", logicStep = "" +
			"1、创建卸数阶段状态信息，更新作业ID,阶段名，阶段开始时间" +
			"2、调用方法，进行文件上传，文件数组和上传目录由构造器传入")
	@Return(desc = "StageStatusInfo是保存每个阶段状态信息的实体类", range = "不会为null,StageStatusInfo实体类对象")
	@Override
	public StageParamInfo handleStage(StageParamInfo stageParamInfo) {
		long startTime = System.currentTimeMillis();
		LOGGER.info("------------------表" + objectTableBean.getEn_name()
				+ "半结构化对象采集上传阶段开始------------------");
		//1、创建卸数阶段状态信息，更新作业ID,阶段名，阶段开始时间
		StageStatusInfo statusInfo = new StageStatusInfo();
		JobStatusInfoUtil.startStageStatusInfo(statusInfo, objectTableBean.getOcs_id(),
				StageConstant.UPLOAD.getCode());
		try {
			ObjectProcessInterface processInterface = null;
			try {
				List<DataStoreConfBean> dataStoreConfBeanList = objectTableBean.getDataStoreConfBean();
				for (DataStoreConfBean dataStoreConfBean : dataStoreConfBeanList) {
					//这边做一个接口多实现，目前只实现传统数据库的增量更新接口
					if (Store_type.DATABASE.getCode().equals(dataStoreConfBean.getStore_type())) {
						//关系型数据库
						processInterface = new MppTableProcessImpl(stageParamInfo.getTableBean(),
								objectTableBean, dataStoreConfBean);
					} else {
						throw new AppSystemException("半结构化对象采集目前不支持入" + dataStoreConfBean.getDsl_name());
					}
					for (String readFile : stageParamInfo.getFileArr()) {
						processInterface.parserFileToTable(readFile);
					}
				}
			} catch (Exception e) {
				throw new AppSystemException("表" + objectTableBean.getEn_name()
						+ "db文件采集增量上传失败", e);
			} finally {
				if (processInterface != null) {
					processInterface.close();
				}
			}
			JobStatusInfoUtil.endStageStatusInfo(statusInfo, RunStatusConstant.SUCCEED.getCode(), "执行成功");
			LOGGER.info("------------------表" + objectTableBean.getEn_name()
					+ "半结构化对象采集上传阶段成功------------------执行时间为："
					+ (System.currentTimeMillis() - startTime) / 1000 + "，秒");
		} catch (Exception e) {
			JobStatusInfoUtil.endStageStatusInfo(statusInfo, RunStatusConstant.FAILED.getCode(), e.getMessage());
			LOGGER.error("半结构对象" + objectTableBean.getEn_name() + "上传阶段失败：", e);
		}
		//结束给stageParamInfo塞值
		JobStatusInfoUtil.endStageParamInfo(stageParamInfo, statusInfo, objectTableBean
				, AgentType.DuiXiang.getCode());
		return stageParamInfo;
	}

	@Override
	public int getStageCode() {
		return StageConstant.UPLOAD.getCode();
	}
}
