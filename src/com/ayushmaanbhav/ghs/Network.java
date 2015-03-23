package com.ayushmaanbhav.ghs;

import java.util.ArrayList;

public class Network {
	private static Node nodes[];
	private static ArrayList<Cluster> clusters;
	private static double weightMatrix[][];
	private static ArrayList<Edge> spanningEdges;
	private static ThreadGroup threadgroup;

	public static void initialise(double[][] adjacencyMatrix) {
		int N = adjacencyMatrix.length;
		nodes = new Node[N];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node(i, getNeighbours(i, adjacencyMatrix));
		}
		weightMatrix = adjacencyMatrix;
		clusters = new ArrayList<Cluster>(N);
		spanningEdges = new ArrayList<Edge>();
		for (Node node : nodes) {
			clusters.add(new Cluster(node));
		}
	}

	public static void startGHS() {
		threadgroup = new ThreadGroup("GHS Nodes");
		for (int i = 0; i < nodes.length; i++) {
			new Thread(threadgroup, nodes[i]).start();
		}
	}

	@SuppressWarnings("deprecation")
	public static void stopGHS() {
		threadgroup.stop();
	}

	private static int[] getNeighbours(int id, double[][] adjacencyMatrix) {
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < adjacencyMatrix[0].length; i++) {
			if (adjacencyMatrix[id][i] > 0) {
				arr.add(i);
			}
		}
		int arr2[] = new int[arr.size()];
		for (int i = 0; i < arr.size(); i++)
			arr2[i] = arr.get(i);
		return arr2;
	}

	public static double getWeight(int id1, int id2) {
		return weightMatrix[id1][id2];
	}

	public static void sendMessage(Message message) {
		nodes[message.receiverID].getMessageQueue().offer(message);
	}

	public static Message receiveMessage(int id) throws InterruptedException {
		return nodes[id].getMessageQueue().take();
	}

	synchronized public static Cluster mergeClusters(int a, int b) {
		Cluster clus, ca = getCluster(a), cb = getCluster(b);
		if (ca == null || cb == null)
			return null;
		if (ca == cb)
			return ca;
		clus = new Cluster();
		clus.getNodes().addAll(ca.getNodes());
		clus.getNodes().addAll(cb.getNodes());
		clusters.remove(ca);
		clusters.remove(cb);
		clusters.add(clus);
		spanningEdges.add(new Edge(a, b));
		Events.paint();
		return clus;
	}

	public static double[][] getWeightMatrix() {
		return weightMatrix;
	}

	public static Node[] getNodes() {
		return nodes;
	}

	public static boolean hasMessage(int id) {
		return nodes[id].getMessageQueue().size() > 0;
	}

	public static Cluster getCluster(int id) {
		for (Cluster c : clusters) {
			for (Node n : c.getNodes()) {
				if (n.getId() == id) {
					return c;
				}
			}
		}
		return null;
	}

	public static Cluster getCluster(Node n) {
		for (Cluster c : clusters) {
			if (c.getNodes().contains(n)) {
				return c;
			}
		}
		return null;
	}

	public static ArrayList<Edge> getSpanningEdges() {
		return spanningEdges;
	}

	public static ArrayList<Cluster> getClusters() {
		return clusters;
	}

	public static ThreadGroup getThreadgroup() {
		return threadgroup;
	}
}
