package server.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.protobuf.InvalidProtocolBufferException;

import common.LogHelper;
import data.AccountAction;
import database.DBManager;
import database.DBUtil;
import database.DataQueryResult;
import define.constant.CodeValidConst;
import http.HttpUtil;
import packet.CocoPacket;
import protobuf.Common;
import protobuf.creator.CommonCreator;
import protocol.c2s.RequestCode;
import server.LoginServer;
import server.ip.IP;
import server.response.LoginResponse;
import util.ASObject;
import util.MiscUtil;
import util.Pair;
import util.Randomizer;
import util.XProperties;

/**
 * Created by Administrator on 2017/2/6.
 */
public class LoginHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
	private Map<String, Integer> tokenMap = new HashMap<>();

//	private static AtomicInteger flowCount = new AtomicInteger(1000000000);

	public class ValidCodeInfo {
		public int validCode;
		public int geneTime;
		public int id;
		public int status = -1;
	}

	@Override
	public void handle(String s, Request request, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
		if (s.equals("/fast_login")) {
			handleFastLogin(httpServletRequest, httpServletResponse);
		} else if (s.equals("/user_login")) {
			handleUserLogin(httpServletRequest, httpServletResponse);
		} else if (s.equals("/valid_login")) {
			handleValidLogin(httpServletRequest, httpServletResponse);
		} else if (s.equals("/gene_code")) {
			handleGainValidCode(httpServletRequest, httpServletResponse);
		} else if (s.equals("/retrieve")) {
			handleRetrieveCode(httpServletRequest, httpServletResponse);
//		} else if (s.equals("/check_valid_code")) {
//			handleCheckValidCode(httpServletRequest, httpServletResponse);
		} else if (s.equals("/url_download")) {
			handleUrlDownload(httpServletRequest, httpServletResponse);
		}
	}

	private void handleUrlDownload(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
		String new_version = httpServletRequest.getParameter("new_version");
		String plat = httpServletRequest.getParameter("plat");
		String package_id = httpServletRequest.getParameter("package_id");

		logger.info("handleUrlDownload new_version:{} plat:{} package_id:{}", new_version, plat, package_id);
		if (0 >= new_version.length() || 0 >= plat.length() || 0 >= package_id.length()) {
			writeResponse(httpServletResponse, "failed_1");
			return;
		}

		int plat_value = Integer.valueOf(plat);
		int package_id_value = Integer.valueOf(package_id);

		Map<String, Object> where = new HashMap<>();
		where.put("plat", plat_value);
		where.put("package_id", package_id_value);
		List<ASObject> userList = DataQueryResult.load("conf_channel_switch", where);

		if (1 < userList.size()) {
			writeResponse(httpServletResponse, "failed_2");
			return;
		} else if (0 == userList.size()) {
			writeResponse(httpServletResponse, "failed_3");
			return;
		}

		int newVersion = Integer.parseInt(new_version);
		int reviewVersion = Integer.parseInt(userList.get(0).getString("review_version"));
		int curVersion = Integer.parseInt(userList.get(0).getString("cur_version"));

		if (newVersion >= reviewVersion) {
			writeResponse(httpServletResponse, "noupdate");
			if (newVersion > reviewVersion) {
				logger.error("handleUrlDownload 登陆发现异常版本 {} 大于审核版本 {}  plat：{} package_id：{}", newVersion, reviewVersion, plat_value, package_id_value);
			}
			return;
		}

		if (newVersion < curVersion) {
			writeResponse(httpServletResponse, userList.get(0).getString("url_download"));
		} else if (newVersion == curVersion) {
			writeResponse(httpServletResponse, "go");
		} else {
			writeResponse(httpServletResponse, "noupdate");
			logger.error("handleUrlDownload 登陆发现异常版本 {} 大于当前版本 {} 小于审核版本 {}  plat：{} package_id：{}", newVersion, curVersion, reviewVersion, plat_value, package_id_value);
		}

//		if (true == new_version.equals(userList.get(0).getString("review_version"))) {
//			writeResponse(httpServletResponse, "noupdate");
//			return;
//		}
//
//		if (false == new_version.equals(userList.get(0).getString("new_version"))) {
//			writeResponse(httpServletResponse, userList.get(0).getString("url_download"));
//			return;
//		}
//		writeResponse(httpServletResponse, "go");
	}

	private ValidCodeInfo loadValidCodeInfo(String phoneNum) {
		List<ASObject> codeList = DataQueryResult.load("SELECT id,valid_code,`status`,create_time FROM valid_code WHERE id = (SELECT MAX(id) FROM valid_code WHERE phone_num = '" + phoneNum + "')");
		if (codeList != null && codeList.size() > 0) {
			ValidCodeInfo codeInfo = new ValidCodeInfo();
			ASObject obj = codeList.get(0);
			codeInfo.id = obj.getInt("id");
			codeInfo.validCode = obj.getInt("valid_code");
			codeInfo.status = obj.getInt("status");
			codeInfo.geneTime = obj.getInt("create_time");
			return codeInfo;
		}
		return null;
	}

//	private void handleCheckValidCode(HttpServletRequest request, HttpServletResponse response) {
//		String phoneNum = request.getParameter("phoneNum");
//		String code = request.getParameter("code");
//
//		logger.info("handleCheckValidCode phoneNum:{} code:{}", phoneNum, code);
//		if (null == phoneNum || 0 == phoneNum.length() || null == code || 0 == code.length()) {
//			logger.error("handleCheckValidCode recv_data:" + request.toString());
//			return;
//		}
//		ValidCodeInfo info = loadValidCodeInfo(phoneNum);
//		if (info == null) {
//			logger.error("handleCheckValidCode failed_0 -> CodeMap not have phoneNum:", phoneNum);
//			writeResponse(response, "failed_0");
//			return;
//		}
//		if (MiscUtil.getCurrentSeconds() - info.geneTime > CodeValidConst.EXPIRE_TIME) {
//			logger.error("handleCheckValidCode failed_2 -> info.geneTime:{}", info.geneTime);
//			writeResponse(response, "failed_2");
//			return;
//		}
//		if (info.validCode != Integer.parseInt(code)) {
//			logger.error("handleCheckValidCode failed_1 -> info.validCode:{}", info.validCode);
//			writeResponse(response, "failed_1");
//			return;
//		}
//		writeResponse(response, "success");
//	}


	private void handleRetrieveCode(HttpServletRequest request, HttpServletResponse response) {
		String resultCode = request.getParameter("abc");
		logger.info("handleUserLogin abc:{}", resultCode);

		Map<String, String> object = decrypted(resultCode);
		String phoneNum = object.get("phoneNum");
		String code = object.get("code");
		String password = object.get("password");
		String package_id = object.get("package_id");

		logger.info("handleRetrieveCode phoneNum:{} code:{} password:{} package_id:{}", phoneNum, code, password, package_id);

		String platform_id = LoginServer.getInst().getPlatformForPackageId(Integer.parseInt(package_id));
		logger.info("handleRetrieveCode platform_id：{}", platform_id);

		if (null == phoneNum || 0 == phoneNum.length() || null == code || 0 == code.length() ||
				null == password || 0 == password.length()) {
			logger.error("handleRetrieveCode recv_data:" + request.toString());
			return;
		}
		ValidCodeInfo info = loadValidCodeInfo(phoneNum);
		if (info == null) {
			logger.error("handleCheckValidCode failed_2 -> CodeMap not have phoneNum:", phoneNum);
			writeResponse(response, "failed_2");
			return;
		}
		if (info.validCode != Integer.parseInt(code)) {
			logger.error("handleCheckValidCode failed_0 -> info.validCode:{}", info.validCode);
			writeResponse(response, "failed_0");
			return;
		}
		if (MiscUtil.getCurrentSeconds() - info.geneTime > CodeValidConst.EXPIRE_TIME) {
			logger.error("handleCheckValidCode failed_3 -> info.geneTime:{}", info.geneTime);
			writeResponse(response, "failed_3");
			return;
		}
		Map<String, Object> where = new HashMap<>();
		where.put("platform_id", platform_id);
		where.put("phone_num", phoneNum);
		Map<String, Object> data = new HashMap<>();
		data.put("password", password);
		try {
			DBUtil.executeUpdate("accounts", where, data);
			writeResponse(response, "success");
		} catch (SQLException e) {
			writeResponse(response, "failed_1");
		}
	}

	private int randomPhoneValidCode() {
		return Randomizer.nextInt(900000) + 100000;
	}

	private void handleGainValidCode(HttpServletRequest request, HttpServletResponse response) {
		String phoneNum = request.getParameter("phoneNum");
		int type = Integer.valueOf(request.getParameter("type"));
		String package_id = request.getParameter("package_id");

		logger.info("handleGainValidCode phoneNum:{} type:{} package_id:{}", phoneNum, type, package_id);

		String platform_id = LoginServer.getInst().getPlatformForPackageId(Integer.parseInt(package_id));
		logger.info("handleGainValidCode platform_id：{}", platform_id);

		if (false == isPhoneNum(phoneNum, platform_id, type, response)) {
			logger.error("handleGainValidCode isPhoneNum() -> false");
			return;
		}
		ValidCodeInfo info = new ValidCodeInfo();
		info.validCode = randomPhoneValidCode();
		info.geneTime = MiscUtil.getCurrentSeconds();
//		info.id = flowCount.getAndIncrement();
//		LoginServer.getInst().addValidCodeInfo(phoneNum, info);
		XProperties properties = LoginServer.getInst().getProps();
		if (!LoginServer.getInst().isGeneSpare()) { // 正常地址
			String url = properties.getString("net_ease_url_gene", "https://api.netease.im/sms/sendcode.action");
			String appId = properties.getString("net_ease_app_key", "d26f4bc935ec0c31e7186742d9fe16b1");
			String content = "";
			if (type == 2) {
				content = String.format(properties.getString("message_template_1"), info.validCode);
			} else {
				content = String.format(properties.getString("message_template_2"), info.validCode);
			}
			logger.info("handleGainValidCode send_url :" + url);
			logger.info("handleGainValidCode phone num :" + phoneNum);
			logger.info("handleGainValidCode api key :" + appId);
			logger.info("handleGainValidCode the send content is:" + content);

			HttpUtil.sendPost(url, MiscUtil.newParamsMap("apikey", appId, "mobile", phoneNum, "text", content), e -> {
				int code = -1;
				String msg = "";
				logger.info("handleGainValidCode the yun pian return is {}", e);
				Gson gson = new Gson();
				try {
					JsonObject jsonRet = gson.fromJson(e.toString(), JsonObject.class);
					code = jsonRet.get("code").getAsInt();
					msg = jsonRet.get("msg").getAsString();
				} catch (JsonSyntaxException e1) {
					e1.printStackTrace();
				}
				if (code != 0) {
					writeResponse(response, "failed");
				} else {
					writeResponse(response, "success");
				}
				recordSendValiCode(phoneNum, code, info, msg);
			});
		} else { // 灾备备用地址
			String url = properties.getString("spare_gene_url", "http://sms.106jiekou.com/utf8/sms.aspx");
			String account = properties.getString("spare_gene_url_account", "yishengsuoai");
			String password = properties.getString("spare_gene_url_password", "Awqs12fa");
			String content = String.format(properties.getString("spare_gene_url_content"), info.validCode);
			logger.info("handleGainValidCode phone num :" + phoneNum);
			logger.info("handleGainValidCode spare_send_url :" + url);
			logger.info("handleGainValidCode spare_gene_url_account :" + account);
			logger.info("handleGainValidCode spare_gene_url_password :" + password);
			logger.info("handleGainValidCode spare_gene_url_content :" + content);
			try {
				content = URLEncoder.encode(content, "utf-8");
			} catch (UnsupportedEncodingException e2) {
				e2.printStackTrace();
			}
			HttpUtil.sendPost(url, MiscUtil.newParamsMap("account", account, "password", password, "mobile", phoneNum, "content", content), e -> {
				int code = -1;
				logger.info("handleGainValidCode the spare return is {}", e);
				code = Integer.parseInt(e.toString());
				if (code != 100) {
					writeResponse(response, "failed");
				} else {
					writeResponse(response, "success");
				}
				recordSendValiCode(phoneNum, code, info, "");
			});
		}
	}

	private void recordSendValiCode(String phoneNum, int code, ValidCodeInfo info, String msg) {
		Map<String, Object> data = new HashMap<>();
		data.put("phone_num", phoneNum);
		data.put("status", code);
		data.put("valid_code", info.validCode);
		data.put("create_time", info.geneTime);
		data.put("send_time", MiscUtil.getCurrentSeconds());
		data.put("msg", msg);
		try {
			DBUtil.executeInsert("valid_code", data);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean isPhoneNum(String phoneNum, String platform_id, int type, HttpServletResponse response) {
		if (null == phoneNum) {
			writeResponse(response, "failed_0");
			return false;
		}
		/// 判断当前手机号码是否有效
		if (null == phoneNum || 0 >= phoneNum.length()) {
			writeResponse(response, "failed");
			return false;
		}
		Map<String, Object> where = new HashMap<>();
		where.put("phone_num", phoneNum);
		where.put("platform_id", platform_id);
		List<ASObject> userList = DataQueryResult.load("accounts", where);
		/// 找回密码
		if (1 == type) {
			if (null == userList || 0 >= userList.size()) {
				writeResponse(response, "failed_1");
				return false;
			}
		}
		/// 绑定账号
		else if (2 == type) {
			if (null == userList || 1 <= userList.size()) {
				writeResponse(response, "failed_2");
				return false;
			}
		} else {
			writeResponse(response, "failed_0");
			return false;
		}

		return true;
	}

	private void writeResponse(HttpServletResponse response, String result) {
		response.setStatus(HttpStatus.OK_200);
		try {
			response.getWriter().write(result);
			response.getWriter().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleValidLogin(HttpServletRequest request, HttpServletResponse response) {
		String token = request.getParameter("token");
		Integer userId = tokenMap.get(token);
		if (userId == null) {
			return;
		}
		//存在的就返回
	}

	private void handleFastLogin(HttpServletRequest request, HttpServletResponse response) {
		String machineId = request.getParameter("machine_id");
//		String channel = request.getParameter("channel");  // 渠道id现在有可能变动  目前通过packageId查找
		String device = request.getParameter("device");
		String package_id = request.getParameter("package_id");
		String game_version = request.getParameter("game_version");
		String app_version = request.getParameter("app_version");

		logger.info("handleFastLogin machine_id:{} device:{} packageId:{} game_version:{} app_version:{}", machineId, device, package_id, game_version, app_version);

//		int channel = LoginServer.getInst().getChannelForPackageId(Integer.parseInt(package_id));
		int channel = 1;
		String platform_id = "";
//		String platform_id = LoginServer.getInst().getPlatformForPackageId(Integer.parseInt(package_id));
		logger.info("handleFastLogin platform_id:{} channel:{}", platform_id, channel);

		if (!LoginServer.getInst().isOpenLogin()) {
			writeResponse(response, "server_maintenance");
			logger.error("handleFastLogin 无法登陆 全局登陆开关为关闭状态");
			return;
		}

		if (machineId == null || /*channel == null ||*/ device == null) {
			logger.error("handleFastLogin 参数异常 machineId:{} device:{}", machineId, device);
			return;
		}

		/// 当前机器码是否已经注册
		if (true == isRegister(machineId, platform_id)) {
			logger.error("handleFastLogin 当前机器码已经注册");
			writeResponse(response, "machine_already");
			return;
		}

		String forward = request.getHeader("x-forwarded-for");
		logger.info("handleFastLogin the forward for is {}", forward);
		String host = forward == null ? request.getRemoteHost() : forward.split(",")[0];
		logger.info("handleFastLogin the host is {}", host);
		if (true == getBlockStatus(1, "", machineId, host)) {
			logger.error("handleFastLogin 相关信息被封禁 getBlockStatus() -> true");
			writeResponse(response, "failed_block");
			return;
		}

		Map<String, Object> where = new HashMap<>();
		where.put("machine_id", machineId);
		where.put("platform_id", platform_id);
		where.put("phone_num", "");
		List<ASObject> userList = DataQueryResult.load("accounts", where);
		//玩家没有账号信息, 创建玩家账号
		int userId = 0;
		String province = "";
		String city = "";
		int createTime = MiscUtil.getCurrentSeconds();
		if (userList.size() <= 0) {
			Pair<String, String> provinceAndCity = getProvinceAndCity(host);
			province = provinceAndCity.getLeft();
			if (LoginServer.getInst().getProvinceOpen(province)) {
				writeResponse(response, "server_maintenance");
				logger.error("handleUserLogin 无法登陆  {}区域维护开关为开启状态", province);
				return;
			}
			city = provinceAndCity.getRight();
			where.put("package_id", package_id);
			where.put("create_time", createTime);
			where.put("register_time", 0);
			where.put("platform_id", platform_id);
			where.put("channel", channel);
			where.put("login_time", createTime);
			where.put("create_ip", host);
			where.put("device", device);
			where.put("last_login_device", device);
			where.put("last_login_ip", host);
			where.put("last_login_machine", machineId);
			where.put("province", province);
			where.put("city", city);
			where.put("login_days", 1);
			try {
				userId = (int) DBUtil.executeInsert("accounts", where);
				LoginServer.getInst().getClient().sendRequest(new CocoPacket(RequestCode.LOG_ACCOUNT
						, LogHelper.logAccount(userId, createTime, channel, AccountAction.CREATE.getValue()
						, Integer.parseInt(package_id), host, province, city, device)));
			} catch (SQLException e) {
				logger.error("error:{}", e);
			}
		} else {
			int cur_time = MiscUtil.getCurrentSeconds();
			ASObject user = userList.get(0);
			userId = user.getInt("user_id");
			province = user.getString("province");
			city = user.getString("city");
			platform_id = user.getString("platform_id");
			if (LoginServer.getInst().getProvinceOpen(province)) {
				writeResponse(response, "server_maintenance");
				logger.error("handleUserLogin 无法登陆  {}区域维护开关为开启状态", province);
				return;
			}
			if (!MiscUtil.isSameDay(cur_time, user.getInt("login_time"))) {
				user.put("login_days", user.getInt("login_days") + 1);
			}
			user.put("last_login_ip", host);
			user.put("login_time", cur_time);
			try {
				DBUtil.executeUpdate("accounts", where, user);
			} catch (SQLException e) {
				logger.error("error :{}", e);
			}

		}
		sendCltResult(response, userId, province, city, String.valueOf(channel), device, package_id, host, createTime, machineId, game_version, app_version, platform_id);
	}


	private Pair<String, String> getProvinceAndCity(String host) {
		String[] result = new String[3];
		try {
			result = IP.find(host);
		} catch (Exception e) {
			logger.error("", e);
		} finally {
			logger.info("the Province and the City is {} ", result[1] == null ? "" : result[1], result[2] == null ? "" : result[2]);
		}
		if (result.length < 4) {
			return MiscUtil.newPair("", "");
		}
		return MiscUtil.newPair(result[1] == null ? "" : result[1], result[2] == null ? "" : result[2]);
	}

	private void handleUserLogin(HttpServletRequest request, HttpServletResponse response) {
		String code = request.getParameter("abc");
		logger.info("handleUserLogin abc:{}", code);

		Map<String, String> object = decrypted(code);
		String phone_num = object.get("phoneNum");
		String revPassword = object.get("password");
//		String channel = object.get("channel");    // 渠道id现在有可能变动  目前通过packageId查找
		String device = object.get("device");
		String machineId = object.get("machine_id");
		String package_id = object.get("package_id");
		String game_version = object.get("game_version");
		String app_version = String.valueOf(object.get("app_version"));

		logger.info("handleUserLogin phone_num:{} revPassword:{} device:{} machineId:{} packageId:{} game_version:{} app_version:{}", phone_num, revPassword, device, machineId, package_id, game_version, app_version);

		int channel = LoginServer.getInst().getChannelForPackageId(Integer.parseInt(package_id));
		String platform_id = LoginServer.getInst().getPlatformForPackageId(Integer.parseInt(package_id));
		logger.info("handleUserLogin platform_id:{} channel:{}", platform_id, channel);

		if (phone_num == null || revPassword == null || phone_num.equals("") || revPassword.equals("")) {
			logger.error("handleUserLogin the phone num or password is null ");
			writeResponse(response, "failed_password");
			return;
		}

		if (!LoginServer.getInst().isOpenLogin()) {
			writeResponse(response, "server_maintenance");
			logger.error("handleUserLogin 无法登陆 全局登陆开关为关闭状态");
			return;
		}

		String forward = request.getHeader("x-forwarded-for");
		logger.info("handleUserLogin the forward for is {}", forward);
		String host = forward == null ? request.getRemoteHost() : forward.split(",")[0];
		logger.info("handleUserLogin the phone num is {} and the remote address is {}", phone_num, request.getRemoteHost());

		Map<String, Object> where = new HashMap<>();
		where.put("phone_num", phone_num);
		where.put("platform_id", platform_id);
		List<ASObject> userList = DataQueryResult.load("accounts", where);
		if (userList.size() == 0) {
			writeResponse(response, "failed_no_player");
			return;
		}
		ASObject account = userList.get(0);
		String password = account.getString("password");
		if (!revPassword.equals(password)) {
			writeResponse(response, "failed_password");
			return;
		}

		if (true == getBlockStatus(2, phone_num, machineId, host)) {
			writeResponse(response, "failed_block");
			return;
		}

		if (userList.size() > 0) {
			int cur_time = MiscUtil.getCurrentSeconds();
			ASObject user = new ASObject();
			if (null == user) {
				writeResponse(response, "failed_user_info");
			}
			int userId = userList.get(0).getInt("user_id");
			int createTime = userList.get(0).getInt("create_time");
			String province = userList.get(0).getString("province");
			if (LoginServer.getInst().getProvinceOpen(province)) {
				writeResponse(response, "server_maintenance");
				logger.error("handleUserLogin 无法登陆  {}区域维护开关为开启状态", province);
				return;
			}
			String city = userList.get(0).getString("city");
			platform_id = userList.get(0).getString("platform_id");
			if (!MiscUtil.isSameDay(cur_time, userList.get(0).getInt("login_time"))) {
				user.put("login_days", userList.get(0).getInt("login_days") + 1);
			}
			user.put("last_login_ip", host);
			user.put("login_time", cur_time);
			try {
				DBUtil.executeUpdate("accounts", where, user);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			sendCltResult(response, userId, province, city, String.valueOf(channel), device, package_id, host, createTime, machineId, game_version, app_version, platform_id);
		}
	}

	private void sendCltResult(HttpServletResponse response, int userId, String province, String city, String channel, String device, String packageId, String loginIp, int createTime, String machineId, String game_version, String app_version, String platform_id) {
		logger.info("sendCltResult {} send login request to center", userId);
		String loginToken = MiscUtil.getMD5(System.currentTimeMillis() + userId + "");
		LoginServer.getInst().getClient().sendRequestSync(new CocoPacket(RequestCode.CENTER_DISPATCH_GATE
				, CommonCreator.createPBStringList(userId + ":" + loginToken, province, city, channel, device, packageId, loginIp, String.valueOf(createTime), machineId, game_version, app_version, platform_id)), e -> {
			logger.info("sendCltResult userId:{} e:{}", userId, e);
			if (e instanceof CocoPacket) {
				byte[] bytes = ((CocoPacket) e).getBytes();
				Common.PBString reply = null;
				try {
					reply = Common.PBString.parseFrom(bytes);
				} catch (InvalidProtocolBufferException e1) {
					e1.printStackTrace();
				}
				logger.info("sendCltResult the reply message is " + reply.getValue());
				Gson gson = new Gson();
				LoginResponse res = new LoginResponse();
				res.setAddress(reply.getValue());
				res.setToken(loginToken);
				writeResponse(response, gson.toJson(res));
			}
		});
	}

	public boolean isRegister(String machine_id, String platform_id) {
		if (machine_id.equals("")) {
			return false;
		}
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet rs = null;
		try {
			conn = DBManager.getConnection();
			String sql = "select * from accounts where machine_id = '" + machine_id + "' and platform_id = '" + platform_id + "' and length(phone_num) > 0";
			stat = conn.prepareStatement(sql);
			rs = stat.executeQuery();

			if (rs.next()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return true;
		} finally {
			DBManager.close(conn, stat, rs);
		}

		return false;
	}

	public boolean getBlockStatus(int flag, String phone_num, String machine_id, String ip) {
		boolean b_ip = false;
		boolean b_phone_num = false;
		boolean b_machine_id = false;

		/// 1.封IP 4.注册IP
		Map<String, Object> where = new HashMap<>();
		where.put("status", 1);
		where.put("info", ip);
		List<ASObject> list_objs = DataQueryResult.load("block", where);
		if (0 < list_objs.size()) {
			b_ip = true;

			if (true == isDelBlock(1, ip)) {
				b_ip = false;
			}
		}
		if (true == b_ip) {
			return true;
		}

		/// 2.封账号
		where.clear();
		list_objs.clear();
		where.put("status", 1);
		where.put("info", phone_num);
		list_objs = DataQueryResult.load("block", where);
		if (0 < list_objs.size()) {
			b_phone_num = true;

			if (true == isDelBlock(2, phone_num)) {
				b_phone_num = false;
			}
		}

		/// 3.封机器码
		where.clear();
		list_objs.clear();
		where.put("status", 1);
		where.put("info", machine_id);
		list_objs = DataQueryResult.load("block", where);
		if (0 < list_objs.size()) {
			b_machine_id = true;

			if (true == isDelBlock(3, machine_id)) {
				b_machine_id = false;
			}
		}

		return b_machine_id || b_phone_num;
	}

	public boolean isDelBlock(int flag, String info) {
		Connection conn = null;
		PreparedStatement stat = null;

		try {
			conn = DBManager.getConnection();
			String sql = "DELETE FROM block WHERE status = 1 AND info='" + info + "' AND end_time <" + MiscUtil.getCurrentSeconds();
			stat = conn.prepareStatement(sql);
			if (true == stat.execute()) {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBManager.close(conn, stat);
		}

		return false;
	}

	private static Map<String, String> decrypted(String code) {
		byte[] bytes = Base64.getDecoder().decode(code.getBytes());
		String content = new String(bytes, Charset.forName("utf-8"));
		Gson gson = new Gson();
		return gson.fromJson(content, Map.class);
	}
}
