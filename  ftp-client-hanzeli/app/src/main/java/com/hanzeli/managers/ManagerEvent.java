package com.hanzeli.managers;

import com.hanzeli.values.EventTypes;


/**
 * class for constants describing file manager events
 * @author Michal
 *
 */
public class ManagerEvent {
	
	private EventTypes event;		//event type
	private Manager manager;	//manager which caused event
	//private String massage;

	public ManagerEvent(EventTypes event){
		this.event=event;
		this.manager=null;
	}
	
	
	public ManagerEvent(EventTypes event, Manager manager) {
		this.event=event;
		this.manager=manager;
	}

	/**
	 * @return manager
	 */
	public Manager getManager() {
		return manager;
	}

	/**
	 * @param manager manager ktory vykonal event
	 */
	public void setManager(Manager manager) {
		this.manager = manager;
	}

	/**
	 * @return event
	 */
	public EventTypes getEvent() {
		return event;
	}

}
