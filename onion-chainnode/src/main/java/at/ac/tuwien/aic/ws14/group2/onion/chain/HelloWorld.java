package at.ac.tuwien.aic.ws14.group2.onion.chain;

import static at.ac.tuwien.aic.ws14.group2.onion.comon.HelloLib.testPrint;

public class HelloWorld {
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
