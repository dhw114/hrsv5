package hrds.c.biz.util;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import fd.ng.core.annotation.DocClass;
import fd.ng.core.annotation.Method;
import fd.ng.core.annotation.Param;
import fd.ng.core.utils.FileUtil;
import hrds.c.biz.util.controlconf.ControlConfParam;
import hrds.commons.exception.AppSystemException;
import hrds.commons.exception.BusinessException;
import hrds.commons.utils.PropertyParaValue;
import hrds.commons.utils.jsch.SCPFileSender;
import hrds.commons.utils.jsch.SFTPChannel;
import hrds.commons.utils.jsch.SFTPDetails;
import hrds.commons.utils.yaml.Yaml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@DocClass(desc = "ETL部署类", author = "dhw", createdate = "2019/12/19 14:57")
public class ETLAgentDeployment {

	private static final Logger logger = LogManager.getLogger();

	// 系统路径的符号
	public static final String SEPARATOR = File.separator;

	@Method(desc = "ETL部署", logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制"
			+ "2.获取ETL下载地址"
			+ "3.根据文件路径获取文件信息"
			+ "4.获取数据库连接配置信息"
			+ "5.获取redis连接配置信息"
			+ "6.获得程序当前路径"
			+ "7.根据程序当前路径获取文件"
			+ "8.集群conf配置文件目录"
			+ "9.创建存放部署ETL连接信息的集合并封装属性"
			+ "10.ETL部署"
			+ "11.部署完成后删除db与redis配置文件")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新建工程时生成")
	@Param(name = "etl_serv_ip", desc = "ETL部署agent的IP地址", range = "如：127.0.0.1")
	@Param(name = "etl_serv_port", desc = "ETL部署agent的端口", range = "默认22")
	@Param(name = "redisIP", desc = "ETL部署redis的ip", range = "如：127.0.0.1")
	@Param(name = "redisPort", desc = "ETL部署redis的端口", range = "默认56379")
	@Param(name = "userName", desc = "ETL部署agent机器的用户名", range = "服务器用户名")
	@Param(name = "password", desc = "ETL部署agent机器的密码", range = "服务器密码")
	@Param(name = "targetDir", desc = "ETL部署服务器目录地址", range = "无限制")
	public static void scpETLAgent(String etl_sys_cd, String etl_serv_ip, String etl_serv_port, String userName,
	                               String password, String targetDir) {
		try {
			// 1.数据可访问权限处理方式，该方法不需要权限控制
			// 2.获取ETL下载地址
			String agentPath = PropertyParaValue.getString("ETLpath", "");
			// 3.根据文件路径获取文件信息
			File sourceFile = new File(agentPath);
			// 4.配置文件的临时存放路径,判断文件目录是否存在，如果不存在创建
			String tmp_conf_path = System.getProperty("user.dir") + SEPARATOR + "etlTempResources"
					+ SEPARATOR + "fdconfig" + SEPARATOR;
			File file = new File(tmp_conf_path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					throw new BusinessException("创建文件临时存放目录失败");
				}
			}
			logger.info("==========配置文件的临时存放路径===========" + tmp_conf_path);
			// 5.生成control.conf配置文件
			Yaml.dump(ControlConfParam.getControlConfParam(), new File(tmp_conf_path
					+ ControlConfParam.CONF_FILE_NAME));
			// 6.根据程序当前路径获取文件
			File userDirFile = FileUtil.getFile(System.getProperty("user.dir"));
			// 7.集群conf配置文件目录 fixme  集群配置文件暂时不知如何获取
			String hadoopConf = userDirFile + SEPARATOR + "conf" + SEPARATOR;
			// 8.创建存放部署ETL连接信息的集合并封装属性
			SFTPDetails sftpDetails = new SFTPDetails();
			// 9.设置部署所需参数
			setSFTPDetails(etl_sys_cd, etl_serv_ip, etl_serv_port, userName, password, targetDir,
					sourceFile.getName(), sourceFile.getParent(), hadoopConf, tmp_conf_path, sftpDetails);
			// 10.ETL部署
			SCPFileSender.etlScpToFrom(sftpDetails);
			// 11.部署完成后删除本地临时配置文件
			FileUtil.deleteDirectoryFiles(tmp_conf_path);
		} catch (Exception e) {
			throw new AppSystemException(e);
		}
	}

	private static void setSFTPDetails(String etl_sys_cd, String etl_serv_ip, String etl_serv_port,
	                                   String userName, String password, String targetDir,
	                                   String sourceFileName, String source_path, String hadoopConf,
	                                   String tmp_conf_path, SFTPDetails sftpDetails) {
		sftpDetails.setHost(etl_serv_ip);
		// 部署agent服务器用户名
		sftpDetails.setUser_name(userName);
		// 部署agent服务器密码
		sftpDetails.setPwd(password);
		// 部署agent服务器端口
		sftpDetails.setPort(Integer.parseInt(etl_serv_port));
		// 本地文件路径
		sftpDetails.setSource_path(source_path + SEPARATOR);
		// 本地文件名称
		sftpDetails.setAgent_gz(sourceFileName);
		// 集群conf配置文件
		sftpDetails.setHADOOP_CONF(hadoopConf);
		// 目标路径
		sftpDetails.setTarget＿dir(targetDir + SEPARATOR + etl_sys_cd + SEPARATOR);
		// 临时存放配置文件路径
		sftpDetails.setTmp_conf_path(tmp_conf_path);
	}

	@Method(desc = "启动Control",
			logicStep = "1.数据可访问权限处理方式，该方法不需要权限控制"
					+ "2.设置连接服务器属性信息"
					+ "3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据"
					+ "4.启动CONTROL脚本命令"
					+ "5.执行命令启动CONTROL"
					+ "6.断开连接")
	@Param(name = "batch_date", desc = "跑批日期", range = "yyyy-MM-dd格式的年月日")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "isResumeRun", desc = "是否续跑", range = "使用（IsFlag）代码项，1代表是，0代表否")
	@Param(name = "isAutoShift", desc = "是否日切", range = "使用（IsFlag）代码项，1代表是，0代表否")
	@Param(name = "etl_serv_ip", desc = "部署服务器IP", range = "如：127.0.0.1")
	@Param(name = "etl_serv_port", desc = "部署服务器端口", range = "默认22")
	@Param(name = "userName", desc = "部署服务器用户名", range = "无限制")
	@Param(name = "userName", desc = "部署服务器密码", range = "无限制")
	@Param(name = "deploymentPath", desc = "部署服务器路径", range = "无限制")
	public static void startEngineBatchControl(String batch_date, String etl_sys_cd, String isResumeRun,
	                                           String isAutoShift, String etl_serv_ip, String etl_serv_port,
	                                           String userName, String password, String deploymentPath) {
		try {
			// 1.数据可访问权限处理方式，该方法不需要权限控制
			// 2.设置连接服务器属性信息
			SFTPDetails sftpDetails = new SFTPDetails();
			sftpDetails.setHost(etl_serv_ip);
			sftpDetails.setPort(Integer.parseInt(etl_serv_port));
			sftpDetails.setUser_name(userName);
			sftpDetails.setPwd(password);
			// 3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据
			Session shellSession = SFTPChannel.getJSchSession(sftpDetails, 0);
			// 4.启动CONTROL脚本命令
			String startAgent =
					"source /etc/profile;source ~/.bash_profile;source ~/.bashrc; cd "
							+ deploymentPath
							+ "/"
							+ etl_sys_cd
							+ "/control/ ;"
							+ "sh startEngineBatchControl.sh"
							+ " "
							+ batch_date
							+ " "
							+ etl_sys_cd
							+ " "
							+ isResumeRun
							+ " "
							+ isAutoShift;
			logger.info("######################" + startAgent);
			// 5.执行命令启动CONTROL
			SFTPChannel.execCommandByJSchNoRs(shellSession, startAgent);
			logger.info("###########启动startEngineBatchControl成功===");
			// 6.断开连接
			shellSession.disconnect();
		} catch (JSchException e) {
			logger.error("连接失败，请确认用户名密码正确", e);
			throw new BusinessException("连接失败，请确认用户名密码正确");
		} catch (IOException e) {
			logger.error("网络异常，请确认网络正常", e);
			throw new BusinessException("网络异常，请确认网络正常");
		} catch (Exception e) {
			throw new AppSystemException("部署失败，请重新部署" + e);
		}
	}

	@Method(
			desc = "启动trigger",
			logicStep =
					"1.数据可访问权限处理方式，该方法不需要权限控制"
							+ "2.设置连接服务器属性信息"
							+ "3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据"
							+ "4.启动TRIGGER脚本命令"
							+ "5.执行启动TRIGGER脚本命令启动TRIGGER"
							+ "6.断开连接")
	@Param(name = "etl_sys_cd", desc = "工程编号", range = "新增工程时生成")
	@Param(name = "etl_serv_ip", desc = "部署服务器IP", range = "如：127.0.0.1")
	@Param(name = "etl_serv_port", desc = "部署服务器端口", range = "默认22")
	@Param(name = "userName", desc = "服务器用户名", range = "无限制")
	@Param(name = "password", desc = "服务器密码", range = "无限制")
	@Param(name = "deploymentPath", desc = "部署服务器路径", range = "无限制")
	public static void startEngineBatchTrigger(
			String etl_sys_cd,
			String etl_serv_ip,
			String etl_serv_port,
			String userName,
			String password,
			String deploymentPath) {
		try {
			// 1.数据可访问权限处理方式，该方法不需要权限控制
			// 2.设置连接服务器属性信息
			SFTPDetails sftpDetails = new SFTPDetails();
			sftpDetails.setHost(etl_serv_ip);
			sftpDetails.setPort(Integer.parseInt(etl_serv_port));
			sftpDetails.setUser_name(userName);
			sftpDetails.setPwd(password);
			// 3.与远端服务器进行交互，建立连接，发送数据到远端并且接收远端发来的数据
			Session shellSession = SFTPChannel.getJSchSession(sftpDetails, 0);
			// 4.启动TRIGGER脚本命令
			String startAgent =
					"source /etc/profile;source ~/.bash_profile;source ~/.bashrc; cd "
							+ deploymentPath
							+ "/"
							+ etl_sys_cd
							+ "/trigger/ ;"
							+ "sh startEngineBatchTrigger.sh"
							+ " "
							+ etl_sys_cd;
			logger.info("##############" + startAgent);
			// 5.执行启动TRIGGER脚本命令启动TRIGGER
			SFTPChannel.execCommandByJSchNoRs(shellSession, startAgent);
			logger.info("###########启动startEngineBatchTrigger成功===");
			// 6.断开连接
			shellSession.disconnect();
		} catch (JSchException e) {
			logger.error("连接失败，请确认用户名密码正确", e);
			throw new BusinessException("连接失败，请确认用户名密码正确");
		} catch (IOException e) {
			logger.error("网络异常，请确认网络正常", e);
			throw new BusinessException("网络异常，请确认网络正常");
		} catch (Exception e) {
			throw new AppSystemException("部署失败，请重新部署" + e);
		}
	}
}
