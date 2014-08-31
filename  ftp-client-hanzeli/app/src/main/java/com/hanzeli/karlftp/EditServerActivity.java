package com.hanzeli.karlftp;

import com.hanzeli.server.Server;
import com.hanzeli.server.ServerDatabase;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParsePosition;


public class EditServerActivity extends Activity implements OnClickListener{
	
	private ServerDatabase database;
	private EditText name;
	private EditText host;
	private EditText user;
	private EditText pass;
	private EditText port;
	private EditText local;
	private EditText remote;
	private CheckBox anonym;
	private Button save;
	private String stat;
	private Intent intent;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server);
        database = new ServerDatabase(this);
        intent = getIntent();
        stat = intent.getStringExtra(ServerScreenActivity.STATE);
        name = (EditText) findViewById(R.id.editTextName);
    	host = (EditText) findViewById(R.id.editTextHost);
    	user = (EditText) findViewById(R.id.editTextUsername);
    	pass = (EditText) findViewById(R.id.editTextPassword);
    	port = (EditText) findViewById(R.id.editTextPort);
    	anonym = (CheckBox) findViewById(R.id.checkBoxAnonym);
    	local = (EditText) findViewById(R.id.editTextLocal);
    	remote = (EditText) findViewById(R.id.editTextRemote);
    	save = (Button) findViewById(R.id.editButtonSave);
    	save.setOnClickListener(this);
        if (stat.equals("1")){	//updating server
        	name.setText(intent.getStringExtra("name"));
        	host.setText(intent.getStringExtra("host"));
        	user.setText(intent.getStringExtra("uname"));
        	pass.setText(intent.getStringExtra("host"));
        	port.setText(Integer.toString(intent.getIntExtra("port",0)));
        	local.setText(intent.getStringExtra("local"));
        	remote.setText(intent.getStringExtra("remote"));
        	anonym.setChecked(intent.getBooleanExtra("anonym", false));
        }
    }

	public void onClick(View v) {
		database.open();
		Server server = new Server();
		if (stat.equals("0")) server.setId(0);	//new server
		else server.setId(intent.getLongExtra("id", 0));
        String nameS = name.getText().toString();
        String hostS = host.getText().toString();
        String userS = user.getText().toString();
        String passS = pass.getText().toString();
        String portS = port.getText().toString();
        boolean anon = anonym.isChecked();
        if(nameS.equals("")){
            Toast.makeText(getApplicationContext(), "Name is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (hostS.equals("")){
            Toast.makeText(getApplicationContext(), "Host is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (portS.equals("")){
            Toast.makeText(getApplicationContext(), "Port is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!anon && userS.equals("")){
            Toast.makeText(getApplicationContext(), "Username is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!anon && passS.equals("")){
            Toast.makeText(getApplicationContext(), "Password is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(!isNumeric(portS)){
            Toast.makeText(getApplicationContext(), "Port is not number", Toast.LENGTH_SHORT).show();
            return;
        }
		server.setName(nameS);
		server.setHost(hostS);
		server.setUsername(userS);
		server.setPassword(passS);
		server.setPort(Integer.parseInt(portS));
		server.setAnonym(anon);
		server.setLocalDir(local.getText().toString());
		server.setRemoteDir(remote.getText().toString());
		database.update(server);
		database.close();
		finish();
	}

    public static boolean isNumeric(String str)
    {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }
}
