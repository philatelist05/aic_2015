package at.ac.tuwien.aic.ws14.group2.onion.node.common.node;

import org.junit.*;

import java.net.InetAddress;

import static org.junit.Assert.*;

public class CircuitTest {

    @Test
    public void createRandomID() throws Exception {
        Endpoint e = new Endpoint(InetAddress.getByName("www.google.at"), 80);

        Circuit c1 = new Circuit(e);
        Circuit c2 = new Circuit(e);

        assertNotEquals(c1.getCircuitID(), c2.getCircuitID());
    }
}