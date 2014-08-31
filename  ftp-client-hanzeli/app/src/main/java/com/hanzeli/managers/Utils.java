package com.hanzeli.managers;

import android.os.Bundle;
import android.util.Log;

import com.hanzeli.resources.EventTypes;
import com.hanzeli.resources.ManagerException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.util.Arrays;


public class Utils {

    private static final String TAG = "Utils";

    public static void connectClient (FTPClient client, Bundle bundle) throws ManagerException {
        String hostname = bundle.getString("server_host");
        int port= bundle.getInt("server_port");
        boolean anonymous = bundle.getBoolean("server_anonym");
        String user = "";
        String password = "";
        if (!anonymous){
            user = bundle.getString("username");
            password = bundle.getString("password");
        }
        try{
            int reply;
            client.setConnectTimeout(200000);
            client.setDataTimeout(12000);    //nastavenie data connection time aby sa dalo posielat/stahovat
            client.connect(hostname, port);
            reply = client.getReplyCode();
            if(!FTPReply.isPositiveCompletion(reply)) {
                client.disconnect();
            }
            if (!anonymous){
                if(!client.login(user, password)){
                    client.disconnect();
                    Log.d(TAG,"Login error");
                    throw new ManagerException(EventTypes.CONNECTION_LOGIN_ERR);
                }
            }
            client.enterLocalPassiveMode();
        } catch(IOException e){
            Log.d(TAG,"Connecting client error");
            throw new ManagerException(EventTypes.CONNECTION_ERROR);
        }
    }

    public static void disconnectClient(FTPClient client) throws ManagerException{
        try {
            client.disconnect();
        }catch (IOException e){
            Log.d(TAG,"Disconnecting error");
            throw new ManagerException(EventTypes.DISCONNECTION_ERROR);
        }
    }

    public static int countOccurrences(String haystack, char needle)
    {
        int count = 0;
        for (int i=0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                count++;
            }
        }
        return count;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

}
