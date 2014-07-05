package com.hanzeli.managers;

/**
 * Interface for managing listeners of file managers
 * @author Michal
 *
 */
public interface EventListener {
	
	public void onEvent(ManagerEvent type);
}
