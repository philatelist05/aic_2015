package at.ac.tuwien.aic.ws14.group2.onion.chain;

import static at.ac.tuwien.aic.ws14.group2.onion.common.HelloLib.testPrint;

/**
 * Sample class to test Gradle setup.
 */
public class HelloWorldChain {
    /**
     * Main method
     * @param args CLI arguments
     */
    public static void main(String [] args) {
        if(args == null || args.length == 0) {
            testPrint("No args");
        } else {
            for (String arg : args) {
                testPrint(arg);
            }
        }
    }
}
