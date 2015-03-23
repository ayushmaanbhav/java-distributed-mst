package com.ayushmaanbhav.ghs;

public class PollingService extends Thread {
	public static boolean print = true;

	public void print() {
		if (print) {
			System.out.println();
			System.out.println("Network State:");
			System.out.println("No. of Nodes: " + Network.getNodes().length);
			System.out.println("No. of Clusters: "
					+ Network.getClusters().size());
			System.out.println("No. of Spanning Edges: "
					+ Network.getSpanningEdges().size());
			System.out.println("Spanning Edges:");
			for (Edge e : Network.getSpanningEdges()) {
				System.out.println(e.id1 + " - " + e.id2);
			}
			System.out.println("Clusters' State:");
			for (Cluster c : Network.getClusters()) {
				System.out.println("Cluster " + c.getId() + ":");
				System.out.println("No. of Nodes: " + c.getNodes().size());
				System.out.print("Nodes: ");
				for (Node n : c.getNodes()) {
					System.out.print(n.getId() + ", ");
				}
				System.out.println();
				if (c.getLeader() != null)
					System.out.println("Leader: " + c.getLeader().getId());
				else
					System.out.println("In leader election phase");
			}
			System.out.println();
			System.out.println();
		}
	}

	public void run() {
		while (Network.getClusters().size() > 1) {
			print();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		print();
	}
}
