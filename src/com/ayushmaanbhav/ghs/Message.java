package com.ayushmaanbhav.ghs;

public class Message {
	public int senderID;
	public int receiverID;
	public int message;
	public double weight;

	public Message(int id1, int id2, int message) {
		this.senderID = id1;
		this.receiverID = id2;
		this.message = message;
	}

	public Message(int id1, int id2, int message, double weight) {
		this.senderID = id1;
		this.receiverID = id2;
		this.message = message;
		this.weight = weight;
	}
}
