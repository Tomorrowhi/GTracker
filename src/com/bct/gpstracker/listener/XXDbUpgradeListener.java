package com.bct.gpstracker.listener;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.exception.DbException;

/**
 * Created by HH
 * Date: 2015/7/15 0015
 * Time: 上午 11:55
 */
public class XXDbUpgradeListener implements DbUtils.DbUpgradeListener {
    @Override
    public void onUpgrade(DbUtils db, int oldVersion, int newVersion) {
        if(oldVersion==1&&oldVersion<newVersion){
            try {
                String sql="ALTER TABLE push_msg RENAME TO push_msg_old";
                db.execNonQuery(sql);
                sql="CREATE TABLE push_msg (id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,type  INTEGER NOT NULL,title  TEXT,msg  TEXT NOT NULL,upload_time  INTEGER,create_time  INTEGER)";
                db.execNonQuery(sql);
                sql="INSERT INTO push_msg (id, msg, upload_time, create_time) SELECT id, msg, warn_time, create_time FROM push_msg_old";
                db.execNonQuery(sql);
                sql="DROP TABLE push_msg_old";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=2;
        }
        if(oldVersion==2&&oldVersion<newVersion){
            try {
                String sql="alter table push_msg add column msg_state integer";
                db.execNonQuery(sql);
                sql="DROP TABLE IF EXISTS chat_msg";
                db.execNonQuery(sql);
                sql="CREATE TABLE chat_msg (id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,type  INTEGER,user_id  INTEGER,user_name  TEXT,local_url  TEXT,content  TEXT,time  INTEGER," +
                        "imei  TEXT,from_user  TEXT,is_send  INTEGER,term_type  INTEGER,remote_url  TEXT)";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=3;
        }
        if(oldVersion==3&&oldVersion<newVersion){
            try {
                String sql="alter table chat_msg add column duration integer";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=4;
        }
        if(oldVersion==4&&oldVersion<newVersion){
            try {
                String sql="CREATE TABLE setting (id  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,key  TEXT NOT NULL,value  TEXT)";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=5;
        }
        if(oldVersion==5&&oldVersion<newVersion){
            try {
                String sql="alter table push_msg add column imei text";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=6;
        }
        if(oldVersion==6&&oldVersion<newVersion){
            try {
                String sql="alter table chat_msg add column succ integer";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=7;
        }
        if(oldVersion==7&&oldVersion<newVersion){
            try {
                String sql="alter table chat_msg add column remote_org_url text";
                db.execNonQuery(sql);
                sql="alter table chat_msg add column local_org_url text";
                db.execNonQuery(sql);
                sql="alter table chat_msg add column file_size integer";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=8;
        }
        if(oldVersion==8&&oldVersion<newVersion){
            try {
                String sql="alter table chat_msg add column icon integer";
                db.execNonQuery(sql);
                sql="alter table chat_msg add column title text";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=9;
        }

        if(oldVersion==9&&oldVersion<newVersion){
            try {
                String sql="alter table chat_msg add column is_read integer";
                db.execNonQuery(sql);
            } catch (DbException e) {
                e.printStackTrace();
            }
            oldVersion=10;
        }
    }
}
