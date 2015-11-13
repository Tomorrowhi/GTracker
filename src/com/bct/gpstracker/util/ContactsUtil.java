package com.bct.gpstracker.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.bct.gpstracker.common.Constants;
import com.bct.gpstracker.pojo.Device;
import com.bct.gpstracker.pojo.Keeper;
import com.bct.gpstracker.vo.Session;

/**
 * Created by lc on 2015/9/29 0029.
 * 通讯录工具类
 */
public class ContactsUtil {

    private Context context;
    private ExecutorService executorService;

    private ContactsUtil(Context context) {
        this.context = context;
    }

    private static ContactsUtil instance = null;

    public static synchronized ContactsUtil getInstance(Context context) {
        if (instance == null) {
            instance = new ContactsUtil(context);
        }
        return instance;
    }

    /**
     * 根据号码查找联系人
     *
     * @param number
     * @throws Exception
     */
    public synchronized String getContactNameByNumber(String number) throws Exception {
        String name = "";
        Uri uri = Uri.parse("content://com.android.contacts/data/phones/filter/" + number);
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{android.provider.ContactsContract.Data.DISPLAY_NAME}, null, null, null);
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    /**
     * 添加联系人
     *
     * @throws Exception
     */
    public synchronized void addContact(String name, String phone) throws Exception {
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        long contactid = ContentUris.parseId(resolver.insert(uri, values));
        uri = Uri.parse("content://com.android.contacts/data");
//          添加姓名
        values.put("raw_contact_id", contactid);
        values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/name");
        values.put("data1", name);
        resolver.insert(uri, values);
        values.clear();
//           添加电话
        values.put("raw_contact_id", contactid);
        values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/phone_v2");
        values.put("data1", phone);
        resolver.insert(uri, values);
        values.clear();
//           添加Email
//        values.put("raw_contact_id", contactid);
//        values.put(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/email_v2");
//        values.put("data1", "1234120155@qq.com");
//        resolver.insert(uri, values);
    }


    /**
     * 根据号码返回id
     */
    public synchronized int getContacts(String phone) {
        Uri uri = Uri.parse("content://com.android.contacts/contacts"); // 访问所有联系人
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id"}, null, null, null);
        while (cursor.moveToNext()) {
            int contactsId = cursor.getInt(0);
//            StringBuilder sb = new StringBuilder("contactsId=");
//            sb.append(contactsId);
            uri = Uri.parse("content://com.android.contacts/contacts/" + contactsId + "/data"); //某个联系人下面的所有数据
            Cursor dataCursor = resolver.query(uri, new String[]{"mimetype", "data1", "data2"}, null, null, null);
            while (dataCursor.moveToNext()) {
                String data = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                String type = dataCursor.getString(dataCursor.getColumnIndex("mimetype"));
                if ("vnd.android.cursor.item/name".equals(type)) {    // 如果他的mimetype类型是name
//                    sb.append(", name=").append(data);
                } else if ("vnd.android.cursor.item/email_v2".equals(type)) { // 如果他的mimetype类型是email
//                    sb.append(", email=").append(data);
                } else if ("vnd.android.cursor.item/phone_v2".equals(type)) { // 如果他的mimetype类型是phone
//                    sb.append(", phone=").append(data);
                    if (data.equals(phone)) { //就是这个号码，返回
                        if (dataCursor != null && !dataCursor.isClosed()) {
                            dataCursor.close();
                        }
                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }
                        return contactsId;
                    }
                }
            }
            if (dataCursor != null && !dataCursor.isClosed()) {
                dataCursor.close();
            }
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return -1;
    }

    /**
     * @param contactId
     * @param name
     * @throws Exception
     */
    public synchronized void testUpdate(String contactId, String name) throws Exception {
        Log.d(Constants.TAG, "id:" + contactId + "  name:" + name);

//        ContentResolver resolver = context.getContentResolver();
//        Uri uri = ContactsContract.Contacts.CONTENT_URI;
//        ContentValues values=new ContentValues();
//        values.put(ContactsContract.Contacts.DISPLAY_NAME, name);
//        resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/name", contactId + ""});


        Uri uri = Uri.parse("content://com.android.contacts/data");//对data表的所有数据操作
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("data1", name);
        resolver.update(uri, values, "mimetype=? and raw_contact_id=?", new String[]{"vnd.android.cursor.item/name", contactId});


    }

    /**
     * 修改联系人
     *
     * @param name
     * @param number
     * @param ContactId
     * @param m_context
     */
    public synchronized void changeContact(String name, String number, String ContactId, Context m_context) {
        ContentValues values = new ContentValues();
        // 更新姓名
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, name);
        m_context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
                values,
                ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                new String[]{ContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE});
        //更新电话
        values.clear();
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, number);
        m_context.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
                values,
                ContactsContract.Data.RAW_CONTACT_ID + "=? and " + ContactsContract.Data.MIMETYPE + "=?",
                new String[]{ContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE});
    }

    /**
     * 新建/更新 监护人和监护对象
     */
    public synchronized void insertOrUpdataAllContacts() {
        if (null == executorService)
            executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new MyTask());
    }

    private class MyTask implements Runnable {

        @Override
        public void run() {
            List<Device> monitors = Session.getInstance().getMonitors();
            for (int i = 0; i < monitors.size(); i++) {
                Device device = monitors.get(i);
                //根据号码查询通讯录中的id
                int contacts = ContactsUtil.getInstance(context).getContacts(device.getPhone());
                if (-1 == contacts) { //为-1则没有些通讯录，要新建
                    try {
                        ContactsUtil.getInstance(context).addContact(device.getName(), device.getPhone());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //否则只是更新
                    try {
                        ContactsUtil.getInstance(context).testUpdate(contacts + "", device.getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            List<Keeper> userList = Session.getInstance().getUserList();
            for (int i = 0; i < userList.size(); i++) {
                Keeper keeper = userList.get(i);
                //根据号码查询通讯录中的id
                int contacts = ContactsUtil.getInstance(context).getContacts(keeper.getCellPhone());
                if (-1 == contacts) { //为-1则没有些通讯录，要新建
                    try {
                        ContactsUtil.getInstance(context).addContact(keeper.getNickName(), keeper.getCellPhone());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //否则只是更新
                    try {
                        ContactsUtil.getInstance(context).testUpdate(contacts + "", keeper.getNickName());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


}
