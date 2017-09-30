package agents.online;

import agents.gui.ClockAgentUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import various.IntegerConstants;

import java.util.ArrayList;

/**
 * Created by Darling on 15/9/2017.
 * Testing Github
 */
public class ClockAgent extends Agent {

    private int slot = 0;
    private int step = 200;
    private boolean finish = false;
    private long initial_millis;
    private ArrayList<AID> receivers;

    private ClockAgentUI ui;

    public void setup () {

        super.setup();
        System.out.println("Clock created!");

        receivers = new ArrayList<>();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("timer");
        sd.setName("clock");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Clock registered successfully in the yellow pages!");

        addBehaviour(new RegisterBehaviour());
        addBehaviour(new CreateUI());
        //addBehaviour(new StartExecution());
    }


    private class RegisterBehaviour extends Behaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive();

            if(msg != null) {
                ui.appendConsole(msg.getSender().getLocalName() + " requested timer!");
                receivers.add(msg.getSender());
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return finish;
        }
    }

    public void sendMessages () {

        initial_millis = System.currentTimeMillis() + 2*step;

        for (AID a: receivers) {
            ACLMessage msg = new ACLMessage(IntegerConstants.CLOCK_TICK);
            msg.setContent(initial_millis + "-" + step);
            msg.setPerformative(IntegerConstants.CLOCK_TICK);
            msg.addReceiver(a);
            ui.appendConsole("Sending to: " + a.getLocalName());
            send(msg);
        }
    }


    private class StartExecution extends Behaviour {

        private boolean finish;
        @Override
        public void action() {


            System.out.println("Why you no work");
            ACLMessage msg = new ACLMessage(IntegerConstants.CLOCK_TICK);
            msg.setContent((System.currentTimeMillis()) + "-" + step);
            System.out.println("Clock sending start message!");
            for (AID a: receivers) {
                System.out.println(a.getLocalName());
                msg.addReceiver(a);
            }
            myAgent.send(msg);
            this.finish = true;
        }

        @Override
        public boolean done() {
            return this.finish;
        }
    }


    private class CreateUI extends OneShotBehaviour {
        @Override
        public void action() {
            ui = new ClockAgentUI((ClockAgent) myAgent);
        }
    }
}
