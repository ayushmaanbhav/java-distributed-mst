package com.ayushmaanbhav.ghs;

import java.awt.Color;
import java.util.ArrayList;

public class Cluster {
	private static int ids = 0;
	private int id;
	private ArrayList<Node> nodes;
	private Node leader;
	public Color color;

	public Cluster() {
		id = ids++;
		nodes = new ArrayList<Node>();
	}

	public Cluster(Node n) {
		id = ids++;
		nodes = new ArrayList<Node>();
		nodes.add(n);
		setLeader(n);
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void addNode(Node n) {
		nodes.add(n);
	}

	public int getId() {
		return id;
	}

	public Node getLeader() {
		return leader;
	}

	public void setLeader(Node leader) {
		this.leader = leader;
	}

	public boolean isInCluster(int id) {
		for (Node n : nodes) {
			if (n.getId() == id)
				return true;
		}
		return false;
	}
}
