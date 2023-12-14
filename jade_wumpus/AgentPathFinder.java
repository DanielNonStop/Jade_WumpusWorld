package jade_wumpus;

import java.util.ArrayList;
import java.util.List;

public class AgentPathFinder {

    public static List<String> findPath(String[][] rooms_info, Room start, Room end, String direction) {
        List<String> path = new ArrayList<>();
        int currentX = start.getX();
        int currentY = start.getY();
        String current_dir = direction;
        Room tmp = start;
        
        while (currentX != end.getX() || currentY != end.getY()) {
            String nextMove = getNextMove(rooms_info, currentX, currentY, end.getX(), end.getY(), current_dir);
            path.add(nextMove);
            if (nextMove.equals(WumpusWorldAgent.Constants.MESSAGE_FORWARD)) {
            	tmp = updatePosition(nextMove, current_dir, tmp);
            }else {
            	current_dir = updateDirection(current_dir, nextMove);
            }
            currentX = tmp.getX();
            currentY = tmp.getY();
        }

        return path;
    }

    private static String getNextMove(String[][] rooms_info, int currentX, int currentY, int targetX, int targetY, String direction) {
        if ((currentX < targetX) && (rooms_info[currentX+1][currentY] != null)) {
            if(direction == "North") {
            	return WumpusWorldAgent.Constants.MESSAGE_FORWARD;
            }else {
            	return "turn " + getTurnDirection(direction, "North");
            }
        } else if ((currentX > targetX) && (rooms_info[currentX-1][currentY] != null)) {
            if(direction == "South") {
            	return WumpusWorldAgent.Constants.MESSAGE_FORWARD;
            }else {
            	return "turn " + getTurnDirection(direction, "South");
            }
        } else if ((currentY < targetY) && (rooms_info[currentX][currentY+1] != null)) {
            if(direction == "East") {
            	return WumpusWorldAgent.Constants.MESSAGE_FORWARD;
            }else {
            	return "turn " + getTurnDirection(direction, "East");
            }
        } else {
            if((direction == "West") && (rooms_info[currentX][currentY-1] != null)) {
            	return WumpusWorldAgent.Constants.MESSAGE_FORWARD;
            }else {
            	return "turn " + getTurnDirection(direction, "West");
            }
        }
    }

    private static String getTurnDirection(String currentDirection, String targetDirection) {
        if (currentDirection.equals(targetDirection)) {
            return WumpusWorldAgent.Constants.MESSAGE_FORWARD;
        }

        switch (currentDirection) {
            case "North":
                return targetDirection.equals("East") ? "right." : "left.";
            case "East":
                return targetDirection.equals("South") ? "right." : "left.";
            case "South":
                return targetDirection.equals("West") ? "right." : "left.";
            case "West":
                return targetDirection.equals("North") ? "right." : "left.";
            default:
                return "";
        }
    }

    private static Room updatePosition(String move, String direction, Room position) {
        switch (direction) {
            case "North":
            	position.setX(position.getX()+1);
                break;
            case "East":
            	position.setY(position.getY()+1);
                break;
            case "South":
            	position.setX(position.getX()-1);
                break;
            case "West":
            	position.setY(position.getY()-1);
                break;
        }

        return position;
    }

    private static String updateDirection(String currentDirection, String move) {
    	String turnDirection = move.split(" ")[1];
        switch (currentDirection) {
            case "North":
                return turnDirection.equals("right.") ? "East" : "West";
            case "East":
                return turnDirection.equals("right.") ? "South" : "North";
            case "South":
                return turnDirection.equals("right.") ? "West" : "East";
            case "West":
                return turnDirection.equals("right.") ? "North" : "South";
            default:
                return "";
        }
    }
}
