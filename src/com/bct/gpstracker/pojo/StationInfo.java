package com.bct.gpstracker.pojo;

import java.io.Serializable;

/**
 * @author 作者 E-mail:黄飞  353240166@qq.com
 * @version 创建时间：2015年2月7日 上午11:54:27
 * 类说明:基站信息实体类
 */
public class StationInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8450626697523196232L;
	
	private String cid;	//基站ID
	private String lac;	//位置区域码
	private String signalStrength;//信号强度
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getLac() {
		return lac;
	}
	public void setLac(String lac) {
		this.lac = lac;
	}
	public String getSignalStrength() {
		return signalStrength;
	}
	public void setSignalStrength(String signalStrength) {
		this.signalStrength = signalStrength;
	}
	
}
