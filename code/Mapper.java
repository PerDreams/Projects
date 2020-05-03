import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;

/**
 * This is the main class for the mapping program. It extends the GUI abstract
 * class and implements all the methods necessary, as well as having a main
 * function.
 * 
 * @author tony
 */
public class Mapper extends GUI{
	public static final Color NODE_COLOUR = new Color(77, 113, 255);
	public static final Color SEGMENT_COLOUR = new Color(130, 130, 130);
	public static final Color HIGHLIGHT_COLOUR = new Color(255, 219, 77);

	// these two constants define the size of the node squares at different zoom
	// levels; the equation used is node size = NODE_INTERCEPT + NODE_GRADIENT *
	// log(scale)
	public static final int NODE_INTERCEPT = 1;
	public static final double NODE_GRADIENT = 0.8;

	// defines how much you move per button press, and is dependent on scale.
	public static final double MOVE_AMOUNT = 100;
	// defines how much you zoom in/out per button press, and the maximum and
	// minimum zoom levels.
	public static final double ZOOM_FACTOR = 1.3;
	public static final double MIN_ZOOM = 1, MAX_ZOOM = 200;

	// how far away from a node you can click before it isn't counted.
	public static final double MAX_CLICKED_DISTANCE = 0.15;

	// these two define the 'view' of the program, ie. where you're looking and
	// how zoomed in you are.
	private Location origin;
	private double scale;

	// our data structures.
	private Graph graph;

	protected Boolean beginOn;
	protected Boolean endOn;
	private static Node finish;
	private static Node start;

	@Override
	protected void setEnd() {
		endOn = true;
		beginOn = false;
	}

	@Override
	protected void setBeginning() {
		beginOn = true;
		endOn = false;
	}

	@Override
	protected void aStarSearch() {
		if(start!=null && finish!=null){
			PriorityQueue<Fringe> aList = new PriorityQueue<Fringe>(new Comparator<Fringe>() {
				@Override
				public int compare(Fringe o1, Fringe o2) {

					return Double.compare(o1.getDistance(), o2.getDistance());
				}
			}

			);

			Segment Temp;
			ArrayList<Node> visited = new ArrayList<Node>();
			Stack<Segment> route = new Stack<Segment>();
			Fringe cursor = new Fringe(start, null, null, 0);

			aList.add(cursor);
			System.out.println("Start of A star");
			while(!aList.isEmpty()) {
				cursor = aList.poll();
				route.add(cursor.getConnector());
				visited.add(cursor.getCurrent());

				if(cursor.getCurrent()==finish){
					System.out.println("fuck2");
					break;
				}

				for (Segment seg : graph.getSegments(cursor.getCurrent())) {
					if (seg.end != cursor.getCurrent() && (!visited.contains(seg.end))) {
						aList.add(new Fringe(seg.end, cursor.getCurrent(), seg, (cursor.getJourney() + cursor.getDistance())));
					} else if (seg.start != cursor.getCurrent() && (!visited.contains(seg.start))) {
						aList.add(new Fringe(seg.start, cursor.getCurrent(), seg, (cursor.getJourney() + cursor.getDistance())));
					}
				}
			}

			System.out.println("fuck3");

			if(route.peek().start==finish || route.peek().end==finish){
				System.out.println("fuck4");
				Temp = route.pop();
				while(!route.isEmpty()){
					System.out.println("fuck");
					if(route.peek().end==Temp.start) {
						Temp = route.pop();
						System.out.println(Temp.road);
						if (Temp.start == start) {
							break;
						}
					}
				}
			}
			else{
				System.out.println("fuck5");
				System.out.println(finish.nodeID);
				System.out.println(route.peek().end.nodeID);
				System.out.println(route.peek().start.nodeID);
			}
		}
		else if(finish==null){System.out.println("The end node is not defined.");}
		else if(start==null){System.out.println("The start node is not defined");}
		else if(start==null && finish==null){System.out.println("Neither the start nor the finish are defined.");}
		System.out.println("");
	}

	private Fringe minimumElement(ArrayList<Fringe> fr){
		Fringe min = new Fringe(null, null, null, Double.POSITIVE_INFINITY);
		min.setDistance(Double.POSITIVE_INFINITY);

		for(Fringe f : fr){
			if(f.getDistance() < min.getDistance()){min = f;}
		}

		return min;
	}

	@Override
	protected void redraw(Graphics g) {
		if (graph != null)
			graph.draw(g, getDrawingAreaDimension(), origin, scale);
	}

	@Override
	protected void onClick(MouseEvent e) {
		Location clicked = Location.newFromPoint(e.getPoint(), origin, scale);
		// find the closest node.
		double bestDist = Double.MAX_VALUE;
		Node closest = null;

		for (Node node : graph.nodes.values()) {
			double distance = clicked.distance(node.location);
			if (distance < bestDist) {
				bestDist = distance;
				closest = node;
			}
		}
																		//HIGHLIGHT IT VISUALLY
		// if it's close enough, highlight it and show some information.
		System.out.println("\n");
		if(endOn && clicked.distance(closest.location) < MAX_CLICKED_DISTANCE){
			finish = closest;
			endOn = false;
			getTextOutputArea().setText("End Node is: \n" + closest.toString());
		}
		else if(beginOn && clicked.distance(closest.location) < MAX_CLICKED_DISTANCE){
			start = closest;
			beginOn = false;
			getTextOutputArea().setText("Start Node is: \n" + closest.toString());
		}
		else if (clicked.distance(closest.location) < MAX_CLICKED_DISTANCE) {
			graph.setHighlight(closest);
			getTextOutputArea().setText(closest.toString());
		}
	}

	@Override
	protected void onSearch() {
		//
	}

	@Override
	protected void onMove(Move m) {
		if (m == GUI.Move.NORTH) {
			origin = origin.moveBy(0, MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.SOUTH) {
			origin = origin.moveBy(0, -MOVE_AMOUNT / scale);
		} else if (m == GUI.Move.EAST) {
			origin = origin.moveBy(MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.WEST) {
			origin = origin.moveBy(-MOVE_AMOUNT / scale, 0);
		} else if (m == GUI.Move.ZOOM_IN) {
			if (scale < MAX_ZOOM) {
				// yes, this does allow you to go slightly over/under the
				// max/min scale, but it means that we always zoom exactly to
				// the centre.
				scaleOrigin(true);
				scale *= ZOOM_FACTOR;
			}
		} else if (m == GUI.Move.ZOOM_OUT) {
			if (scale > MIN_ZOOM) {
				scaleOrigin(false);
				scale /= ZOOM_FACTOR;
			}
		}
	}

	@Override
	protected void onLoad(File nodes, File roads, File segments, File polygons) {
		graph = new Graph(nodes, roads, segments, polygons);
		origin = new Location(-250, 250); // close enough
		scale = 1;
	}

	/**
	 * This method does the nasty logic of making sure we always zoom into/out
	 * of the centre of the screen. It assumes that scale has just been updated
	 * to be either scale * ZOOM_FACTOR (zooming in) or scale / ZOOM_FACTOR
	 * (zooming out). The passed boolean should correspond to this, ie. be true
	 * if the scale was just increased.
	 */
	private void scaleOrigin(boolean zoomIn) {
		Dimension area = getDrawingAreaDimension();
		double zoom = zoomIn ? 1 / ZOOM_FACTOR : ZOOM_FACTOR;

		int dx = (int) ((area.width - (area.width * zoom)) / 2);
		int dy = (int) ((area.height - (area.height * zoom)) / 2);

		origin = Location.newFromPoint(new Point(dx, dy), origin, scale);
	}

	public static void main(String[] args) {
		new Mapper();
	}
}

// code for COMP261 assignments