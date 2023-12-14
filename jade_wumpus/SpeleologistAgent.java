package jade_wumpus;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.*;

public class SpeleologistAgent extends Agent {

    private AID wumpusWorld;
    private AID navigationAgent;

    @Override
    protected void setup() {
        System.out.println("The speleologist agent " + getAID().getName() + " is ready.");
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                myAgent.addBehaviour(new FindWumpusWorldAgent());
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("The speleologist agent " + getAID().getName() + " is terminated.");
    }

    private class FindWumpusWorldAgent extends Behaviour {

        private boolean isFound = false;

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(WumpusWorldAgent.Constants.WUMPUS_WORLD_TYPE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    wumpusWorld = result[0].getName();
                    myAgent.addBehaviour(new InitializeWumpusWorld());
                } else {
                    block();
                }
                isFound = true;
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return isFound;
        }
    }

    private class InitializeWumpusWorld extends Behaviour {

        private MessageTemplate mt;
        private int step = 0;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    cfp.addReceiver(wumpusWorld);
                    cfp.setContent(WumpusWorldAgent.Constants.GO_INSIDE);
                    cfp.setConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.CONFIRM) {
                            super.myAgent.addBehaviour(new TickerBehaviour(super.myAgent, 5000) {
                                @Override
                                protected void onTick() {
                                    myAgent.addBehaviour(new FindNavigatorAgent());
                                }
                            });
                            step++;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 2;
        }
    }

    private class FindNavigatorAgent extends Behaviour {

        private boolean isFound = false;

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(NavigatorAgent.Constants.NAVIGATOR_AGENT_TYPE);
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result != null && result.length > 0) {
                    navigationAgent = result[0].getName();
                    myAgent.addBehaviour(new PerformInWumpusWorld());
                    isFound = true;
                } else {
                    block();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean done() {
            return isFound;
        }
    }

    private class PerformInWumpusWorld extends Behaviour {
        private int step = 0;
        private MessageTemplate mt;
        private ACLMessage reply;

        @Override
        public void action() {
            switch (step) {
                case 0:
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.addReceiver(navigationAgent);
                    inform.setContent(Commands.ADVICE_PROPOSAL);
                    inform.setConversationId(NavigatorAgent.Constants.NAVIGATOR_DIGGER_CONVERSATION_ID);
                    inform.setReplyWith("inform" + System.currentTimeMillis());
                    System.out.println("SpeleologistAgent: " + Commands.ADVICE_PROPOSAL);
                    myAgent.send(inform);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId(NavigatorAgent.Constants.NAVIGATOR_DIGGER_CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(inform.getReplyWith()));
                    step++;
                    break;
                case 1:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.REQUEST) {
                        	if (reply.getContent().equals(NavigatorAgent.Constants.INFORMATION_PROPOSAL_NAVIGATOR)) {
                                ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                                req.addReceiver(wumpusWorld);
                                req.setContent(WumpusWorldAgent.Constants.GAME_INFORMATION);
                                req.setConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                                req.setReplyWith("req" + System.currentTimeMillis());
                                myAgent.send(req);
                                mt = MessageTemplate.and(MessageTemplate.MatchConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                                        MessageTemplate.MatchInReplyTo(req.getReplyWith()));
                                step++;
                            } else
                                System.out.println("SpeleologistAgent: There is an unknown command!");
                        }
                    } else {

                        block();
                    }
                    break;
                case 2:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            String rep = reply.getContent();
                            ACLMessage mes = new ACLMessage(ACLMessage.INFORM);
                            mes.addReceiver(navigationAgent);
                            mes.setContent(Commands.INFORMATION_PROPOSAL_SPELEOLOGIST + rep);
                            mes.setConversationId(NavigatorAgent.Constants.NAVIGATOR_DIGGER_CONVERSATION_ID);
                            mes.setReplyWith("mes" + System.currentTimeMillis());
                            System.out.println("SpeleologistAgent: " + Commands.INFORMATION_PROPOSAL_SPELEOLOGIST + rep);
                            myAgent.send(mes);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(NavigatorAgent.Constants.NAVIGATOR_DIGGER_CONVERSATION_ID),
                                    MessageTemplate.MatchInReplyTo(mes.getReplyWith()));
                            step++;
                        }
                    } else
                        block();
                    break;
                case 3:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            String navigator_message = reply.getContent();
                            String action = parseActionMessage(navigator_message);
                            String content = "";

                            switch (action) {
                                case Commands.TURN_LEFT:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_TURN_LEFT;
                                    break;
                                case Commands.TURN_RIGHT:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_TURN_RIGHT;
                                    break;
                                case Commands.MOVE_FORWARD:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_MOVE_FORWARD;
                                    break;
                                case Commands.GRAB:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_GRAB;
                                    break;
                                case Commands.SHOOT:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_SHOOT;
                                    break;
                                case Commands.CLIMB:
                                    content = WumpusWorldAgent.Constants.SPELEOLOGIST_CLIMB;
                                    break;
                                default:
                                    System.out.println("SpeleologistAgent: There is no right action!");
                                    break;
                            }

                            ACLMessage prop = new ACLMessage(ACLMessage.PROPOSE);
                            prop.addReceiver(wumpusWorld);
                            prop.setContent(content);
                            prop.setConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID);
                            prop.setReplyWith("prop" + System.currentTimeMillis());
                            myAgent.send(prop);
                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId(WumpusWorldAgent.Constants.WUMPUS_WORLD_DIGGER_CONVERSATION_ID),
                                    MessageTemplate.MatchInReplyTo(prop.getReplyWith()));
                            step++;
                        }
                    } else {
                        block();
                    }
                    break;
                case 4:
                    reply = myAgent.receive(mt);
                    if (reply != null) {
                        String answer = reply.getContent();
                        switch (answer) {
                            case WumpusWorldAgent.Constants.FAIL_MESSAGE:
                                System.out.println("SpeleologistAgent: You failed!");
                                step++;
                                doDelete();
                                break;
                            case WumpusWorldAgent.Constants.OK_MESSAGE:
                                System.out.println("SpeleologistAgent: Wumpus world answers OK.");
                                step = 0;
                                break;
                            case WumpusWorldAgent.Constants.WIN_MESSAGE:
                                System.out.println("SpeleologistAgent: The speleologist survived and won!");
                                step++;
                                doDelete();
                                break;
                        }
                    } else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return step == 5;
        }

        private String parseActionMessage(String instruction) {
			for(String value : Commands.Commands) {
				if(instruction.contains(value)) {
					return value;
				}
			}
            return "";
        }
    }

    final static class Commands {
        static final String TURN_LEFT = "left";
        static final String TURN_RIGHT = "right";
        static final String MOVE_FORWARD = "forward";
        static final String GRAB = "grab";
        static final String SHOOT = "shoot";
        static final String CLIMB = "climb";
        static final List<String> Commands = List.of("left", "right", "forward", "grab", "shoot", "climb");
        
        static final String ADVICE_PROPOSAL = "Help, what i need to do?";
        static final String INFORMATION_PROPOSAL_SPELEOLOGIST = "Position: ";
        
    }
}