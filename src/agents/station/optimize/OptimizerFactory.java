package agents.station.optimize;

public class OptimizerFactory {

    public static Optimizer getOptimizer (String type) {
        switch (type) {
            case "profit":
                return new ProfitCPLEX();
            case "service":
                return new ServiceCPLEX();
            case "alternatives":
                return new AlternativesCPLEX();
        }
        return null;
    }
}
