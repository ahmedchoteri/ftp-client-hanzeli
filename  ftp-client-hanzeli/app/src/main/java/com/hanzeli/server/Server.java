package com.hanzeli.server;

public class Server {
	private String name;
	private String host;
	private String username;
	private String password;
	private String localDir;
	private String remoteDir;
	private int port;
	private long _id;	//long because of return type of database after inserting new entry
	private boolean anonymous;	// if logged as anonymous then true
	
	
	/**
	 * Constructor when user will be logged with username and password
	 * @param name
	 * @param host
	 * @param username
	 * @param password
	 */
	public Server(String name, String host, String username, String password){
		this.name=name;
		this.host=host;
		this.username=username;
		this.password=password;
		port=21;	//default FTP port
		anonymous=false;
		_id=0;	//for test in database, that this instance is new
	}
	
	/**
	 * Constructor when user will be logged as anonymous
	 * @param name
	 * @param host
	 */
	public Server(String name, String host){
		this.name=name;
		this.host=host;
		port=21;	//default FTP port
		anonymous=true;
		_id=0; 	//for test in database, that this instance is new
	}
	
	/**
	 * Empty constructor for database fetching
	 */
	public Server(){
		
	}
	public void setName(String name){
		this.name=name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setHost(String host){
		this.host=host;
	}
	
	public String getHost(){
		return host;
	}
	
	public void setUsername(String username){
		this.username=username;
	}
	
	public String getUsername(){
		return username;
	}
	
	public void setPassword(String password){
		this.password=password;
	}
	
	public String getPassword(){
		return password;
	}
	
	public void setPort(int port){
		this.port=port;
	}
	
	public int getPort(){
		return port;
	}
	
	public void setId(long Id){
		this._id=Id;
	}
	
	public long getId(){
		return _id;
	}
	
	public void setAnonym(boolean bool){
		anonymous=bool;
	}
	
	public boolean getAnonym(){
		return anonymous;
	}
	
	public void setLocalDir(String dir){
		localDir=dir;
	}
	
	public String getLocalDir(){
		return localDir;
	}
	
	public void setRemoteDir(String dir){
		remoteDir=dir;
	}
	
	public String getRemoteDir(){
		return remoteDir;
	}
	/**
	 * Will be used by the ArrayAdapter in the Spinner
	 */
	@Override
	public String toString(){
		return name;
	}
}
