package agents.online;

import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import various.IntegerConstants;
import io.JSONFileParser;

/**
 * Created by Darling on 15/9/2017.
 */
class EVOnlineAgent extends OnlineAgent {


    private int energy;
    private int inform_slot;
    private int bid;
    private int start;
    private int end;
    private AID station;
    private String data_string;


    @Override
    protected void setup() {
        super.setup();
        System.out.println("EVObject agent created!");

        JSONFileParser p = new JSONFileParser();
        Object[] args = this.getArguments();
        if(args != null) {
            energy = Integer.parseInt(args[0].toString());
            inform_slot = Integer.parseInt(args[1].toString());
            bid = Integer.parseInt(args[2].toString());
            start = Integer.parseInt(args[3].toString());
            end = Integer.parseInt(args[4].toString());

            data_string = p.getJSONStringEV(bid, start, end, energy);
        }

        this.setUIDimensions(250, 800);
        addBehaviour(new CreateUI());

        addBehaviour(new RegisterStationBehaviour());

        addBehaviour(new ClockRequestBehaviour());
        addBehaviour(new ReceiveStartingMillis());

        addBehaviour(new EVBehaviour());

    }



    private class EVBehaviour extends Behaviour {

        private int step = 0;
        @Override
        public void action() {
            switch (step) {
                case 0:
                    if (current_slot == inform_slot) {
                        ui.appendConsole("I am going to send a message now.");
                        ACLMessage msg = new ACLMessage(IntegerConstants.CHARGE_REQUEST);
                        msg.setContent(data_string);
                        msg.setConversationId("charging");
                        msg.addReceiver(station);
                        myAgent.send(msg);
                        step++;
                    }

            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }

    private class RegisterStationBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            printName();
            ui.appendConsole("Searching for registered station in the yellow pages.");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("charge");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                printName();
                ui.appendConsole("Found the following station:");
                ui.appendConsole(result[0].getName()+"");
                station = result[0].getName();
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }
    }


    private void printName () {
        System.out.print(getLocalName() + ": ");
    }
}
