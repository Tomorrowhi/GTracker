package com.bct.gpstracker.pojo;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by HH
 * Date: 2015/7/29 0029
 * Time: 下午 6:22
 */
@Table(name="setting")
public class Setting {
    @Column(column = "id")
    private Long id;
    @Column(column = "key")
    private String key;
    @Column(column = "value")
    private String value;

    public Setting() {
    }

    public Setting(String key, String value) {
        this.key=key;
        this.value=value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
