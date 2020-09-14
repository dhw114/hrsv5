package hrds.g.biz.commons;

import fd.ng.core.utils.CodecUtil;
import fd.ng.web.util.Dbo;
import fd.ng.web.util.RequestUtil;
import fd.ng.web.util.ResponseUtil;
import hrds.commons.codes.DataBaseCode;
import hrds.commons.entity.Interface_file_info;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Base64;
import java.util.Map;

public class FileDownload {

	public HttpServletResponse downLoadFile(String uuid, Long user_id) {

		OutputStream out;
		BufferedInputStream in;
		HttpServletResponse response = ResponseUtil.getResponse();
		try {
			Map<String, Object> fileInfo = fileInfo(uuid, user_id);
			String file_path = fileInfo.get("file_path").toString();
			String data_class = fileInfo.get("data_class").toString();
			uuid = uuid + '.' + data_class;
			//文件存放的路径
			file_path = file_path + File.separator + uuid;
			// 清空response
			response.reset();
			// 设置响应头，控制浏览器下载该文件
			if (RequestUtil.getRequest().getHeader("User-Agent").toLowerCase().indexOf("firefox") > 0) {
				// 4.1firefox浏览器
				response.setHeader("content-disposition", "attachment;filename="
						+ new String(uuid.getBytes(CodecUtil.UTF8_CHARSET), DataBaseCode.ISO_8859_1.getCode()));
			} else {
				// 4.2其它浏览器
				response.setHeader("content-disposition", "attachment;filename="
						+ Base64.getEncoder().encodeToString(uuid.getBytes(CodecUtil.UTF8_CHARSET)));
			}
			response.setContentType("APPLICATION/OCTET-STREAM");
			//读取要下载的文件，保存到文件输入流
			in = new BufferedInputStream(new FileInputStream(file_path));
			// 创建输出流
			out = response.getOutputStream();
			//将输入流转换成byte,写入到浏览器中
			byte[] bytes = new byte[10 * 1024 * 1024];
			int read;
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
				out.flush();
			}
		} catch (IOException e) {
			response.setStatus(500);
			e.printStackTrace();
		}
		return response;
	}

	public Map<String, Object> fileInfo(String uuid, Long user_id) {

		Interface_file_info info = new Interface_file_info();
		info.setUser_id(user_id);
		info.setFile_id(uuid);
		return Dbo.queryOneObject("SELECT * FROM " + Interface_file_info.TableName +
				" WHERE file_id = ? AND user_id=?", info.getFile_id(), info.getUser_id());
	}

}
