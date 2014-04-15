package com.hanzeli.managers;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Values;

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
