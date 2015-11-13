package com.bct.gpstracker.util;

/**
 * Created by HH
 * Date: 2015/7/21 0021
 * Time: 上午 11:56
 */
public class ByteUtil {
    /**
     * Short->Byte[]
     *
     * @param s
     * @return
     */
    public static byte[] shortToByteArray(short s) {
        byte[] shortBuf = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (shortBuf.length - 1 - i) * 8;
            shortBuf[i] = (byte) ((s >>> offset) & 0xff);
        }
        return shortBuf;
    }

    /**
     * Byte[]->Short
     *
     * @param b
     * @return
     */
    public static final int byteArrayToShort(byte[] b) {
        return (b[0] << 8) + (b[1] & 0xFF);
    }

    /**
     * Int->Byte[]
     *
     * @param value
     * @return
     */
    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }

    /**
     * 将byte[]转换为整数
     *
     * @param b
     * @return
     */
    public static final int byteArrayToInt(byte[] b) {
        switch (b.length) {
            case 1:
                return b[0] & 0xFF;
            case 2:
                return ((b[0] & 0xFF) << 8) + (b[1] & 0xFF);
            case 3:
                return ((b[0] & 0xFF) << 16) + ((b[1] & 0xFF) << 8) + (b[2] & 0xFF);
            default:
                return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
        }
    }

    /**
     * 取某byte[]中的一段转换成整数
     *
     * @param bytes
     * @param start
     * @param num
     * @return
     */
    public static int subBytesToInt(byte[] bytes, int start, int num) {
        byte[] bts = new byte[num];
        System.arraycopy(bytes, start, bts, 0, num);
        return byteArrayToInt(bts);
    }

    /**
     * 取十六进制的字面字符
     *
     * @param bytes
     * @param start
     * @param num
     * @return
     */
    public static String subBytesToHex(byte[] bytes, int start, int num) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            byte bt = bytes[start + i];
            sb.append(Integer.toHexString(bt >>> 4 & 0xF)).append(Integer.toHexString(bt & 0xF));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 取某byte[]中的一段转换成字符串
     *
     * @param bytes
     * @param start
     * @param num
     */
    public static String subBytesToString(byte[] bytes, int start, int num) {
        byte[] bts = new byte[num];
        System.arraycopy(bytes, start, bts, 0, num);
        return new String(bts);
    }

    /**
     *
     * @param bytes
     * @param start
     * @param num
     * @param obj
     */
    public static void setBytes(byte[] bytes, int start, int num, Object obj) {
        byte[] srcBytes = null;
        if (obj instanceof Byte) {
            srcBytes = new byte[]{(byte) obj};
        } else if (obj instanceof byte[]) {
            srcBytes = (byte[]) obj;
        } else if (obj instanceof Short) {
            short nb = (short) obj;
            srcBytes = shortToByteArray(nb);
        } else if (obj instanceof Integer) {
            int it = (int) obj;
            srcBytes = intToByteArray(it);
        } else if (obj instanceof String) {
            srcBytes = ((String) obj).getBytes();
        }

        if (srcBytes != null) {
            int min=CommUtil.min(num, srcBytes.length, bytes.length - start);
            System.arraycopy(srcBytes, srcBytes.length-min, bytes, start, min);
        }
    }

    public static void setBytesByHex(byte[] bytes, int start, int num, String str) {
        boolean dvd = str.length() % 2 == 0;
        int len = dvd ? str.length() / 2 : str.length() / 2 + 1;
        byte[] srcBytes = new byte[len];
        for (int i = len - 1; i >= 0; i--) {
            int idx = dvd ? i * 2 + 1 : i * 2;
            srcBytes[i] = (byte) ((idx - 1 < 0 ? 0 : Integer.parseInt(String.valueOf(str.charAt(idx - 1))) << 4) + Integer.parseInt(String.valueOf(str.charAt(idx))) & 0xFF);
        }
        setBytes(bytes, start, num, srcBytes);
    }

    public static String bytesToHexString(byte[] bts) {
        StringBuffer sb=new StringBuffer();
        for(byte bt:bts) {
            sb.append(Integer.toHexString((bt >>> 4) & 0xF)).append(Integer.toHexString(bt & 0xF)).append(' ');
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString().toUpperCase();
    }

//    public static void main(String[] args) {
//        byte[] bytes = new byte[8];
//        setBytesByHex(bytes, 0, 8, "123456789012345");
//        StringBuffer sb=new StringBuffer();
//        for(int i=0;i<bytes.length;i++){
//            if(i>0){
//                sb.append(',');
//            }
//            sb.append(Integer.toHexString(bytes[i] >>> 4 & 0xF)).append(Integer.toHexString(bytes[i] & 0xF));
//        }
//        System.out.println(sb.toString());
//        System.out.println(subBytesToHex(bytes, 0, 8));
//    }
}
