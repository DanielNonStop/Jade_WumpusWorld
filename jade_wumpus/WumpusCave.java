package jade_wumpus;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class WumpusCave {

    private int caveXDimension;
    private int caveYDimension;

    private AgentPosition start = new AgentPosition(1, 1, AgentPosition.Orientation.FACING_NORTH);
    private Room wumpus;
    private Room gold;
    private Set<Room> pits = new LinkedHashSet<>();

    private Set<Room> allowedRooms;

    public WumpusCave() {
        this(4,4);
    }
    
    public WumpusCave(int caveXDimension, int caveYDimension) {
        if (caveXDimension < 1)
            throw new IllegalArgumentException("Cave must have x dimension >= 1");
        if (caveYDimension < 1)
            throw new IllegalArgumentException("Case must have y dimension >= 1");
        this.caveXDimension = caveXDimension;
        this.caveYDimension = caveYDimension;
        allowedRooms = getAllRooms();
    }

    public WumpusCave(int caveXDimension, int caveYDimension, String config) {
        this(caveXDimension, caveYDimension);
        if (config.length() != 2 * caveXDimension * caveYDimension)
            throw new IllegalStateException("Wrong configuration length.");
        for (int i = 0; i < config.length(); i++) {
            char c = config.charAt(i);
            Room r = new Room(i / 2 % caveXDimension + 1, caveYDimension - i / 2 / caveXDimension);
            switch (c) {
                case 'S': start = new AgentPosition(r.getX(), r.getY(), AgentPosition.Orientation.FACING_NORTH); break;
                case 'W': wumpus = r; break;
                case 'G': gold = r; break;
                case 'P': pits.add(r); break;
            }
        }
    }

    public WumpusCave setAllowed(Set<Room> allowedRooms) {
        this.allowedRooms.clear();
        this.allowedRooms.addAll(allowedRooms);
        return this;
    }

    public void setWumpus(Room room) {
        wumpus = room;
    }

    public void setGold(Room room) {
        gold = room;
    }

    public void setPit(Room room, boolean b) {
        if (!b)
            pits.remove(room);
        else if (!room.equals(start.getRoom()) && !room.equals(gold))
            pits.add(room);
    }

    public int getCaveXDimension() {
        return caveXDimension;
    }

    public int getCaveYDimension() {
        return caveYDimension;
    }

    public AgentPosition getStart() {
        return start;
    }

    public Room getWumpus() {
        return wumpus;
    }

    public Room getGold() {
        return gold;
    }

    public boolean isPit(Room room) {
        return pits.contains(room);
    }

    public Set<Room> getAllRooms() {
        Set<Room> allowedRooms = new HashSet<>();
        for (int x = 1; x <= caveXDimension; x++)
            for (int y = 1; y <= caveYDimension; y++)
                allowedRooms.add(new Room(x, y));
        return allowedRooms;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int y = caveYDimension; y >= 1; y--) {
            for (int x = 1; x <= caveXDimension; x++) {
                Room r = new Room(x, y);
                String txt = "";
                if (r.equals(start.getRoom()))
                    txt += "S";
                if (r.equals(gold))
                    txt += "G";
                if (r.equals(wumpus))
                    txt += "W";
                if (isPit(r))
                    txt += "P";

                if (txt.isEmpty())
                    txt = ". ";
                else if (txt.length() == 1)
                    txt += " ";
                else if ( txt.length() > 2) // cannot represent...
                    txt = txt.substring(0, 2);
                builder.append(txt);
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
