package com.ayushmaanbhav.ghs;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node implements Runnable {
	private static int ids = 0;
	private int id, neighbours[];
	private BlockingQueue<Message> messageQueue;
	private Cluster cluster;

	public Node(int[] neighbours) {
		this.id = ids++;
		this.neighbours = neighbours;
		this.messageQueue = new LinkedBlockingQueue<Message>();
	}

	public Node(int id, int[] neighbours) {
		this.id = id;
		this.neighbours = neighbours;
		this.messageQueue = new LinkedBlockingQueue<Message>();
	}

	public void run() {
		cluster = Network.getCluster(this);
		cluster.setLeader(this);
		while (Network.getClusters().size() > 1) {
			try {
				randomSleep();
				if (cluster.getLeader() == null
						&& this == cluster.getNodes().get(0)) {
					electLeader(getLeastWeightedEdgeOutOfCluster());
				}
				if (cluster.getLeader() == this) {
					System.out.println(getId() + " leader, receiving");
					if (receiveAndMerge())
						continue;
					System.out.println(getId() + " leader, sending");
					if (sendAndMerge(getNodeWithLeastWeightedEdgeOutOfCluster()))
						continue;
					System.out.println(getId() + " leader going for election");
					electLeader(getLeastWeightedEdgeOutOfCluster());
				} else {
					System.out.println(getId() + " receiving");
					receiveAndMerge();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void electLeader(double weight) throws InterruptedException {
		// System.out.println("Electing Leader for cluster: " +
		// cluster.getId());
		if (cluster.getNodes().size() == 1) {
			System.out.println("Elected leader: " + getId() + " for cluster: "
					+ cluster.getId());
			cluster.setLeader(this);
			return;
		}

		for (Node n : cluster.getNodes()) {
			if (n.getId() != id)
				Network.sendMessage(new Message(id, n.getId(),
						Commands.LEADER_ELECTION));
		}
		// System.out.println(getId() + ": sent messages");

		ArrayList<Message> arrm = new ArrayList<Message>();
		while (arrm.size() < cluster.getNodes().size() - 1) {
			// System.out.println(getId() + ": messages size" + arrm.size());
			Message recMessage = Network.receiveMessage(id);
			if (recMessage.message == Commands.LEADER_ELECTION_REPLY) {
				arrm.add(recMessage);
			} else if (recMessage.message == Commands.MERGE) {
				Message toSend = new Message(id, recMessage.senderID,
						Commands.REJECT);
				Network.sendMessage(toSend);
			}
		}
		double min = weight;
		for (Message m : arrm) {
			if (min > m.weight) {
				min = m.weight;
			}
		}
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (Message m : arrm) {
			if (min == m.weight) {
				arr.add(m.senderID);
			}
		}
		// System.out.println(getId() + ": Arr size: " + arr.size());
		if (arr.size() == 0) {
			System.out.println("Elected leader: " + getId() + " for cluster: "
					+ getId());
			cluster.setLeader(this);
			return;
		}

		int node_id = arr.get((int) (Math.random() * arr.size()));
		for (Node n : cluster.getNodes()) {
			if (n.getId() == node_id) {
				System.out.println("Elected leader: " + n.getId()
						+ " for cluster: " + getId());
				cluster.setLeader(n);
				break;
			}
		}
	}

	private boolean sendAndMerge(int least_id) throws InterruptedException {
		Message recMessage;
		Network.sendMessage(new Message(getId(), least_id, Commands.MERGE));
		recMessage = receiveMessageFrom(cluster, least_id);
		if (recMessage.message == Commands.MERGE
				|| recMessage.message == Commands.ACCEPT) {
			Network.sendMessage(new Message(getId(), least_id, Commands.ACCEPT));
			cluster = Network.mergeClusters(getId(), least_id);
			submitLeaderElectionRequest();
			return true;
		}
		return false;
	}

	private boolean receiveAndMerge() throws InterruptedException {
		Message recMessage;
		while (Network.hasMessage(getId())) {
			System.out.println(getId() + ": got message");
			recMessage = checkForCLusterCommands(Network
					.receiveMessage(getId()));
			if (recMessage != null && recMessage.message == Commands.MERGE) {
				Network.sendMessage(new Message(getId(), recMessage.senderID,
						Commands.ACCEPT));
				cluster = Network.mergeClusters(getId(), recMessage.senderID);
				submitLeaderElectionRequest();
				return true;
			}
		}
		return false;
	}

	private void submitLeaderElectionRequest() {
		if (this != cluster.getLeader())
			Network.sendMessage(new Message(getId(), cluster.getNodes().get(0)
					.getId(), Commands.LEADER_ELECTION_REQUEST));
	}

	private Message checkForCLusterCommands(Message receivedMessage)
			throws InterruptedException {
		if (receivedMessage.message == Commands.LEADER_ELECTION) {
			System.out.println(getId() + ": got leader election message");
			Message m = new Message(getId(), receivedMessage.senderID,
					Commands.LEADER_ELECTION_REPLY,
					getLeastWeightedEdgeOutOfCluster());
			Network.sendMessage(m);
			return null;
		} else if (receivedMessage.message == Commands.LEADER_ELECTION_REQUEST) {
			System.out.println(getId() + ": got leader election request");
			return null;
		}
		return receivedMessage;
	}

	private void randomSleep() throws InterruptedException {
		Thread.sleep((long) (4000 * Math.random() + 1000));
	}

	private Message receiveMessageFrom(Cluster cluster, int fromID)
			throws InterruptedException {
		Message recMessage = null;
		while (true) {
			recMessage = checkForCLusterCommands(Network
					.receiveMessage(getId()));
			if (recMessage != null) {
				if (recMessage.senderID == fromID) {
					break;
				} else if (recMessage.message == Commands.MERGE) {
					Message toSend = new Message(getId(), recMessage.senderID,
							Commands.REJECT);
					Network.sendMessage(toSend);
				}
			}
		}
		return recMessage;
	}

	private int getNodeWithLeastWeightedEdgeOutOfCluster() {
		double min = getLeastWeightedEdgeOutOfCluster();
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < neighbours.length; i++) {
			if (!cluster.isInCluster(neighbours[i])) {
				double d = Network.getWeight(id, neighbours[i]);
				if (min == d) {
					arr.add(neighbours[i]);
				}
			}
		}
		int id = (int) (Math.random() * arr.size());
		return arr.get(id);
	}

	private double getLeastWeightedEdgeOutOfCluster() {
		double min = Double.MAX_VALUE;
		for (int i = 0; i < neighbours.length; i++) {
			if (!cluster.isInCluster(neighbours[i])) {
				double d = Network.getWeight(id, neighbours[i]);
				if (min > d) {
					min = d;
				}
			}
		}
		return min;
	}

	public BlockingQueue<Message> getMessageQueue() {
		return messageQueue;
	}

	public int getId() {
		return id;
	}

	public int[] getNeighbours() {
		return neighbours;
	}
}
