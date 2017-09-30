package agents.online;

import agents.offline.StationAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import station.Station;
import various.IntegerConstants;
import various.JSONFileParser;

/**
 * Created by Darling on 15/9/2017.
 */
public class StationOnlineAgent extends OnlineAgent {

    private Station station;

    @Override
    protected void setup () {
        super.setup();
        System.out.println("Station agent created!");

        station = new Station();

        this.setUIDimensions(250, 800);
        addBehaviour(new CreateUI());

        addBehaviour(new RegisterToYellowPagesBehaviour());
        addBehaviour(new ClockRequestBehaviour());
        addBehaviour(new ReceiveStartingMillis());

        addBehaviour(new StationBehaviour());
        //addBehaviour(new ComputeScheduleBehaviour());

    }





    private class StationBehaviour extends Behaviour {

        @Override
        public void action() {


            JSONFileParser p = new JSONFileParser();
            MessageTemplate mt = MessageTemplate.MatchPerformative(IntegerConstants.CHARGE_REQUEST);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                station.addEVBidder(msg.getContent());
                ui.appendConsole(station.printEVBidders());
            } else {
                station.computeSchedule();
                ui.appendConsole("Station: Read all the messages in the que!");
                block();
            }
        }

        @Override
        public boolean done() {
            return false;
        }
    }

    private class RegisterToYellowPagesBehaviour extends OneShotBehaviour {


        @Override
        public void action() {
            System.out.println("Station agent is registering in the yellow pages...");
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("charge");
            sd.setName("station");
            dfd.addServices(sd);
            try {
                DFService.register(myAgent, dfd);
            }
            catch (FIPAException fe) {
                fe.printStackTrace();
            }
            System.out.println("Station registered successfully in the yellow pages!");

        }
    }

    private class ComputeScheduleBehaviour extends CyclicBehaviour {

        private int bidders_num = 0;
        @Override
        public void action() {

            if (station.getBiddersNumber() == 2) {
                station.computeSchedule();
            }

        }
    }


}
