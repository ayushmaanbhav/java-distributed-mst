package com.ayushmaanbhav.ghs;

public class Commands {
	public static final int MERGE = 0;
	public static final int ACCEPT = 1;
	public static final int REJECT = 2;
	public static final int LEADER_ELECTION = 3;
	public static final int LEADER_ELECTION_REPLY = 4;
	public static final int LEADER_ELECTION_REQUEST = 5;
	
	public static final String SEPERATOR = ":";

	public static String makeCommand(int senderId, String message) {
		return senderId + SEPERATOR + message;
	}

	public static String[] breakCommand(String command) {
		return command.split(SEPERATOR);
	}
}
