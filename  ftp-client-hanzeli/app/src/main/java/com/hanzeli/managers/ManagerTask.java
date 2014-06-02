package com.hanzeli.managers;

import java.util.ArrayList;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Task;

public class ManagerTask {
	
	//predefined manager tasks
	public static ManagerTask CONNECT;
	public static ManagerTask DISCONNECT;
	public static ManagerTask DELETE;
	public static ManagerTask NEW_FOLDER;
	public static ManagerTask RENAME;
	public static ManagerTask CHNG_DIR;
	public static ManagerTask GO_PARENT;
	public static ManagerTask GO_ROOT;
	public static ManagerTask CHNG_ORDER;
	public static ManagerTask REFRESH;
	public static ManagerTask START_TRANSFER;
	public static ManagerTask STOP_TRANSFER;
	public static ManagerTask TRANSF_LIST_CHANGE;

	//lists of starting and ending events
	private ArrayList<ManagerEvent> startEvents = new ArrayList<ManagerEvent>();
	private ArrayList<ManagerEvent> endEvents = new ArrayList<ManagerEvent>();
	
	private Task task;

	static {
		//connect operation
		CONNECT = new ManagerTask(Task.CONNECT, true);
		//CONNECT.addStartEvents(EventTypes.CONNECTION_START);
		//CONNECT.addEndEvents(EventTypes.FILES_LIST_CHANGE);
		//CONNECT.addEndEvents(EventTypes.CONNECTED);
		
		//disconnection operation
		DISCONNECT = new ManagerTask(Task.DISCONNECT, false);
		
		//delete files
		DELETE = new ManagerTask(Task.DELETE, true);
		
		//create new folder
		NEW_FOLDER = new ManagerTask(Task.NEW_FOLDER, true);
		
		//rename file
		RENAME = new ManagerTask(Task.RENAME, true);
		
		//change working directory
		CHNG_DIR = new ManagerTask(Task.CHNG_DIR, true);
		
		//go to parent directory
		GO_PARENT = new ManagerTask(Task.GO_PARENT, true);
		
		//go to root folder
		GO_ROOT = new ManagerTask(Task.GO_ROOT, true);
		
		//change ordering
		CHNG_ORDER = new ManagerTask(Task.CHNG_ORDER, true);
		
		//refresh manager
		REFRESH = new ManagerTask(Task.REFRESH, true);
		
		//start transfers
		START_TRANSFER = new ManagerTask(Task.START, false);
		
		//stop transfers
		STOP_TRANSFER = new ManagerTask(Task.STOP, false);

	}

	/**
	 * constructor for ManagerTask
	 * @param task 
	 * @param addLoadList
	 */
	private ManagerTask(Task task, boolean addLoadList) {
		this.task=task;
		if (addLoadList) {
			addStartEvents(EventTypes.FILES_LOAD);
			addEndEvents(EventTypes.FILES_LOADED);
            if (task == Task.CONNECT){
                addEndEvents(EventTypes.CONNECTED);
            }
		}
	}
	
	
	/**
	 * @return startEvents
	 */
	public ArrayList<ManagerEvent> getStartEvents() {
		return startEvents;
	}

	/**
	 * @return endEvents
	 */
	public ArrayList<ManagerEvent> getEndEvents() {
		return endEvents;
	}
	
	/**
	 * @return task
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * events before given manager operation
	 * @param 
	 */
	private void addStartEvents(EventTypes event) {
		startEvents.add(new ManagerEvent(event));
	}

	/**
	 * events that should be done after given manager operation
	 * @param 
	 */
	private void addEndEvents(EventTypes event) {
		endEvents.add(new ManagerEvent(event));
	}

}
