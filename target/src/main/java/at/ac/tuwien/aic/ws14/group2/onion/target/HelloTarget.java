package at.ac.tuwien.aic.ws14.group2.onion.target;

/**
 * Sample class to test Gradle setup.
 */
public class HelloTarget {
    /**
     * Main method
     * @param args CLI arguments
     */
    public static void main(String [] args) {
        if(args == null || args.length == 0) {
            System.out.println("No args");
        } else {
            for (String arg : args) {
                System.out.println(arg);
            }
        }
    }
}
