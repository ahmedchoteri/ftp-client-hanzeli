package com.hanzeli.ftpdroid;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.hanzeli.server.*;

public class ServerScreenActivity extends Activity implements OnItemSelectedListener, OnClickListener{
	public static final String STATE = "state";
	private Spinner spinner;
	private Button btnConnect;
	private Button btnNew;
	private Button btnEdit;
	private Button btnDelete;
	
	private ServerDatabase database;
	private List<Server> serverList;
	ArrayAdapter<Server> adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_screen);
		loadUI();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		loadUI();
	}
	
	
	public void onPause(){
		database.close();
		super.onPause();
	}
	private void loadUI(){
		//Initialization of spinner
		database = new ServerDatabase(this);
		database.open();
		serverList = database.allServers();
		database.close();
		adapter = new ArrayAdapter<Server>(this,R.layout.spinner_bkg,serverList);		
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner = (Spinner) findViewById(R.id.spinner1);
		spinner.setOnItemSelectedListener(this);
		spinner.setAdapter(adapter);
		
		btnConnect = (Button) findViewById(R.id.btnConnect);
		btnNew = (Button) findViewById(R.id.btnNew);
		btnEdit = (Button) findViewById(R.id.btnEdit);
		btnDelete = (Button) findViewById(R.id.btnDelete);
		
		btnConnect.setOnClickListener(this);
		btnNew.setOnClickListener(this);
		btnEdit.setOnClickListener(this);
		btnDelete.setOnClickListener(this);
		
		btnConnect.setEnabled(false);
		btnEdit.setEnabled(false);
		btnDelete.setEnabled(false);
		btnNew.setEnabled(true);
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id){
		btnConnect.setEnabled(true);
		btnEdit.setEnabled(true);
		btnDelete.setEnabled(true);
	}
	
	public void onNothingSelected(AdapterView<?> parent){
		//Nothing
	}
	
	public void onClick(View view){
		switch (view.getId()){
			case R.id.btnConnect: connect(); break;
			case R.id.btnNew: newServer(); break;
			case R.id.btnEdit: edit(); break;
			case R.id.btnDelete: delete(); break;
		}
		
	}
	
	private void connect(){
		Intent intent = new Intent(this, MainActivity.class);
		Server server = serverList.get(spinner.getSelectedItemPosition());
		MainApplication.getInstance().saveLastSelectedServer(server.getId());
		intent.putExtra("name", server.getName());
		intent.putExtra("host", server.getHost());
		intent.putExtra("uname", server.getUsername());
		intent.putExtra("pass", server.getPassword());
		intent.putExtra("port", server.getPort());
		intent.putExtra("anonym", server.getAnonym());
		intent.putExtra("local", server.getLocalDir());
		intent.putExtra("remote", server.getRemoteDir());
		startActivity(intent);
	}
	
	private void newServer(){
		Intent intent = new Intent(this, EditServerActivity.class);
		intent.putExtra(STATE, "0");	//sign for creating a new server in edit server activity
		startActivity(intent);
		
	}
	
	private void edit(){
		Intent intent = new Intent(this, EditServerActivity.class);
		intent.putExtra(STATE, "1");	//sign for editing server in edit server activity
		
		Server server = serverList.get(spinner.getSelectedItemPosition());
		intent.putExtra("id", server.getId());
		intent.putExtra("name", server.getName());
		intent.putExtra("host", server.getHost());
		intent.putExtra("uname", server.getUsername());
		intent.putExtra("pass", server.getPassword());
		intent.putExtra("port", server.getPort());
		intent.putExtra("anonym", server.getAnonym());
		intent.putExtra("local", server.getLocalDir());
		intent.putExtra("remote", server.getRemoteDir());
		startActivity(intent);
		
	}
	
	private void delete(){
		Server server = serverList.get(spinner.getSelectedItemPosition());
		adapter.remove(server);
		spinner.setAdapter(adapter);
		long id = server.getId();
		database.open();
		boolean succ = database.remove(id);
		database.close();
		CharSequence text;
		int duration = Toast.LENGTH_SHORT;
		if (succ) { text="Server " + server.getName() + " deleted"; }
		else text="Server "  + server.getName() + " not deleted";
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

}
