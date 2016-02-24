/**
 * @author kimi
 * @dateTime 2013-4-28 下午4:07:25
 */
package com.nfl.kfb.shop;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * MMIAP支付服务器通知支付结果信息bean
 * <p>
 * 对于需要校验的字段，可以在对应属性名称的set方法中进行
 * </p>
 * @author kimi
 * @dateTime 2013-4-28 下午4:07:25
 */
@XmlRootElement(name = "SyncAppOrderReq", namespace = "http://www.monternet.com/dsmp/schemas/")
public class SyncAppOrderReq implements Serializable {

	/**
	 * @author kimi
	 * @dateTime 2013-4-28 上午11:47:08
	 */
	private static final long serialVersionUID = -5719870901076921628L;

	private String msgType;//消息类型

	private String version;//版本号

	private Device send_Address;//发送方地址

	private Device dest_Address;//接收方地址

	private String orderID;//订单编号

	private Integer checkID;//开放平台订购，鉴权接口中的CheckID

	private String tradeID;//外部交易ID

	private Integer price;//业务资费(分)

	private String actionTime;//操作时间

	private Integer actionID;//操作代码

	private String MSISDN;//目标用户手机号码

	private String feeMSISDN;//计费手机号码

	private String appID;//应用ID

	private String programID;//应用程序包ID

	private String payCode;//应用计费点编码

	private Integer totalPrice;//订购总价(分)

	private Integer subsNumb;//订购关系个数

	private Integer subsSeq;//档次同步的序号

	private String channelID;//渠道ID

	private String exData;//应用自定义参数

	public String getMsgType() {
		return msgType;
	}

	@XmlElement(name = "MsgType", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getVersion() {
		return version;
	}

	@XmlElement(name = "Version", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setVersion(String version) {
		this.version = version;
	}

	public Device getSend_Address() {
		return send_Address;
	}

	@XmlElement(name = "Send_Address", type = Device.class, namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setSend_Address(Device send_Address) {
		this.send_Address = send_Address;
	}

	public Device getDest_Address() {
		return dest_Address;
	}

	@XmlElement(name = "Dest_Address", type = Device.class, namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setDest_Address(Device dest_Address) {
		this.dest_Address = dest_Address;
	}

	public String getOrderID() {
		return orderID;
	}

	@XmlElement(name = "OrderID", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public Integer getCheckID() {
		return checkID;
	}

	@XmlElement(name = "CheckID", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setCheckID(Integer checkID) {
		this.checkID = checkID;
	}

	public String getTradeID() {
		return tradeID;
	}

	@XmlElement(name = "TradeID", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setTradeID(String tradeID) {
		this.tradeID = tradeID;
	}

	public Integer getPrice() {
		return price;
	}

	@XmlElement(name = "Price", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getActionTime() {
		return actionTime;
	}

	@XmlElement(name = "ActionTime", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setActionTime(String actionTime) {
		this.actionTime = actionTime;
	}

	public Integer getActionID() {
		return actionID;
	}

	@XmlElement(name = "ActionID", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setActionID(Integer actionID) {
		this.actionID = actionID;
	}

	public String getMSISDN() {
		return MSISDN;
	}

	@XmlElement(name = "MSISDN", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setMSISDN(String mSISDN) {
		MSISDN = mSISDN;
	}

	public String getFeeMSISDN() {
		return feeMSISDN;
	}

	@XmlElement(name = "FeeMSISDN", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setFeeMSISDN(String feeMSISDN) {
		this.feeMSISDN = feeMSISDN;
	}

	public String getAppID() {
		return appID;
	}

	@XmlElement(name = "AppID", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setAppID(String appID) {
		this.appID = appID;
	}

	public String getProgramID() {
		return programID;
	}

	@XmlElement(name = "ProgramID", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setProgramID(String programID) {
		this.programID = programID;
	}

	public String getPayCode() {
		return payCode;
	}

	@XmlElement(name = "PayCode", namespace = "http://www.monternet.com/dsmp/schemas/")
	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public Integer getTotalPrice() {
		return totalPrice;
	}

	@XmlElement(name = "TotalPrice", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setTotalPrice(Integer totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Integer getSubsNumb() {
		return subsNumb;
	}

	@XmlElement(name = "SubsNumb", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setSubsNumb(Integer subsNumb) {
		this.subsNumb = subsNumb;
	}

	public Integer getSubsSeq() {
		return subsSeq;
	}

	@XmlElement(name = "SubsSeq", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setSubsSeq(Integer subsSeq) {
		this.subsSeq = subsSeq;
	}

	public String getChannelID() {
		return channelID;
	}

	@XmlElement(name = "ChannelID", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getExData() {
		return exData;
	}

	@XmlElement(name = "ExData", namespace = "http://www.monternet.com/dsmp/schemas/", required = false)
	public void setExData(String exData) {
		this.exData = exData;
	}
}
