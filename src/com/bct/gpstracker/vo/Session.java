package com.bct.gpstracker.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.content.SharedPreferences;

import com.amap.api.maps.model.LatLng;
import com.bct.gpstracker.AppContext;
import com.bct.gpstracker.MainActivity;
import com.bct.gpstracker.common.MyConstants;
import com.bct.gpstracker.pojo.*;
import com.bct.gpstracker.ui.LoginActivity;
import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.util.Utils;
import com.lurencun.android.encrypt.HashEncrypt;
import com.lurencun.android.system.DeviceIdentify;


public class Session extends Observable{
	private static Session instance = null;
	
	public synchronized static Session getInstance() {
        if (instance == null) {
            synchronized (Session.class) {
                if (instance == null) {
                    instance = new Session();
                }
            }
        }
        return instance;
    }

    private User user;
	private boolean logOut;
	private boolean isUpdate = false;
	//private String accessKey = "";
	private boolean isFirst = true;
	private LoginActivity loginActivity;
	private String location = null;
	private LatLng latLng = null;
	private int hasNew = 0;
	private MapEntity mapEntity;
	private MainActivity mainActivity;
    private int iFlag;
    private String currentTime;
//    private List<Device> devices = new ArrayList<>();//终端
    private List<Device> monitors = new ArrayList<>();//监护对象
    private List<Keeper> userList = new ArrayList<>();//管理员，监护人
    private List<TermFriend> friendList =new ArrayList<>();

    private boolean isNeedClose = false;
    private String gsmCellInfo;
    private String wifiInfo;
    private Device device;
	private FenceEntity setupfence;
	private Device setupDevice;
    private String clientId;
    private String accessKey = "";
    private String imei;


    public void clearData(){	//退出的时候清理数据
    	this.user =null;
    	this.device = null;
    	this.monitors.clear();
    	this.userList.clear();
        this.friendList.clear();
        this.mainActivity=null;
    }


    public List<TermFriend> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<TermFriend> friendList) {
        this.friendList = friendList;
    }

    public List<Device> getMonitors() {
        return monitors;
    }

    public void setMonitors(List<Device> monitors) {
        this.monitors = monitors;
    }

    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	public boolean isLogOut() {
		return logOut;
	}

	public void setLogOut(boolean logOut) {
		this.logOut = logOut;
	}

//	public String getAccessKey() {
//		return accessKey;
//	}
//
//	public void setAccessKey(String accessKey) {
//		this.accessKey = accessKey;
//	}
	
	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public LoginActivity getLoginActivity() {
		return loginActivity;
	}

	public void setLoginActivity(LoginActivity loginActivity) {
		this.loginActivity = loginActivity;
	}
	
    public String getDeviceID(){
    	return HashEncrypt.encode(HashEncrypt.CryptType.MD5, DeviceIdentify.PUID());
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getHasNew() {
		return hasNew;
	}

	public void setHasNew(int hasNew) {
		this.hasNew = hasNew;
	}

	public MapEntity getMapEntity() {
		return mapEntity;
	}

	public void setMapEntity(MapEntity mapEntity) {
		this.mapEntity = mapEntity;
	}

	public MainActivity getMainActivity() {
		return mainActivity;
	}

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
	}

	public int getiFlag() {
		return iFlag;
	}

	public void setiFlag(int iFlag) {
		this.iFlag = iFlag;
	}

	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

//	public List<Device> getDevices() {
//		return devices;
//	}

//	public void setDevices(List<Device> devices) {
//		this.devices = devices;
//	}

	public boolean isNeedClose() {
		return isNeedClose;
	}

	public void setNeedClose(boolean isNeedClose) {
		this.isNeedClose = isNeedClose;
	}

	public List<Keeper> getUserList() {
		return userList;
	}

	public void setUserList(List<Keeper> userList) {
		this.userList = userList;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}

	public String getGsmCellInfo() {
		return gsmCellInfo;
	}

	public void setGsmCellInfo(String gsmCellInfo) {
		this.gsmCellInfo = gsmCellInfo;
	}

	public String getWifiInfo() {
		return wifiInfo;
	}

	public void setWifiInfo(String wifiInfo) {
		this.wifiInfo = wifiInfo;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public Device getSetupDevice() {
		return setupDevice;
	}

	public void setSetupDevice(Device setupDevice) {
		this.setupDevice = setupDevice;
	}

	public FenceEntity getSetupfence() {
		return setupfence;
	}

	public void setSetupfence(FenceEntity setupfence) {
		this.setupfence = setupfence;
	}

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public Device getDevice(String imei) {
		if(CommUtil.isNotEmpty(monitors)) {
			for (Device dvi : monitors) {
				if (dvi.getImei().equals(imei)) {
					return dvi;
				}
			}
		}
        return null;
    }

	public MapEntity getMapEntityByImei(String imei) {
		Device device=getDevice(imei);
        Keeper keeper;
		MapEntity mapEntity=new MapEntity();
		mapEntity.setImei(imei);
		if(device!=null){
			mapEntity.setName(device.getName());
            mapEntity.setTermType(TermType.WATCH);
            mapEntity.setPortrait(device.getPortrait());
            return mapEntity;
		}else if((keeper=queryKeeper(imei))!=null){
            mapEntity.setName(keeper.getNickName());
            mapEntity.setTermType(TermType.APP);
            mapEntity.setPortrait(keeper.getPortrait());
        }else{
			mapEntity.setName(imei);
            mapEntity.setTermType(TermType.APP);
        }
		return mapEntity;
	}

    public long getLoginedUserId(){
        long userId;
		if (user != null && user.getId() != null) {
			userId = user.getId();
		} else {
			SharedPreferences mSharedPreferences = Utils.getPreferences(AppContext.getContext());
            userId = mSharedPreferences.getLong(MyConstants.USER_ID, 0);
        }
        return userId;
    }

    public ManaRelation getAbstractUser(String imei) {
        Device device=getDevice(imei);
        if(device!=null){
            return device;
        }
        return queryKeeper(imei);
    }

    private Keeper queryKeeper(String imei) {
        for (Keeper keeper : userList) {
            if (keeper.getImei() != null && keeper.getImei().equals(imei)) {
                return keeper;
            }
        }
        return null;
    }

	@Override
	public void setChanged() {
		super.setChanged();
	}
}
