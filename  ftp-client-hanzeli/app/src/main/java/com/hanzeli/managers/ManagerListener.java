package com.hanzeli.managers;


/**
 * Interface for managing listeners of file managers
 * @author Michal
 *
 */
public interface ManagerListener {
	
	public void managerEvent(ManagerEvent type);
}
