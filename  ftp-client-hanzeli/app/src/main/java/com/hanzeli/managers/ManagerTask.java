package com.hanzeli.managers;

import java.util.ArrayList;

import com.hanzeli.values.EventTypes;
import com.hanzeli.values.Task;

public class ManagerTask {
	
	// preddefinovane operacie
	public static ManagerTask CONNECT_LR;
    public static ManagerTask CONNECT_T;
	public static ManagerTask DISCONNECT;
	public static ManagerTask DELETE;
	public static ManagerTask NEW_FOLDER;
	public static ManagerTask RENAME;
	public static ManagerTask CHNG_DIR;
	public static ManagerTask GO_PARENT;
	public static ManagerTask GO_ROOT;
	public static ManagerTask CHNG_ORDER;
	public static ManagerTask REFRESH;
	public static ManagerTask START_TRANSFER;
	public static ManagerTask STOP_TRANSFER;
    public static ManagerTask COUNT_FILES;
    public static ManagerTask COPY_FILES;

	// zoznam startovacich a ukoncovacich eventov
	private ArrayList<ManagerEvent> startEventsFragment = new ArrayList<ManagerEvent>();
	private ArrayList<ManagerEvent> endEventsFragment = new ArrayList<ManagerEvent>();
    private ArrayList<ManagerEvent> startEventsActivity = new ArrayList<ManagerEvent>();
    private ArrayList<ManagerEvent> endEventsActivity = new ArrayList<ManagerEvent>();
	
	private Task task;

	static {
		// pripojenie local alebo remote managera
		CONNECT_LR = new ManagerTask(Task.CONNECT_LR);

        // pripojenie transfer managera
		CONNECT_T = new ManagerTask(Task.CONNECT_T);
		
		// odpojenie klienta
		DISCONNECT = new ManagerTask(Task.DISCONNECT);
		
		// zmazanie suboru
		DELETE = new ManagerTask(Task.DELETE);
		
		// vytvorenie noveho priecinku
		NEW_FOLDER = new ManagerTask(Task.NEW_FOLDER);
		
		// premenovanie suboru
		RENAME = new ManagerTask(Task.RENAME);
		
		// zmena priecinku
		CHNG_DIR = new ManagerTask(Task.CHNG_DIR);
		
		// prechod do rodicovskeho priecinku
		GO_PARENT = new ManagerTask(Task.GO_PARENT);
		
		// prechod do root priecinku
		GO_ROOT = new ManagerTask(Task.GO_ROOT);
		
		// zmena usporiadania
		CHNG_ORDER = new ManagerTask(Task.CHNG_ORDER);
		
		// refresh managera
		REFRESH = new ManagerTask(Task.REFRESH);
		
		// spustenie prenosu
		START_TRANSFER = new ManagerTask(Task.START);
		
		// zastavenie prenosu
		STOP_TRANSFER = new ManagerTask(Task.STOP);

        // spocitanie velkosti suborov pre prenos
        COUNT_FILES = new ManagerTask(Task.COUNT);

        // KOPIROVANIE SUBOROV
        COPY_FILES = new ManagerTask(Task.COPY);
	}

	/**
	 * @param task hlavna operacia ktora sa ma vykonat
	 */
	private ManagerTask(Task task) {
		this.task=task;
        switch(task){
            case CONNECT_LR:
                addStartEventsFragment(EventTypes.FILES_LOAD);
                addEndEventsFragment(EventTypes.FILES_LOADED);
                addEndEventsActivity(EventTypes.CONNECTED);
                break;
            case CONNECT_T:
                addEndEventsActivity(EventTypes.CONNECTED);
                break;
            case COUNT:
                addEndEventsFragment(EventTypes.TRANSFER_LIST_CHANGE);
                addEndEventsFragment(EventTypes.START_TRANSFER);
                break;
            case DELETE:
            case NEW_FOLDER:
            case RENAME:
            case CHNG_DIR:
            case CHNG_ORDER:
            case GO_PARENT:
            case GO_ROOT:
            case REFRESH:
                addStartEventsFragment(EventTypes.FILES_LOAD);
                addEndEventsFragment(EventTypes.FILES_LOADED);
                break;
            default:
                break;
        }
	}
	
	
	/**
	 * @return ArrayList<ManagerEvent> zoznam startovacich eventov
	 */
	public ArrayList<ManagerEvent> getStartEventsFragment() {
		return startEventsFragment;
	}

    public ArrayList<ManagerEvent> getStartEventsActivity(){
        return startEventsActivity;
    }

	/**
	 * @return ArrayList<ManagerEvent> zoznam ukoncovacich eventov
	 */
	public ArrayList<ManagerEvent> getEndEventsFragment() {
		return endEventsFragment;
	}

    public ArrayList<ManagerEvent> getEndEventsActivity(){
        return endEventsActivity;
    }
	/**
	 * @return TASK hlavnu operaciu
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * eventy ktore sa maju vykonat pred vykonanim hlavnej operacie
	 * @param event typ eventu
	 */
	private void addStartEventsFragment(EventTypes event) {
		startEventsFragment.add(new ManagerEvent(event));
	}

    private void addStartEventsActivity(EventTypes event){
        startEventsActivity.add(new ManagerEvent(event));
    }

	/**
	 * eventy ktore sa maju vykonat po vykonani hlavnej operacie
	 * @param event typ eventu
	 */
	private void addEndEventsFragment(EventTypes event) {
		endEventsFragment.add(new ManagerEvent(event));
	}

    private void addEndEventsActivity(EventTypes event){
        endEventsActivity.add(new ManagerEvent(event));
    }
}
