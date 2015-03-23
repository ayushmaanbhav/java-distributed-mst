package com.ayushmaanbhav.ghs;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.*;

/**
 * @author John B. Matthews; distribution per GPL.
 */
public class GraphPanel extends JComponent {

	public static final int WIDE = 640;
	public static final int HIGH = 480;
	public static final int RADIUS = 35;
	public static final Random rnd = new Random();
	public ControlPanel control = new ControlPanel();
	public int radius = RADIUS;
	public Kind kind = Kind.Circular;
	public List<Node1> nodes = new ArrayList<Node1>();
	public List<Node1> selected = new ArrayList<Node1>();
	public List<Edge1> edges = new ArrayList<Edge1>();
	public Point mousePt = new Point(WIDE / 2, HIGH / 2);
	public Rectangle mouseRect = new Rectangle();
	public boolean selecting = false;

	/*public static void main(String[] args) throws Exception {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				JFrame f = new JFrame("GraphPanel");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				GraphPanel gp = new GraphPanel();
				f.add(gp.control, BorderLayout.NORTH);
				f.add(new JScrollPane(gp), BorderLayout.CENTER);
				f.getRootPane().setDefaultButton(gp.control.defaultButton);
				f.pack();
				f.setLocationByPlatform(true);
				f.setVisible(true);
			}
		});
	}*/

	public GraphPanel() {
		this.setOpaque(true);
		this.addMouseListener(new MouseHandler());
		this.addMouseMotionListener(new MouseMotionHandler());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDE, HIGH);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(new Color(0x00f0f0f0));
		g.fillRect(0, 0, getWidth(), getHeight());
		for (Edge1 e : edges) {
			e.draw(g);
		}
		for (Node1 n : nodes) {
			n.draw(g);
		}
		if (selecting) {
			g.setColor(Color.darkGray);
			g.drawRect(mouseRect.x, mouseRect.y, mouseRect.width,
					mouseRect.height);
		}
	}

	public class MouseHandler extends MouseAdapter {

		@Override
		public void mouseReleased(MouseEvent e) {
			selecting = false;
			mouseRect.setBounds(0, 0, 0, 0);
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
			e.getComponent().repaint();
		}

		@Override
		public void mousePressed(MouseEvent e) {
			mousePt = e.getPoint();
			if (e.isShiftDown()) {
				Node1.selectToggle(nodes, mousePt);
			} else if (e.isPopupTrigger()) {
				Node1.selectOne(nodes, mousePt);
				showPopup(e);
			} else if (Node1.selectOne(nodes, mousePt)) {
				selecting = false;
			} else {
				Node1.selectNone(nodes);
				selecting = true;
			}
			e.getComponent().repaint();
		}

		public void showPopup(MouseEvent e) {
			control.popup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public class MouseMotionHandler extends MouseMotionAdapter {

		Point delta = new Point();

		@Override
		public void mouseDragged(MouseEvent e) {
			if (selecting) {
				mouseRect.setBounds(Math.min(mousePt.x, e.getX()),
						Math.min(mousePt.y, e.getY()),
						Math.abs(mousePt.x - e.getX()),
						Math.abs(mousePt.y - e.getY()));
				Node1.selectRect(nodes, mouseRect);
			} else {
				delta.setLocation(e.getX() - mousePt.x, e.getY() - mousePt.y);
				Node1.updatePosition(nodes, delta);
				mousePt = e.getPoint();
			}
			e.getComponent().repaint();
		}
	}

	public JToolBar getControlPanel() {
		return control;
	}

	public class ControlPanel extends JToolBar {

		public Action newNode = new NewNodeAction("New");
		public Action clearAll = new ClearAction("Clear");
		public Action kind = new KindComboAction("Kind");
		public Action color = new ColorAction("Color");
		public Action connect = new ConnectAction("Connect");
		public Action delete = new DeleteAction("Delete");
		public Action random = new RandomAction("Random");
		public JButton defaultButton = new JButton(newNode);
		public JComboBox kindCombo = new JComboBox();
		public ColorIcon hueIcon = new ColorIcon(Color.blue);
		public JPopupMenu popup = new JPopupMenu();

		ControlPanel() {
			this.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.setBackground(Color.lightGray);

			this.add(defaultButton);
			this.add(new JButton(clearAll));
			this.add(kindCombo);
			this.add(new JButton(color));
			this.add(new JLabel(hueIcon));
			JSpinner js = new JSpinner();
			js.setModel(new SpinnerNumberModel(RADIUS, 5, 100, 5));
			js.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					JSpinner s = (JSpinner) e.getSource();
					radius = (Integer) s.getValue();
					Node1.updateRadius(nodes, radius);
					GraphPanel.this.repaint();
				}
			});
			this.add(new JLabel("Size:"));
			this.add(js);
			this.add(new JButton(random));

			popup.add(new JMenuItem(newNode));
			popup.add(new JMenuItem(color));
			popup.add(new JMenuItem(connect));
			popup.add(new JMenuItem(delete));
			JMenu subMenu = new JMenu("Kind");
			for (Kind k : Kind.values()) {
				kindCombo.addItem(k);
				subMenu.add(new JMenuItem(new KindItemAction(k)));
			}
			popup.add(subMenu);
			kindCombo.addActionListener(kind);
		}

		class KindItemAction extends AbstractAction {

			public Kind k;

			public KindItemAction(Kind k) {
				super(k.toString());
				this.k = k;
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				kindCombo.setSelectedItem(k);
			}
		}
	}

	public class ClearAction extends AbstractAction {

		public ClearAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			nodes.clear();
			edges.clear();
			repaint();
		}
	}

	public class ColorAction extends AbstractAction {

		public ColorAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			Color color = control.hueIcon.getColor();
			color = JColorChooser.showDialog(GraphPanel.this, "Choose a color",
					color);
			if (color != null) {
				Node1.updateColor(nodes, color);
				control.hueIcon.setColor(color);
				control.repaint();
				repaint();
			}
		}
	}

	public class ConnectAction extends AbstractAction {

		public ConnectAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			Node1.getSelected(nodes, selected);
			if (selected.size() > 1) {
				for (int i = 0; i < selected.size() - 1; ++i) {
					Node1 n1 = selected.get(i);
					Node1 n2 = selected.get(i + 1);
					edges.add(new Edge1(n1, n2));
				}
			}
			repaint();
		}
	}

	public class DeleteAction extends AbstractAction {

		public DeleteAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			ListIterator<Node1> iter = nodes.listIterator();
			while (iter.hasNext()) {
				Node1 n = iter.next();
				if (n.isSelected()) {
					deleteEdges(n);
					iter.remove();
				}
			}
			repaint();
		}

		public void deleteEdges(Node1 n) {
			ListIterator<Edge1> iter = edges.listIterator();
			while (iter.hasNext()) {
				Edge1 e = iter.next();
				if (e.n1 == n || e.n2 == n) {
					iter.remove();
				}
			}
		}
	}

	public class KindComboAction extends AbstractAction {

		public KindComboAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			JComboBox combo = (JComboBox) e.getSource();
			kind = (Kind) combo.getSelectedItem();
			Node1.updateKind(nodes, kind);
			repaint();
		}
	}

	public class NewNodeAction extends AbstractAction {

		public NewNodeAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			Node1.selectNone(nodes);
			Point p = mousePt.getLocation();
			Color color = control.hueIcon.getColor();
			Node1 n = new Node1(p, radius, color, kind, null);
			n.setSelected(true);
			nodes.add(n);
			repaint();
		}
	}

	public class RandomAction extends AbstractAction {

		public RandomAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < 16; i++) {
				Point p = new Point(rnd.nextInt(getWidth()),
						rnd.nextInt(getHeight()));
				nodes.add(new Node1(p, radius, new Color(rnd.nextInt()), kind, null));
			}
			repaint();
		}
	}

	/**
	 * The kinds of node in a graph.
	 */
	public enum Kind {

		Circular, Rounded, Square;
	}

	/**
	 * An Edge is a pair of Nodes.
	 */
	public static class Edge1 {

		public Node1 n1;
		public Node1 n2;
		public Color c;

		public Edge1(Node1 n1, Node1 n2) {
			this.n1 = n1;
			this.n2 = n2;
			c = Color.darkGray;
		}

		public void draw(Graphics g) {
			Point p1 = n1.getLocation();
			Point p2 = n2.getLocation();
			Graphics2D g2 = (Graphics2D) g;
			Stroke s = g2.getStroke();
			g2.setStroke(new BasicStroke(5));
			g.setColor(c);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			g2.setStroke(s);
		}
	}

	/**
	 * A Node represents a node in a graph.
	 */
	public static class Node1 {

		public Point p;
		public int r;
		public Color color;
		public Kind kind;
		public boolean selected = false;
		public Rectangle b = new Rectangle();
		public Node n;

		/**
		 * Construct a new node.
		 */
		public Node1(Point p, int r, Color color, Kind kind, Node n) {
			this.p = p;
			this.r = r;
			this.color = color;
			this.kind = kind;
			this.n = n;
			setBoundary(b);
		}

		/**
		 * Calculate this node's rectangular boundary.
		 */
		public void setBoundary(Rectangle b) {
			b.setBounds(p.x - r, p.y - r, 2 * r, 2 * r);
		}

		/**
		 * Draw this node.
		 */
		public void draw(Graphics g) {
			g.setColor(this.color);
			if (this.kind == Kind.Circular) {
				g.fillOval(b.x, b.y, b.width, b.height);
			} else if (this.kind == Kind.Rounded) {
				g.fillRoundRect(b.x, b.y, b.width, b.height, r, r);
			} else if (this.kind == Kind.Square) {
				g.fillRect(b.x, b.y, b.width, b.height);
			}
			if (selected) {
				g.setColor(Color.darkGray);
				g.drawRect(b.x, b.y, b.width, b.height);
			}
		}

		/**
		 * Return this node's location.
		 */
		public Point getLocation() {
			return p;
		}

		/**
		 * Return true if this node contains p.
		 */
		public boolean contains(Point p) {
			return b.contains(p);
		}

		/**
		 * Return true if this node is selected.
		 */
		public boolean isSelected() {
			return selected;
		}

		/**
		 * Mark this node as selected.
		 */
		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		/**
		 * Collected all the selected nodes in list.
		 */
		public static void getSelected(List<Node1> list, List<Node1> selected) {
			selected.clear();
			for (Node1 n : list) {
				if (n.isSelected()) {
					selected.add(n);
				}
			}
		}

		/**
		 * Select no nodes.
		 */
		public static void selectNone(List<Node1> list) {
			for (Node1 n : list) {
				n.setSelected(false);
			}
		}

		/**
		 * Select a single node; return true if not already selected.
		 */
		public static boolean selectOne(List<Node1> list, Point p) {
			for (Node1 n : list) {
				if (n.contains(p)) {
					if (!n.isSelected()) {
						Node1.selectNone(list);
						n.setSelected(true);
					}
					return true;
				}
			}
			return false;
		}

		/**
		 * Select each node in r.
		 */
		public static void selectRect(List<Node1> list, Rectangle r) {
			for (Node1 n : list) {
				n.setSelected(r.contains(n.p));
			}
		}

		/**
		 * Toggle selected state of each node containing p.
		 */
		public static void selectToggle(List<Node1> list, Point p) {
			for (Node1 n : list) {
				if (n.contains(p)) {
					n.setSelected(!n.isSelected());
				}
			}
		}

		/**
		 * Update each node's position by d (delta).
		 */
		public static void updatePosition(List<Node1> list, Point d) {
			for (Node1 n : list) {
				if (n.isSelected()) {
					n.p.x += d.x;
					n.p.y += d.y;
					n.setBoundary(n.b);
				}
			}
		}

		/**
		 * Update each node's radius r.
		 */
		public static void updateRadius(List<Node1> list, int r) {
			for (Node1 n : list) {
				if (n.isSelected()) {
					n.r = r;
					n.setBoundary(n.b);
				}
			}
		}

		/**
		 * Update each node's color.
		 */
		public static void updateColor(List<Node1> list, Color color) {
			for (Node1 n : list) {
				if (n.isSelected()) {
					n.color = color;
				}
			}
		}

		/**
		 * Update each node's kind.
		 */
		public static void updateKind(List<Node1> list, Kind kind) {
			for (Node1 n : list) {
				if (n.isSelected()) {
					n.kind = kind;
				}
			}
		}
	}

	public static class ColorIcon implements Icon {

		public static final int WIDE = 20;
		public static final int HIGH = 20;
		public Color color;

		public ColorIcon(Color color) {
			this.color = color;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.setColor(color);
			g.fillRect(x, y, WIDE, HIGH);
		}

		public int getIconWidth() {
			return WIDE;
		}

		public int getIconHeight() {
			return HIGH;
		}
	}
}
