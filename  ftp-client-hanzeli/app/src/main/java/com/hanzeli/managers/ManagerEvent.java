package com.hanzeli.managers;

import com.hanzeli.values.EventTypes;


/**
 * class for constants describing file manager events
 * @author Michal
 *
 */
public class ManagerEvent {
	
	private EventTypes event;		//event type
	private String manager;	//manager which caused event
	//private String massage;

	public ManagerEvent(EventTypes event){
		this.event=event;
		this.manager=null;
	}
	
	
	public ManagerEvent(EventTypes event, String manager) {
		this.event=event;
		this.manager=manager;
	}

	/**
	 * @return manager
	 */
	public String getManager() {
		return manager;
	}

	/**
	 * @param manager manager ktory vykonal event
	 */
	public void setManager(String manager) {
		this.manager = manager;
	}

	/**
	 * @return event
	 */
	public EventTypes getEvent() {
		return event;
	}

}
