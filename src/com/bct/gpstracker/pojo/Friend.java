package com.bct.gpstracker.pojo;

import java.io.Serializable;

import com.bct.gpstracker.util.CommUtil;
import com.bct.gpstracker.vo.TermType;

public class Friend implements Serializable,Comparable<Friend>{
	private static final long serialVersionUID = -6053507440740958222L;

    private Long id;
	private Long friendId;
    private Integer sex;//0 未知，1 男，2 女，3 中性
    private String name;
    private String nickName;
    private Integer groupId;
    private Integer focus;//1：关注，0：未关注，-1：屏蔽
    private Boolean online=true;
    private String lastMsg;
    private String lastPost;
    private String photo;
    private String sortLetters;
    private String imei;
    private String phone;
    private Long lastConnectTime;
    private Long lastLoadTime;
    private TermType termType;

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getFocus() {
        return focus;
    }

    public void setFocus(Integer focus) {
        this.focus = focus;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(String lastMsg) {
        this.lastMsg = lastMsg;
    }

    public String getLastPost() {
        return lastPost;
    }

    public void setLastPost(String lastPost) {
        this.lastPost = lastPost;
    }

    public Long getLastConnectTime() {
        return lastConnectTime;
    }

    public void setLastConnectTime(Long lastConnectTime) {
        this.lastConnectTime = lastConnectTime;
    }

    public Long getLastLoadTime() {
        return lastLoadTime;
    }

    public void setLastLoadTime(Long lastLoadTime) {
        this.lastLoadTime = lastLoadTime;
    }

    public String getSortLetters() {
        return sortLetters;
    }

    public void setSortLetters(String sortLetters) {
        this.sortLetters = sortLetters;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Friend{");
        sb.append("id=").append(id);
        sb.append(", friendId=").append(friendId);
        sb.append(", sex=").append(sex);
        sb.append(", name='").append(name).append('\'');
        sb.append(", nickName='").append(nickName).append('\'');
        sb.append(", groupId=").append(groupId);
        sb.append(", focus=").append(focus);
        sb.append(", online=").append(online);
        sb.append(", lastMsg='").append(lastMsg).append('\'');
        sb.append(", lastPost='").append(lastPost).append('\'');
        sb.append(", photo='").append(photo).append('\'');
        sb.append(", sortLetters='").append(sortLetters).append('\'');
        sb.append(", imei='").append(imei).append('\'');
        sb.append(", phone='").append(phone).append('\'');
        sb.append(", lastConnectTime=").append(lastConnectTime);
        sb.append(", lastLoadTime=").append(lastLoadTime);
        sb.append(", termType=").append(termType);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Friend friend = (Friend) o;

        if (id != null ? !id.equals(friend.id) : friend.id != null) return false;
        if (friendId != null ? !friendId.equals(friend.friendId) : friend.friendId != null) return false;
        if (sex != null ? !sex.equals(friend.sex) : friend.sex != null) return false;
        if (name != null ? !name.equals(friend.name) : friend.name != null) return false;
        if (nickName != null ? !nickName.equals(friend.nickName) : friend.nickName != null) return false;
        if (groupId != null ? !groupId.equals(friend.groupId) : friend.groupId != null) return false;
        if (focus != null ? !focus.equals(friend.focus) : friend.focus != null) return false;
        if (online != null ? !online.equals(friend.online) : friend.online != null) return false;
        if (lastMsg != null ? !lastMsg.equals(friend.lastMsg) : friend.lastMsg != null) return false;
        if (lastPost != null ? !lastPost.equals(friend.lastPost) : friend.lastPost != null) return false;
        if (photo != null ? !photo.equals(friend.photo) : friend.photo != null) return false;
        if (sortLetters != null ? !sortLetters.equals(friend.sortLetters) : friend.sortLetters != null) return false;
        if (imei != null ? !imei.equals(friend.imei) : friend.imei != null) return false;
        if (phone != null ? !phone.equals(friend.phone) : friend.phone != null) return false;
        if (lastConnectTime != null ? !lastConnectTime.equals(friend.lastConnectTime) : friend.lastConnectTime != null) return false;
        if (lastLoadTime != null ? !lastLoadTime.equals(friend.lastLoadTime) : friend.lastLoadTime != null) return false;
        return termType == friend.termType;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (friendId != null ? friendId.hashCode() : 0);
        result = 31 * result + (sex != null ? sex.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
        result = 31 * result + (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (focus != null ? focus.hashCode() : 0);
        result = 31 * result + (online != null ? online.hashCode() : 0);
        result = 31 * result + (lastMsg != null ? lastMsg.hashCode() : 0);
        result = 31 * result + (lastPost != null ? lastPost.hashCode() : 0);
        result = 31 * result + (photo != null ? photo.hashCode() : 0);
        result = 31 * result + (sortLetters != null ? sortLetters.hashCode() : 0);
        result = 31 * result + (imei != null ? imei.hashCode() : 0);
        result = 31 * result + (phone != null ? phone.hashCode() : 0);
        result = 31 * result + (lastConnectTime != null ? lastConnectTime.hashCode() : 0);
        result = 31 * result + (lastLoadTime != null ? lastLoadTime.hashCode() : 0);
        result = 31 * result + (termType != null ? termType.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Friend fd) {

        if (CommUtil.isNotBlank(this.getSortLetters())&&CommUtil.isBlank(fd.getSortLetters())) {
            return -1;
        }
        if (CommUtil.isBlank(this.getSortLetters())&&CommUtil.isNotBlank(fd.getSortLetters())) {
            return 1;
        }
        int res = getSortLetters().toUpperCase().compareTo(fd.getSortLetters().toUpperCase());
        if (res == 0) {
            return getName().compareTo(fd.getName());
        } else {
            return res;
        }
    }
}
