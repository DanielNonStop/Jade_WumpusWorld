package jade_wumpus;

public class Room {
    private int x = 1;
    private int y = 1;

    public Room(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int new_x) {
        this.x = new_x;
    }

    public void setY(int new_y) {
    	this.y = new_y;
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Room) {
            Room r = (Room) o;
            return x == r.x && y == r.y;
        }
        return false;
    }
}