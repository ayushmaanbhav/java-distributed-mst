package com.ayushmaanbhav.ghs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.ayushmaanbhav.ghs.GraphPanel.Edge1;
import com.ayushmaanbhav.ghs.GraphPanel.Node1;

public class Events {
	public static GraphPanel gp;

	@SuppressWarnings("static-access")
	public static void startRecording() {
		JFrame f = new JFrame("GraphPanel");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gp = new GraphPanel();
		for (Node n : Network.getNodes()) {
			Point p = new Point(gp.rnd.nextInt(gp.WIDE),
					gp.rnd.nextInt(gp.HIGH));
			gp.nodes.add(new Node1(p, gp.radius, null, gp.kind, n));
		}
		f.add(gp.control, BorderLayout.NORTH);
		f.add(new JScrollPane(gp), BorderLayout.CENTER);
		f.getRootPane().setDefaultButton(gp.control.defaultButton);
		f.pack();
		f.setLocationByPlatform(true);
		f.setVisible(true);
		paint();
	}

	public static void paint() {
		SwingUtilities.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				// gp.nodes.clear();
				gp.edges.clear();
				for (Cluster c : Network.getClusters()) {
					if (c.color == null)
						c.color = new Color(gp.rnd.nextInt());
					for (Node n1 : c.getNodes()) {
						for (Node1 n2 : gp.nodes) {
							if (n2.n == n1) {
								n2.color = c.color;
							}
						}
					}
					for (Edge e : Network.getSpanningEdges()) {
						Node n1 = null, n2 = null;
						Node1 n3 = null, n4 = null;
						for (Node temp : c.getNodes()) {
							if (temp.getId() == e.id1)
								n1 = temp;
						}
						for (Node temp : c.getNodes()) {
							if (temp.getId() == e.id2)
								n2 = temp;
						}
						if (n1 == null || n2 == null)
							continue;
						for (Node1 temp : gp.nodes) {
							if (temp.n == n1)
								n3 = temp;
						}
						for (Node1 temp : gp.nodes) {
							if (temp.n == n2)
								n4 = temp;
						}
						if (n3 == null || n4 == null)
							continue;
						gp.edges.add(new Edge1(n3, n4));
					}
				}
				gp.repaint();
			}
		});
	}
}
