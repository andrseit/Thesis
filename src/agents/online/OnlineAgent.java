package agents.online;

import agents.gui.AgentUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import various.IntegerConstants;

/**
 * Created by Darling on 28/9/2017.
 */
public class OnlineAgent extends Agent {

    protected AID clock;
    protected AgentUI ui;
    protected long first_slot, step;
    protected int ui_height, ui_width;
    protected int current_slot;

    public void setUIDimensions(int ui_height, int ui_width) {
        this.ui_height = ui_height;
        this.ui_width = ui_width;
    }



    protected class ClockRequestBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Searching for the clock in the yellow pages.");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("timer");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println("Found the following clock:");
                System.out.println(result[0].getName());
                clock = result[0].getName();
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            System.out.println("Sending message");
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setContent(myAgent.getAID() + "");
            request.setConversationId("clock");
            request.setReplyWith("inform " + System.currentTimeMillis());
            request.addReceiver(clock);
            myAgent.send(request);
        }

    }

    protected class ReceiveStartingMillis extends Behaviour {

        boolean finish = false;
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(IntegerConstants.CLOCK_TICK);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {

                String[] tokens = msg.getContent().split("-");
                first_slot = Long.parseLong(tokens[0]);
                step = Long.parseLong(tokens[1]);

                myAgent.addBehaviour(new CPUClockBehaviour());
                finish = true;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return finish;
        }
    }

    protected class CPUClockBehaviour extends Behaviour {

        @Override
        public void action() {

            this.block(step);
            long init = System.currentTimeMillis();
            double millis = ((double) init - (double) first_slot) / (double) step;
            current_slot = (int)millis - 1;
            ui.appendClock(current_slot + "");

        }

        @Override
        public boolean done() {
            return false;
        }
    }

    protected class CreateUI extends OneShotBehaviour {

        @Override
        public void action() {
            ui = new AgentUI(getLocalName(), ui_height, ui_width, myAgent);
        }

    }
}
