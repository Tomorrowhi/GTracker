/**
 *
 */
package com.bct.gpstracker.vo;

/**
 * @author lijunqi
 */
public enum CmdType {

    MO("模式转换","mo"),
    TE("同步时间","te"),
    TK("即时定位","tk"),
    TT("定时定位","tt"),
    OF("开关机","of"),
    RT("重启","rt"),
    ST("初始化","st"),
    PO("省电开关机","po"),
    IP("IP及端口","ip"),
    UP("远程升级","up"),
    YS("确认紧急报警","ys"),
    NS("解除紧急报警","ns"),
    HJ("呼叫","hj"),
    BV("栅栏报警","bv"),
    CL("休眠(免打扰)时段","cl"),
    PA("密码","pa"),
    EM("授权号","em"),
    CT("授权号","ct"),
    CUSTOM("自定义","custom"),
    VO("音量","vo"),
    HF("查询话费","hf"),
    RC("远程录音","rc"),
    BL("蓝牙控制","bl");

    private String name;

    private String type;

    private CmdType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }
    public String getType() {
        return type;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return type;
    }

    public static CmdType getType(String value) {
        CmdType result =null;
        for (CmdType c : CmdType.values()) {
            if (c.getType().equals(value)) {
                result = c;
            }
        }
        return result;
    }
}
