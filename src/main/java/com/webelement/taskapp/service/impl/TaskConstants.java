package com.webelement.taskapp.service.impl;

import java.util.Map;

public interface TaskConstants {
	Map<Short, Short> STATUS_FLOW = Map.of((short) 1, (short) 2, (short) 2, (short) 3, (short) 3, (short) 4, (short) 4,
			(short) 5);
	Map<Short, Short> REOPEN_FLOW = Map.of((short) 3, (short) 3, (short) 5, (short) 5);

	Map<Short, String> STATUS_LABELS = Map.of((short) 1, "Assigned", (short) 2, "Assignee Closure", (short) 3,
			"Re-Open", (short) 4, "Assignee Re-Closure", (short) 5, "Assignor Closure");

	long MAX_PDF_SIZE = 10 * 1024 * 1024; // 10 MB
	long MAX_ZIP_SIZE = 50 * 1024 * 1024; // 50 MB
}
