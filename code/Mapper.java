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

	//VARIABLES FOR THE A STAR SEARCH
	protected Boolean beginOn;
	protected Boolean endOn;
	private static Node finish;
	private static Node start;

	//BUTTON FOR SETTING THE ENDING SEARCH NODE
	@Override
	protected void setEnd() {
		endOn = true;
		beginOn = false;
	}

	//BUTTON FOR SETTING THE BEGINNING SEARCH NODE
	@Override
	protected void setBeginning() {
		beginOn = true;
		endOn = false;
	}

	//BUTTON AND METHOD FOR THE A STAR SEARCH
	@Override
	protected void aStarSearch() {
		//THIS IF PREVENTS ANY ISSUES WITH THE START OR FINISH. IF A START AND AN END EXIST, THEN THERE IS AN ANSWER (PRE-ONEWAY TRAVEL)

		if(start!=null && finish!=null){
			//THE COMPARATOR FOR THE SMALLEST DISTANCE OBJECT IN THE A STAR SEARCH
			PriorityQueue<Fringe> aList = new PriorityQueue<Fringe>(new Comparator<Fringe>() {
				@Override
				public int compare(Fringe o1, Fringe o2) {

					return Double.compare(o1.getDistance(), o2.getDistance());
				}
			}
			);

			//LOCAL INSTANCES VARIABLES FOR THE A STAR SEARCH
			Fringe Temp;
			ArrayList<Node> visited = new ArrayList<Node>();
			Stack<Fringe> route = new Stack<Fringe>();
			Stack<Fringe> finalRoute = new Stack<Fringe>();
			Fringe cursor = new Fringe(start, null, null, 0);
			Double totalDistance = 0.0;
			Double duplicateDistance = 0.0;
			String message = "";

			aList.add(cursor);

			//THIS IS THE A STAR METHOD ITSELF. BY RELYING ON ITERATING THROUGH THE AVALIABLE SEGMENTS UNTIL FINDING THE ENDING.
			//IT RELIES ON THE QUEUE OF FRINGES OBJECT AND THE SORT OF THE QUEUE FOR THE CORRECT ROUTE.
			while(!aList.isEmpty()) {
				cursor = aList.poll();
				route.add(cursor);
				visited.add(cursor.getCurrent());

				if(cursor.getCurrent()==finish){ //peeek then quit
					break;
				}

				for (Segment seg : cursor.getCurrent().segments) {
					if (seg.end != cursor.getCurrent() && (!visited.contains(seg.end))) {
						aList.add(new Fringe(seg.end, cursor.getCurrent(), seg, (cursor.getJourney() + cursor.getDistance())));
					} else if (seg.start != cursor.getCurrent() && (!visited.contains(seg.start)) && (seg.road.oneway!=1))  {
						aList.add(new Fringe(seg.start, cursor.getCurrent(), seg, (cursor.getJourney() + cursor.getDistance())));
					}
				}
			}

			//THIS WORKS BACKWARDS TO ENSURE THAT THE WE HAVE THE RIGHT ROUTE.
			//MOVING FROM FINISH TO START BY ITERATING THROUGH THE FRINGE ELEMENTS BY THEIR PREVIOUS ELEMENT TILL THE START
			if(route.peek().getCurrent() == finish){
				Temp = route.pop();
				finalRoute.add(Temp);
				while(!route.isEmpty()){
					if(route.peek().getCurrent() == Temp.getPrevious()){
						if(route.peek().getCurrent()==start){
							finalRoute.add(Temp);
							finalRoute.add(route.peek());
							break;
						}
						Temp = route.pop();
						finalRoute.add(Temp);
					}
					else{
						route.pop();
					}
				}
			}

			//BUG NUMBER 1
			finalRoute.pop();
			finalRoute.pop();

			//NOW THAT WE HAVE A STACK WITH THE PROPER ORDER, THIS METHOD PRINTS THE ROUTE, IN ORDER TO THE SCREEN WITH THEIR DISTANCE (HOPEFFULLY IN A STEP)
			while(!finalRoute.isEmpty()){
				Temp = finalRoute.pop();
				graph.highlightedRoads.add(Temp.getConnector().road);
				if(Temp.getConnector()!=null) {
					duplicateDistance = Temp.getConnector().length;
						while (finalRoute.size() > 0 && finalRoute.peek()!=null && finalRoute.peek().getConnector()!=null && Temp.getConnector().road == finalRoute.peek().getConnector().road) {
							duplicateDistance += finalRoute.peek().getConnector().length;
							Temp = finalRoute.pop();
							graph.highlightedRoads.add(Temp.getConnector().road);
						}
					totalDistance+=duplicateDistance;

					message+=(Temp.getConnector().road.name + ": " + String.format("%.2f", duplicateDistance) + "km" + "\n");
				}
			}
			message+=("Total Distance: " + String.format("%.2f", totalDistance) + "km" + "\n");
			getTextOutputArea().setText(message);
		}
		else if(finish==null){getTextOutputArea().setText("The end node is not defined.");}
		else if(start==null){getTextOutputArea().setText("The start node is not defined");}
		else if(start==null && finish==null){getTextOutputArea().setText("Neither the start nor the finish are defined.");}
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