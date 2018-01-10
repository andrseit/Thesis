package station.negotiation;

import evs.Preferences;
import station.EVObject;

import java.util.ArrayList;

/**
 * Created by Thesis on 8/12/2017.
 */
public class Conversation {

    // sta evs periexetai to suggestion
    // se autin tin lista periexontai ta evs pou prokeitai na tous ginei protasi
    private ArrayList<EVObject> evs;

    // h lista anamonis, an kapoio oxima exei aporripsei mia protasi kai perimenei alli
    // an exei arnithei teleiws alles protaseis feugei apo pantou
    private ArrayList<EVObject> pendingEvs;

    private ArrayList<EVObject> acceptedEVs;

    // auto to pernaw giati periexei idi tin pliroforia gia tous chargers,
    // opote h eprepe na pernaw tous chargers, h ton computer
    // telika tha pernaw tous chargers gt tous xreiazomai kai parakatw
    private int[] chargers;
    private int[] price;

    private boolean finish;

    public Conversation(ArrayList<EVObject> evs) {
        this.evs = evs;
        this.price = price;
        pendingEvs = new ArrayList<>();
        acceptedEVs = new ArrayList<>();
    }

    public boolean hasFinished () {
        return finish;
    }

    /**
     * WARNING: stin periptwsi pou einai 2 kai  o enas arnithei,
     * enw o deuteros zitisei alli prosfora - tote h pending list tha einai keni
     * alla tha prepei na ginei alternative suggestion
     */
    public void conversation () {

        for (EVObject evInfo: evs) {
            System.out.println("Sending suggestion to ev: " + evInfo.getId());
            Preferences suggestion = evInfo.getFinalSuggestion();
            int accepted = 1; //evInfo.getObjectAddress().evaluateSuggestionOld(suggestion);
            switch (accepted) {
                case 1:
                    System.out.println("    EV accepted suggestion");
                    System.out.println(suggestion.toString());
                    acceptedEVs.add(evInfo);
                    break;
                case 2:
                    System.out.println("    EV waits for a new suggestion");
                    pendingEvs.add(evInfo);
                    break;
                case 3:
                    System.out.println("    EV declined suggestion");
                    break;
            }
        }



        /*
        for (EVObject evInfo: evs) {
            System.out.println("Sending suggestion to ev: " + evInfo.getId());
            Preferences suggestion = evInfo.getSuggestion();
            int accepted = evInfo.getObjectAddress().evaluateSuggestionOld(suggestion);
            switch (accepted) {
                case 1:
                    System.out.println("    EV accepted suggestion");
                    acceptedEVs.add(evInfo);
                    break;
                case 2:
                    System.out.println("    EV waits for a new suggestion");
                    resetChargers(evInfo, 1);
                    computeSuggestionsForPending();
                    pendingEvs.add(evInfo);
                    break;
                case 3:
                    System.out.println("    EV declined suggestion");
                    resetChargers(evInfo, 1);
                    computeSuggestionsForPending();
                    break;
            }
        }

        boolean finished = true;
        for (EVObject ev : pendingEvs) {
            if (!(ev.getBestLessEnergy() < Integer.MAX_VALUE &&
                    ev.getBestAlteredWindow() < Integer.MAX_VALUE))
                finished = false;
        }
        if (finished)
            System.out.println("Negotiation is over!");
        else
            System.out.println("Compute alternative suggestions for remaining evs.");
        */
    }


    /**
     * WARNING: na to kanw na ypologizei neo suggestion mono se periptwsi pou efyge kapoios
     * pou ta slots tous tairiazoun, px an enas arnithike suggestion sto 1-3 kai oloi oi alloi
     * exoun 5-10 arxika slots, e den exei noima
     * TO DO: na ypologizei gia olous kai na pairnei tis top protaseis
     * kai oxi na ypologizei px gia ton prwto na to stelnei, gia ton deutero na to stelnei kok
     * opote h tha xreiastei kai edw priority queue h tha ginetai sort.
     * mporei na ginei ena priority queue pou tha exei mono autous pou exoun dynates alternatives
     */
    /*
    private void computeSuggestionsForPending () {

        PriorityQueue<EVObject> queue = new PriorityQueue<>(10, new Comparator<EVObject>() {
            @Override
            public int compare(EVObject o1, EVObject o2) {
                return o2.getSuggestion().getRating() - o1.getSuggestion().getRating();
            }
        });


        SuggestionComputer computer = new SuggestionComputer(chargers, price, IntegerConstants.SUGGESTION_COMPUTER_CONVERSATION);
        if (!pendingEvs.isEmpty()) {
            System.out.println("Computing for pending, because someone rejected.");
            for (EVObject ev: pendingEvs) {
                if (!computer.computeAlternative(ev)) System.out.println("Could not found better suggestion!");
                else {
                    System.out.println("Found a new suggestion for ev: " + ev.getId());
                    System.out.println("    " + ev.getSuggestion());
                    queue.offer(ev);
                }
            }
        }

        // send new suggestions

        // WARNING: Ananewsi twn chargers!
        EVObject ev;
        while ((ev = queue.poll()) != null) {
            System.out.println("Sending new suggestion to ev: " + ev.getId());
            int accepted = ev.getObjectAddress().evaluateSuggestionOld(ev.getSuggestion());
            switch (accepted) {
                // here the chargers should be updated (-1)
                case 1:
                    System.out.println("    EV accepted suggestion");
                    resetChargers(ev, -1);
                    acceptedEVs.add(ev);
                    break;
                case 2:
                    System.out.println("    EV waits for a new suggestion");
                    pendingEvs.add(ev);
                    break;
                case 3:
                    System.out.println("    EV declined suggestion");
                    break;

            }

        }
    }
    */

    /**
     *
     * @param ev gia na pareis to suggestion pou aparnithike wste na kaneis reset tous chargers
     * @param step einai gia na kses an tha prostheseis h tha afaireseis
     */
    private void resetChargers (EVObject ev, int step) {
        System.out.println("        Reset chargers affected by suggestion.");
        Suggestion suggestion = ev.getSuggestion();
        int start = suggestion.getStart();
        int end = suggestion.getEnd();
        int[] slots_affected = suggestion.getSlotsAfected();
        for (int s = start; s <= end; s++) {
            if (slots_affected[s] == 1)
                chargers[s] += step;
        }
    }

    public ArrayList<EVObject> getAcceptedEVs () { return acceptedEVs; }

    public ArrayList<EVObject> getPendingEvs() {
        return pendingEvs;
    }
}
