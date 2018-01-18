package agents.offline;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import station.Station;
import io.JSONFileParser;

/**
 * Created by Darling on 8/8/2017.
 */
class StationAgent extends Agent {

    private Station station;

    @Override
    protected void setup () {
        super.setup();
        System.out.println("Station agent created!");
        //station = new Station();
        System.out.println("Station agent is registering in the yellow pages...");
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("charge");
        sd.setName("station");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Station registered successfully in the yellow pages!");
        try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //addBehaviour(new StationBehaviour(this, 2000));

        addBehaviour(new StationBehaviour());
    }


    private class StationBehaviour extends Behaviour {
        @Override
        public void action() {


            JSONFileParser p = new JSONFileParser();
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                station.addEVBidder(msg.getContent());
                station.printEVBidders();
            } else {
                station.computeSchedule();
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }
}
