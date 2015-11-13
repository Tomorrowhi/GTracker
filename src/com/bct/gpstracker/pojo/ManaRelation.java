package com.bct.gpstracker.pojo;

import java.io.Serializable;

import android.support.annotation.NonNull;

/**
 * Created by Administrator on 15-9-8.
 * 管理界面操作对象的基类
 */
public class ManaRelation implements Comparable<ManaRelation>,Serializable{

    protected String authDiscrible;//用于描叙各个关系账户相对于当前用户所扮演的角色

    protected Integer sort;

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getAuthDiscrible() {
        return authDiscrible;
    }

    public void setAuthDiscrible(String authDiscrible) {
        this.authDiscrible = authDiscrible;
    }

    @Override
    public int compareTo(@NonNull ManaRelation rhs) {
        if(this.getSort()==null || rhs.getSort() ==null){
            return 1;
        }
        int i=this.getSort().compareTo(rhs.getSort());
        if(i!=0){
            return i;
        }
        if(this.getAuthDiscrible()==null||rhs.getAuthDiscrible()==null){
            return 1;
        }
        return this.getAuthDiscrible().compareTo(rhs.getAuthDiscrible());
    }
}
