import java.util.Comparator;

public class Fringe {
    private Node current;
    private Node previous;
    private double journey;
    private double distance;
    private Segment connector;

    public Fringe(Node current, Node previous, Segment connector, double journey){
        this.current = current;
        this.previous = previous;
        this.journey = journey;
        this.connector = connector;
        if(connector!=null){this.distance = Math.sqrt(Math.pow((previous.location.x - current.location.x), 2) + Math.pow((previous.location.y - current.location.y), 2));}
        else{this.distance = 0;}
    }

    public void setDistance(Double d){this.distance = d;}

    public Node getCurrent(){return this.current;}

    public Node getPrevious(){return this.previous;}

    public double getJourney(){return this.journey;}

    public double getDistance(){return this.distance;}

    public Segment getConnector(){return this.connector;}
}
