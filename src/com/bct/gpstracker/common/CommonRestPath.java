package com.bct.gpstracker.common;

public class CommonRestPath {

    /**
     * 发送异常崩溃日志
     */
    public static String sendExFile() {
        return "/exception/addExFileToDb";
    }

    /**
     * 获得表情列表
     */
    public static String GetEmojiList() {
        return "/appEmoticon/query";
    }

    /**
     * 设置表情
     */
    public static String SetTerminalEmoji() {
        return "/appEmoticon/download";
    }


    /**
     * 查询表情版本
     */
    public static String AppEmoticonVersion() {
        return "/appEmoticon/verifyVersion";
    }


    /**
     * 查询好友信息
     */
    public static String FriendsQuery() {
        return "/friend/query";
    }

    /**
     * 删除好友
     */
    public static String FriendsDelete() {
        return "/friend/delete";
    }

    /**
     * 添加/修改好友信息
     */
    public static String FriendsEdit() {
        return "/friend/edit";
    }

    /**
     * 录音指令
     */
    public static String RecordSound() {
        return "/remoteCtrl/recordSound";
    }

    /**
     * 发现查询
     */
    public static String FoundQuery() {
        return "/foundBaby/query";
    }

    /**
     * 宝宝圈查询
     */
    public static String baByGroupQuery() {
        return "/babyRing/query";
    }

    /**
     * 宝宝圈发表消息
     */
    public static String baByGroupAddPublish() {
        return "/babyRing/addPublish";
    }

    /**
     * 宝宝圈发表图片
     */
    public static String baByGroupAddPic() {
        return "/babyRing/addPic";
    }

    /**
     * 宝宝圈添加评论
     */
    public static String baByGroupAdd() {
        return "/babyRing/add";
    }

    /**
     * 宝宝圈删除评论
     */
    public static String baByGroupDelete() {
        return "/babyRing/delete";
    }

    /**
     * 用户验证
     */
    public static String checkPhone() {
        return "/user/check";
    }

    /**
     * 用户注册
     */
    public static String register() {
        return "/user/register";
    }

    /**
     * 用户登录
     */
    public static String login() {
        return "/user/login";
    }

    /**
     * 用户退出
     */
    public static String logout() {
        return "/user/logout";
    }

    /**
     * 用户修改密码
     */
    public static String modifyPwd() {
        return "/user/modifypwd";
    }

    /**
     * 发送验证码
     */
    public static String validCode() {
        return "/user/validcode";
    }

    /**
     * 所有定位
     */
    public static String getTrackall() {
        return "/position/trackall";
    }

    /**
     * 单个定位
     */
    public static String getTrack() {
        return "/position/track";
    }


    /**
     * 设备的编辑和添加
     */
    public static String deviceAdd() {
        return "/terminal/edit";
    }


    /**
     * 设备的删除
     */
    public static String deleteDevice() {
        return "/terminal/delete";
    }

    /**
     * 查询终端的绑定状态
     * @return
     */
    public static String queryDeviceStatus() {
        return "/terminal/getTermStates";
    }


    /**
     * 发送语音
     */
    public static String sendVoice() {
        return "/appVoice/send";
    }

    /**
     * 发送验证码
     */
    public static String pushInformation() {
        return "/user/pushInformation";
    }

    /**
     * 获取所有的设备
     */
    public static String getDeviceList() {
        return "/terminal/query";
    }

    /**
     * 获取电子围栏
     */
    public static String getFenceList() {
        return "/fence/query";
    }

    /**
     * 编辑或新增电子围栏
     */
    public static String fenceAdd() {
        return "/fence/edit";
    }

    /**
     * 删除电子围栏
     */
    public static String fenceDelete() {
        return "/fence/delete";
    }

    /**
     * 发送指令
     */
    public static String sendCommand() {
        return "/position/commandSend";
    }


    /**
     * 获取版本信息
     */
    public static String getVersion() {
        return "/version/update?platform=1";
    }

    /**
     * 获取消息开关现在状态
     */
    public static String getMessageSwitch() {
        return "/position/messageSwitchQuery";
    }

    /**
     * 设置消息开关
     */
    public static String setMessageSwitch() {
        return "/position/messageSwitch";
    }

    /**
     * 上传吐槽信息
     */
    public static String sendFeedback() {
        return "/aboutUs/gagSend";
    }

    /**
     * 监护人的添加
     */
    public static String userADD() {
        return "/identityUser/edit";
    }


    /**
     * 监护人的移除
     */
    public static String DeleteUser() {
        return "/identityUser/delete";
    }


    /**
     * 获取所有的监护人
     */
    public static String getUserList() {
        return "/identityUser/query";
    }

    public static String querytMonitorObject() {
        return "/monitorObjct/query";
    }

    public static String editMonitorObject() {
        return "/monitorObjct/edit";
    }

    /**
     * 监护对象的删除
     */
    public static String deleteMonitorObjct() {
        return "/monitorObjct/delete";
    }


    /**
     * APP定位信息上传
     */
    public static String postPosition() {
        return "/position/appPosition";
    }

    /**
     * 历史轨迹
     */
    public static String historyLocation() {
        return "/locus/query";
    }

    /**
     * 位置纠偏
     *
     * @return
     */
    public static String reCorrect() {
        return "/position/rectifying";
    }

    /**
     * 查询生活助手列表
     */
    public static String alarmQuery() {
        return "/alarmClock/query";
    }

    /**
     * 生活助手编辑
     *
     * @return
     */
    public static String alarmAdd() {
        return "/alarmClock/edit";
    }

    /**
     * 生活助手删除
     *
     * @return
     */
    public static String alarmDelete() {
        return "/alarmClock/delete";
    }

    public static String wifiEdit() {
        return "/wifi/edit";
    }

    public static String wifiQuery() {
        return "/wifi/query";
    }

    public static String restTimeDuration() {
        return "/position/getCommandDataByImeiAndType";
    }

    public static String audioList() {
        return "/appAudio/query";
    }

    public static String sendAudio() {
        return "/appAudio/download";
    }

    public static String audioSelectedList() {
        return "/appAudio/queryTermAudio";
    }

    public static String audioDownloadedList() {
        return "/alarmClock/queryTermAudio";
    }

}
