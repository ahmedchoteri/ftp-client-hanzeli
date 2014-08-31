package com.hanzeli.resources;

import com.hanzeli.resources.EventTypes;

public class ManagerException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private EventTypes event;
    private String value;
	
	public ManagerException(EventTypes event){
		this.event=event;
	}

    public ManagerException(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
	
	public EventTypes getEvent(){
		return event;
	}
}
