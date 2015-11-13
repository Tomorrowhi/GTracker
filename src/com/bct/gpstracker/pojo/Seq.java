package com.bct.gpstracker.pojo;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 下午 6:35
 */
@Table(name="sqlite_sequence")
public class Seq {
    @Column(column = "name")
    private String tableName;
    @Column(column = "seq")
    private Long id;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
