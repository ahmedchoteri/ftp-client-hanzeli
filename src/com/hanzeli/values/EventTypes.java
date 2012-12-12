package com.hanzeli.values;

public enum EventTypes {
	// messages for connecting manager
	CONNECTION_START,
	CONNECTED,
	CONNECTION_END,
	CONNECTION_ERROR,
	CONNECTION_LOGIN_ERR,
	CONNECTION_LOST,
		
	// changes in list of files
	FILES_LIST_CHANGE,
	FILES_LOAD,
	FILES_LOADED,
		
	//manager status massages
	NEW_FOLDER_OK, 
	DEL_FILE_OK, 
	DEL_FOLERD_OK,
	RENAME_FILE_OK,
	RENAME_FOLDER_OK,

	//manager status errors
	ERROR,
	NEW_FOLDER_ERR, 
	DEL_FOLDER_ERR,
	DEL_FILE_ERR,
	RENAME_ERR,
	FILE_EXISTS_ERR,
	FOLDER_EXISTS_ERR;
	
}
