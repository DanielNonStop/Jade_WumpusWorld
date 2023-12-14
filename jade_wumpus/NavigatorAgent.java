package jade_wumpus;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;
import java.lang.Math;

public class NavigatorAgent extends Agent {
	
    @Override
    protected void setup() {
        System.out.println("The navigator agent " + getAID().getName() + " is ready.");

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(Constants.NAVIGATOR_AGENT_TYPE);
        sd.setName(Constants.NAVIGATOR_SERVICE_DESCRIPTION);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        addBehaviour(new AnalyzeRequests());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("The navigator agent " + getAID().getName() + " is terminated.");
    }

    private class AnalyzeRequests extends CyclicBehaviour {

    	List<String> moves_history = new ArrayList<>();
    	List<String> plan = new ArrayList<>();
    	boolean is_gold_found = false;
    	String[][] rooms_info = new String[4][4];
    	String[][] wumpus_info = new String[4][4];
    	String[][] pit_info = new String[4][4];
    	AgentPosition agent_info = null;
    	
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                if (parseSpeleologistMessageRequest(msg.getContent())){
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REQUEST);
                    reply.setContent(Constants.INFORMATION_PROPOSAL_NAVIGATOR);
                    System.out.println("NavigatorAgent: " + Constants.INFORMATION_PROPOSAL_NAVIGATOR);
                    if(agent_info == null) {
                    	agent_info = new AgentPosition(0, 0, AgentPosition.Orientation.FACING_NORTH);
                    }
                    myAgent.send(reply);
                } else if (parseSpeleologistMessageProposal(msg.getContent()))
                {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.PROPOSE);
                    String advice = getAdvice(msg.getContent());
                    reply.setContent(advice);
                    System.out.println("NavigatorAgent: " + advice);
                    myAgent.send(reply);

                } else
                    System.out.println("NavigatorAgent: Wrong message!");
            } else {
                block();
            }
        }

        private boolean parseSpeleologistMessageRequest(String instruction) {
            String regex = "\\bHelp\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(instruction);
            if (matcher.find()) {
                String res = matcher.group();
                return res.length() > 0;
            }
            return false;
        }

        private boolean parseSpeleologistMessageProposal(String instruction) {
            String regex = "\\bPosition\\b";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(instruction);
            if (matcher.find()) {
                String res = matcher.group();
                return res.length() > 0;
            }
            return false;
        }
        
        private String getAdvice(String content){
            boolean stench = false;
            boolean breeze = false;
            boolean glitter = false;
            boolean scream = false;
            boolean nothing = true;
            String advicedAction = "";

			for(String word : PerceptKeyWords.stench) {
				if(content.contains(word)) {
					stench = true;
					nothing = false;
					break;
				}
			}
			for(String word : PerceptKeyWords.breeze) {
				if(content.contains(word)) {
					breeze = true;
					nothing = false;
					break;
				}
			}
			for(String word : PerceptKeyWords.glitter) {
				if(content.contains(word)) {
					glitter = true;
					nothing = false;
					break;
				}
			}
			for(String word : PerceptKeyWords.scream) {
				if(content.contains(word)) {
					scream = true;
					break;
				}
			}

    		if(is_gold_found == false) {
    			if(glitter) {
    				advicedAction = WumpusWorldAgent.Constants.MESSAGE_GRAB; 
    				is_gold_found=true;
    				moves_history.add(WumpusWorldAgent.Constants.MESSAGE_RIGHT);
                	moves_history.add(WumpusWorldAgent.Constants.MESSAGE_RIGHT);
    			} else {
    				if(plan.size() == 0){
        				if(nothing) {
        					advicedAction = WumpusWorldAgent.Constants.MESSAGE_FORWARD;
        					fillSafetyInfo();
        					fillWumpusInfo(false); 
        					fillPitInfo(false); 
        					rooms_info[agent_info.getY()][agent_info.getX()] = "Test";
        					findSafeRooms();
        					//printRoomsInfo();
        				}else {
        					if(stench) {
    	    					fillWumpusInfo(true); 
    	    				}else {
    	    					fillWumpusInfo(false); 
    	    				}
        					if(breeze) {
    	    					fillPitInfo(true); 
    	    				}else {
    	    					fillPitInfo(false);
    	    				}
        					rooms_info[agent_info.getY()][agent_info.getX()] = "Test";
        					findSafeRooms();
        					//printRoomsInfo();
        					Room end_point = findClosestSafe(agent_info.getY(), agent_info.getX());
        					if(end_point.getX() == -1) {
        						System.out.println("It is necessary to risk.");
        						end_point = findRiskRoom(agent_info.getY(), agent_info.getX());
        					}
        					plan = AgentPathFinder.findPath(rooms_info, new Room(agent_info.getY(), agent_info.getX()), end_point, agent_info.getOrientation().getSymbol());
        					//System.out.println(plan);
        					advicedAction = plan.remove(0);
        						
        				}
    				}
    				else {
    					System.out.println(plan);
    					advicedAction = plan.remove(0);
    				}
    				switch(advicedAction) {
    				    case WumpusWorldAgent.Constants.MESSAGE_FORWARD: agent_info.moveForward(); break;
    				    case WumpusWorldAgent.Constants.MESSAGE_LEFT: agent_info.turnLeft(); break;
    				    case WumpusWorldAgent.Constants.MESSAGE_RIGHT: agent_info.turnRight(); break;
    				}
                    moves_history.add(advicedAction);
    			}
            }
    		else {
    			System.out.println(agent_info.getX() + " " + agent_info.getY());
    			if((moves_history.size() == 0) || ((agent_info.getX() == 0) && (agent_info.getY() == 0))) {
    				advicedAction = WumpusWorldAgent.Constants.MESSAGE_CLIMB;
    			}else {
        			advicedAction = moves_history.remove(moves_history.size()-1);
        			switch (advicedAction) {
        			case WumpusWorldAgent.Constants.MESSAGE_RIGHT: advicedAction = WumpusWorldAgent.Constants.MESSAGE_LEFT; agent_info.turnLeft(); break;
        			case WumpusWorldAgent.Constants.MESSAGE_LEFT: advicedAction = WumpusWorldAgent.Constants.MESSAGE_RIGHT; agent_info.turnRight(); break;
        			case WumpusWorldAgent.Constants.MESSAGE_FORWARD: agent_info.moveForward(); break;
        			}
    			}
            }
            
            Random random_generator = new Random();
            return PerceptKeyWords.action_proposals.get(random_generator.nextInt(PerceptKeyWords.action_proposals.size())) + advicedAction;
        }

    	private void fillWumpusInfo(boolean if_stench) {
    		String info = "";
    		int tmp_y = agent_info.getX();
    		int tmp_x = agent_info.getY();
    		if(if_stench) {
    			info = "Wump";
    		}else {
    			info = "Not!";
    			wumpus_info[tmp_x][tmp_y] = info;
    		}
    		
    		if(tmp_x != 0) {
    			if(if_stench && wumpus_info[tmp_x - 1][tmp_y] == null) {
    				wumpus_info[tmp_x - 1][tmp_y] = info;
    			}else if(info == "Not!") {
    				wumpus_info[tmp_x - 1][tmp_y] = info;
    			}
    		}
    		if(tmp_x != rooms_info[0].length) {
    			if(if_stench && wumpus_info[tmp_x + 1][tmp_y] == null) {
    				wumpus_info[tmp_x + 1][tmp_y] = info;
    			}else if(info == "Not!") {
    				wumpus_info[tmp_x + 1][tmp_y] = info;
    			}
    		}
    		if(tmp_y != 0) {
    			if(if_stench && wumpus_info[tmp_x][tmp_y - 1] == null) {
    				wumpus_info[tmp_x][tmp_y - 1] = info;
    			}else if(info == "Not!") {
    				wumpus_info[tmp_x][tmp_y - 1] = info;
    			}
    		}
    		if(tmp_y != rooms_info[0].length) {
    			if(if_stench && wumpus_info[tmp_x][tmp_y + 1] == null) {
    				wumpus_info[tmp_x][tmp_y + 1] = info;
    			}else if(info == "Not!") {
    				wumpus_info[tmp_x][tmp_y + 1] = info;
    			}
    		}
    	}
    	
    	private void fillPitInfo(boolean if_breeze) {
    		String info = "";
    		int tmp_y = agent_info.getX();
    		int tmp_x = agent_info.getY();
    		if(if_breeze) {
    			info = "Pit";
    		}else {
    			info = "Not!";
    			pit_info[tmp_x][tmp_y] = info;
    		}
    		System.out.println(tmp_x + " " + tmp_y);
    		if(tmp_x != 0) {
    			if(if_breeze && pit_info[tmp_x - 1][tmp_y] == null) {
    				pit_info[tmp_x - 1][tmp_y] = info;
    			}else if(info == "Not!") {
    				pit_info[tmp_x - 1][tmp_y] = info;
    			}
    		}
    		if(tmp_x != rooms_info[0].length) {
    			if(if_breeze && pit_info[tmp_x + 1][tmp_y] == null) {
    				pit_info[tmp_x + 1][tmp_y] = info;
    			}else if(info == "Not!") {
    				pit_info[tmp_x + 1][tmp_y] = info;
    			}
    		}
    		if(tmp_y != 0) {
    			if(if_breeze && pit_info[tmp_x][tmp_y - 1] == null) {
    				pit_info[tmp_x][tmp_y - 1] = info;
    			}else if(info == "Not!") {
    				pit_info[tmp_x][tmp_y - 1] = info;
    			}
    		}
    		if(tmp_y != rooms_info[0].length) {
    			if(if_breeze && pit_info[tmp_x][tmp_y + 1] == null) {
    				pit_info[tmp_x][tmp_y + 1] = info;
    			}else if(info == "Not!") {
    				pit_info[tmp_x][tmp_y + 1] = info;
    			}
    		}
    	}
    	
    	private void fillSafetyInfo() {
    		int tmp_y = agent_info.getX();
    		int tmp_x = agent_info.getY();
    		
    		rooms_info[tmp_x][tmp_y] = "Safe";
    		if(tmp_x != 0) {
    			if(rooms_info[tmp_x - 1][tmp_y] != "Test") {
    				rooms_info[tmp_x - 1][tmp_y] = "Safe";
    			}
    		}
    		if(tmp_x != rooms_info[0].length) {
    			rooms_info[tmp_x + 1][tmp_y] = "Safe";
    		}
    		if(tmp_y != 0) {
    			rooms_info[tmp_x][tmp_y - 1] = "Safe";
    		}
    		if(tmp_y != rooms_info[0].length) {
    			rooms_info[tmp_x][tmp_y + 1] = "Safe";
    		}
    	}
    	
        private void printRoomsInfo() {
        	for(int x = 0; x < rooms_info.length; x++) {
        		for(int y = 0; y < rooms_info.length; y++) {
            		System.out.print(rooms_info[rooms_info.length-x-1][y] + " ");
            	}
        		System.out.println();
        	}
        	for(int x = 0; x < wumpus_info.length; x++) {
        		for(int y = 0; y < wumpus_info.length; y++) {
            		System.out.print(wumpus_info[wumpus_info.length-x-1][y] + " ");
            	}
        		System.out.println();
        	}
        	for(int x = 0; x < pit_info.length; x++) {
        		for(int y = 0; y < pit_info.length; y++) {
            		System.out.print(pit_info[pit_info.length-x-1][y] + " ");
            	}
        		System.out.println();
        	}
        }
        
        private Room findClosestSafe(int x, int y) {
        	
        	int best_dist = 10;
        	int best_x = -1;
        	int best_y = -1;
        	
        	for(int tmp_x = 0; tmp_x < rooms_info.length; tmp_x++) {
        		for(int tmp_y = 0; tmp_y < rooms_info.length; tmp_y++) {
            		if(rooms_info[tmp_x][tmp_y] == "Safe") {
            			int distance = (int) (Math.pow(x - tmp_x, 2) + Math.pow(y - tmp_y, 2));
            			if((best_x == -1) || (distance < best_dist)) {
            				best_dist = distance;
            				best_x = tmp_x;
            				best_y = tmp_y;
            			}
            		}
            	}
        	}
        	
			return new Room(best_x, best_y);
        }
        
        private Room findRiskRoom(int x, int y) {
        	
        	int best_dist = 10;
        	int best_risk = 3;
        	int best_x = -1;
        	int best_y = -1;
        	
        	for(int tmp_x = 0; tmp_x < rooms_info.length; tmp_x++) {
        		for(int tmp_y = 0; tmp_y < rooms_info.length; tmp_y++) {
            		int tmp_risk = 0;
            		if(wumpus_info[tmp_x][tmp_y] == "Wump") {
            			tmp_risk += 1;
            		}
            		if(pit_info[tmp_x][tmp_y] == "Pit") {
            			tmp_risk += 1;
            		}
        			int distance = (int) (Math.pow(x - tmp_x, 2) + Math.pow(y - tmp_y, 2));
        			if(tmp_risk <= best_risk ) {
            			if((best_x == -1) || (distance < best_dist)) {
            				best_dist = distance;
            				best_risk = tmp_risk;
            				best_x = tmp_x;
            				best_y = tmp_y;
            			}
        			}
            	}
        	}
        	
			return new Room(best_x, best_y);
        }
        
        private void findSafeRooms() {
        	
        	for(int tmp_x = 0; tmp_x < rooms_info.length; tmp_x++) {
        		for(int tmp_y = 0; tmp_y < rooms_info.length; tmp_y++) {
        			if((pit_info[tmp_x][tmp_y] == "Not!") && (wumpus_info[tmp_x][tmp_y] == "Not!") && (rooms_info[tmp_x][tmp_y] == null)) {
        				rooms_info[tmp_x][tmp_y] = "Safe";
        			}
        		}
        	}
        }
        
    	public static final class PerceptKeyWords {
    		public static List<String> breeze = List.of("breez");
    		public static List<String> stench = List.of("stench", "stinky", "smell");
    		public static List<String> glitter = List.of("glitter", "shiny");
    		public static List<String> scream = List.of("scream", "hear");
    		public static List<String> action_proposals = List.of("It seems to me you should ", "It would be a good idea to ", "I think you can ");
    	}	    	
    }
    
    final static class Constants {
    	
        static final String NAVIGATOR_DIGGER_CONVERSATION_ID = "digger_navigator";
        static final String NAVIGATOR_AGENT_TYPE = "navigator_agent";
        static final String NAVIGATOR_SERVICE_DESCRIPTION = "navigator";
        static final String INFORMATION_PROPOSAL_NAVIGATOR = "Give me your position.";
        
    }
}
