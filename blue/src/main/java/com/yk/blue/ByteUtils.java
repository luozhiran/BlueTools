package com.yk.blue;

public class ByteUtils {

    // 转化十六进制编码为字符串
    public static String toStringHex2(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                        i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }


    public static String restVin( String data){
        data=data.replace(" ","").replace(">","").replace("\r","");
        if(data.startsWith("0902014")){
            data=data.replace("0902014","");
        }
        if(data.startsWith("0902")){
            data=data.replace("0902","");
        }
        return data.replace("0:","").replace("1:","").replace("2:","").replace("490201","").replace("490202","").replace("490203","").replace("490204","").replace("490205","");

    }
}
