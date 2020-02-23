/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

/**
 *
 * @author walter
 */
public class Analyze {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        System.out.println("This is a complex and quite experimental tool.");
        System.out.println("For it to work correcly, you will need SSH access to all nodes.");
        System.out.println("Press Enter to proceed or ^C to abort.");
        System.in.read();
        Maps.collect();
        System.out.println(Maps.analyse());
    }
    
}
