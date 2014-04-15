package com.hanzeli.managers;

import java.util.ArrayList;

public class AsyncTaskResult {
	
	private boolean result;
	private ArrayList<ManagerEvent> mEvents = new ArrayList<ManagerEvent>();    //events from Manager
    private ArrayList<String> sEvent = new ArrayList<String>(); //events from Service

	/**
	 * @return result
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * set result
	 * @param bool
	 */
	public void setResult(boolean bool) {
		result = bool;
	}

	/**
	 * @return the mEvents
	 */
	public ArrayList<ManagerEvent> getmEvents() {
		return mEvents;
	}

	/**
	 * 
	 * @param event
	 */
	public void addmEvent(ManagerEvent event) {
		mEvents.add(event);
	}

    public void addsEvent(String event){
        sEvent.add(event);
    }

    public ArrayList<String> getsEvents(){
        return sEvent;
    }
}
