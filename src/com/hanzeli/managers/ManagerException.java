package com.hanzeli.managers;

import com.hanzeli.values.EventTypes;

public class ManagerException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EventTypes event;
	
	public ManagerException(EventTypes event){
		this.event=event;
	}
	
	public EventTypes getEvent(){
		return event;
	}
}
