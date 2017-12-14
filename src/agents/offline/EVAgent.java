package agents.offline;

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
import various.JSONFileParser;

import java.util.ArrayList;

/**
 * Created by Darling on 8/8/2017.
 */
public class EVAgent extends Agent{

    private int energy;
    private int inform_slot;
    private int bid;
    private int start;
    private int end;
    //private int[][] bids;
    private AID station;
    private String data_string;


    @Override
    protected void setup() {
        super.setup();
        System.out.println("EVInfo agent created!");

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


        addBehaviour(new EVBehaviour());
    }

    private class EVBehaviour extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Searching for registered station in the yellow pages.");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("charge");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                System.out.println("Found the following station:");
                System.out.println(result[0].getName());
                station = result[0].getName();
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            System.out.println("Sending message");
            ACLMessage informal = new ACLMessage(ACLMessage.INFORM);
            informal.setContent(data_string);
            informal.setConversationId("charging");
            informal.setReplyWith("inform " + System.currentTimeMillis());
            informal.addReceiver(station);
            myAgent.send(informal);
        }

    }
}
