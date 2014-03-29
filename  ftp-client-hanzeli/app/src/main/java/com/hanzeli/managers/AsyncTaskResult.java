package com.hanzeli.managers;

import java.util.ArrayList;

public class AsyncTaskResult {
	
	private boolean result;
	private ArrayList<ManagerEvent> events = new ArrayList<ManagerEvent>();

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
	public ArrayList<ManagerEvent> getEvents() {
		return events;
	}

	/**
	 * 
	 * @param event
	 */
	public void addEvent(ManagerEvent event) {
		events.add(event);
	}
}
