package com.hanzeli.managers;

import com.hanzeli.resources.ManagerEvent;

/**
 * Interface for managing listeners of file managers
 * @author Michal
 *
 */
public interface EventListener {
	
	public void onEvent(ManagerEvent type);
}
