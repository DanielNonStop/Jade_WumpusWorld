package jade_wumpus;

import java.util.Random;
import java.util.List;

public class WumpusPercept {
	
	public static final class SpeleologistPhrases {
		public static List<String> pit_info = List.of("I feel breeze.", "It's breezy here.", "There is a breeze.");
		public static List<String> wumpus_info = List.of("I smell something.", "It's stinky here.", "There is a stench.");
		public static List<String> gold_info = List.of("I see something shiny.", "It's glittery here.", "There is a glitter.");
		public static List<String> wall_info = List.of("I hit the wall.", "It's bumping here.", "There is a bump.");
		public static List<String> wumpusKilled_info = List.of("I hear something.", "It's screaming here.", "There is a scream.");
	}

	private Random randomGenerator = new Random();
	
    private boolean stench = false;
    private boolean breeze = false;
    private boolean glitter = false;
    private boolean scream = false;

    public WumpusPercept setStench() {
        stench = true;
        return this;
    }

    public WumpusPercept setBreeze() {
        breeze = true;
        return this;
    }

    public WumpusPercept setGlitter() {
        glitter = true;
        return this;
    }

    public WumpusPercept setScream() {
        scream = true;
        return this;
    }

    @Override	
    public String toString() {
    	randomGenerator = new Random();
        StringBuilder result = new StringBuilder();
        int index = 0;
        if (breeze) {
        	index = randomGenerator.nextInt(SpeleologistPhrases.pit_info.size());
            result.append(SpeleologistPhrases.pit_info.get(index) + " "); }
        if (stench) {
        	index = randomGenerator.nextInt(SpeleologistPhrases.wumpus_info.size());
            result.append(SpeleologistPhrases.wumpus_info.get(index) + " "); }
        if (glitter) {
        	index = randomGenerator.nextInt(SpeleologistPhrases.gold_info.size());
            result.append(SpeleologistPhrases.gold_info.get(index) + " "); }
        if (scream) {
        	index = randomGenerator.nextInt(SpeleologistPhrases.wumpusKilled_info.size());
            result.append(SpeleologistPhrases.wumpusKilled_info.get(index) + " "); }
        return result.toString();
    }
}