package at.ac.tuwien.aic.ws14.group2.onion.directory;

import static at.ac.tuwien.aic.ws14.group2.onion.directory.api.HelloLib.testPrint;

public class HelloDirectory {
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
