public class Fringe implements Comparable<Fringe>{
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
        if(connector!=null){this.distance = connector.length;}
        else{this.distance = 0;}
    }

    public Node getCurrent(){return this.current;}

    public Node getPrevious(){return this.previous;}

    public double getJourney(){return this.journey;}

    public double getDistance(){return this.distance;}

    public Segment getConnector(){return this.connector;}

    public int compareTo(Fringe next){
        return Double.compare(this.distance, next.getDistance());
    }
}
