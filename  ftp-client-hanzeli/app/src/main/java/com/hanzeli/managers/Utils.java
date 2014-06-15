package com.hanzeli.managers;

import android.os.Bundle;
import android.util.Log;

import com.hanzeli.values.EventTypes;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;


public class Utils {

    private static final String TAG = "Utils";

    public static void connectClient (FTPClient client, Bundle bundle) throws ManagerException{
        String hostname = bundle.getString("server_host");
        int port= bundle.getInt("server_port");
        boolean anonymous = bundle.getBoolean("server_anonym");
        int timeout = bundle.getInt("server_timeout");
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

}
