package jade_wumpus;

public class AgentPosition {

    public enum Orientation {
        FACING_NORTH("North"),
        FACING_SOUTH("South"),
        FACING_EAST("East"),
        FACING_WEST("West");

        public String getSymbol() {
            return symbol;
        }

        private final String symbol;

        Orientation(String sym) {
            symbol = sym;
        }
    }

    private Room room;
    private Orientation orientation;

    public AgentPosition(int x, int y, Orientation orientation) {
        this(new Room(x, y), orientation);
    }

    public AgentPosition(Room room, Orientation orientation) {
        this.room = room;
        this.orientation = orientation;
    }

    public Room getRoom() {
        return room;
    }

    public int getX() {
        return room.getX();
    }

    public int getY() {
        return room.getY();
    }

    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public String toString() {
        return room.toString() + "->" + orientation.getSymbol();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && getClass() == obj.getClass()) {
            AgentPosition other = (AgentPosition) obj;
            return (getX() == other.getX()) && (getY() == other.getY())
                    && (orientation == other.getOrientation());
        }
        return false;
    }
    
    public void moveForward() {
        int x = this.getX();
        int y = this.getY();
        switch (this.getOrientation()) {
            case FACING_NORTH: y++; break;
            case FACING_SOUTH: y--; break;
            case FACING_EAST: x++; break;
            case FACING_WEST: x--; break;
        }
        Room new_room = new Room(x, y);
        this.room = new_room;
    }
    
    public void turnLeft() {
        AgentPosition.Orientation new_orientation = null;
        switch (this.getOrientation()) {
            case FACING_NORTH: new_orientation = Orientation.FACING_WEST; break;
            case FACING_SOUTH: new_orientation = Orientation.FACING_EAST; break;
            case FACING_EAST: new_orientation = Orientation.FACING_NORTH; break;
            case FACING_WEST: new_orientation = Orientation.FACING_SOUTH; break;
        }
        this.orientation = new_orientation;
    }
    
    public void turnRight() {
        AgentPosition.Orientation new_orientation = null;
        switch (this.getOrientation()) {
            case FACING_NORTH: new_orientation = AgentPosition.Orientation.FACING_EAST; break;
            case FACING_SOUTH: new_orientation = AgentPosition.Orientation.FACING_WEST; break;
            case FACING_EAST: new_orientation = AgentPosition.Orientation.FACING_SOUTH; break;
            case FACING_WEST: new_orientation = AgentPosition.Orientation.FACING_NORTH; break;
        }
        this.orientation = new_orientation;
    }
}
