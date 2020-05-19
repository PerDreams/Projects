import java.util.Comparator;

//THIS CLASS IS SPECIFICALLY TO HOLD ALL THE INFORMATION FOR THE A STAR SEARCH OBJECTS
public class Fringe {
    private Node current;
    private Node previous;
    private double journey;
    private double distance;
    private Segment connector;

    //THIS IS THE CURRENT AND PREVIOUS NODE OBJECT, THEIR SEGMENT, THE LENGTH OF THE SEGMENT (EUCLIDEAN BETWEEN POINTS), AND THE TOTAL LENGTH TO THIS SEGMENT (THAT MAKES IT MUCH EASIER TO COMPARE FOR LEAST LENGTH)
    public Fringe(Node current, Node previous, Segment connector, double journey){
        this.current = current;
        this.previous = previous;
        this.journey = journey;
        this.connector = connector;
        //THIS IS THE EUCLIDEAN DISTANCE (REFERENCED IN THE ASSIGNMENT). THIS CAN EASILY BE SWAPPED WITH SEGMENT LENGTH.
        if(connector!=null){this.distance = (Math.sqrt(Math.pow((previous.location.x - current.location.x), 2) + Math.pow((previous.location.y - current.location.y), 2))) + this.journey;}
        else{this.distance = 0;}
    }

    public void setDistance(Double d){this.distance = d;}

    public Node getCurrent(){return this.current;}

    public Node getPrevious(){return this.previous;}

    public double getJourney(){return this.journey;}

    public double getDistance(){return this.distance;}

    public Segment getConnector(){return this.connector;}
}
