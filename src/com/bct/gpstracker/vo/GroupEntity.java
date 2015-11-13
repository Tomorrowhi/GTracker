package com.bct.gpstracker.vo;

/**
 * Created by HH
 * Date: 2015/10/26 0026
 * Time: 上午 10:45
 */
public class GroupEntity {
    private int groupId;
    private String groupName;

    public GroupEntity() {
    }

    public GroupEntity(int groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
