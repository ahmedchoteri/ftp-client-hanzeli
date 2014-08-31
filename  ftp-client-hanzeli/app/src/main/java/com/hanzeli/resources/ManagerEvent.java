package com.hanzeli.resources;


/**
 * class for constants describing file manager events
 * @author Michal
 *
 */
public class ManagerEvent {
	
	private EventTypes event;		//event type
	private String manager;	//manager which caused event

	public ManagerEvent(EventTypes event){
		this.event=event;
		this.manager=null;
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
