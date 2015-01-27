package at.ac.tuwien.aic.ws14.group2.onion.node.local.web;

import at.ac.tuwien.aic.ws14.group2.onion.node.common.node.Endpoint;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainMetaData;
import at.ac.tuwien.aic.ws14.group2.onion.node.local.node.ChainNodeMetaData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Stefan on 27.01.15.
 */
public class GetChainBoxesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            throw new ServletException("Session is not initialized");
        }
        long id = (long) session.getAttribute("id");
        WebInformationCallbackImpl callback = (WebInformationCallbackImpl) session.getAttribute("callback");

        List<ChainMetaData> chainBuildUp = callback.getChainBuildUp(id);
        List<ChainMetaData> chainEstablished = callback.getChainEstablished(id);

        Template template = new Template("webapp/templates/chainBoxes.hbs");
        template.render(resp.getWriter(), new Context(chainBuildUp, chainEstablished));
    }

    static class Context {
        Map<Integer, Node> nodes;

        Context(List<ChainMetaData> chainBuildUp, List<ChainMetaData> chainEstablished) {
            nodes = new HashMap<>();
            Map<Integer, ChainNodeMetaData> buildUpNodes = new HashMap<>();
            chainBuildUp.forEach(chainMetaData -> buildUpNodes.putAll(chainMetaData.getNodes()));

            Map<Integer, ChainNodeMetaData> establishedNodes = new HashMap<>();
            chainEstablished.forEach(chainMetaData -> establishedNodes.putAll(chainMetaData.getNodes()));

            buildUpNodes.forEach((integer, chainNodeMetaData) -> {
                List<Chain> chains = new LinkedList<>();
                Endpoint endPoint = chainNodeMetaData.getEndPoint();


                String address = null;
                try {
                    address = endPoint.getAddress().toString();
                } catch (UnknownHostException e) {
                    address = "";
                }


                int port = endPoint.getPort();
                chains.add(new Chain(address, port));
                Node node = nodes.get(integer);
                if (node == null) {
                    node = new Node(chains, integer, chainNodeMetaData.getInstanceId(),
                            chainNodeMetaData.getDomainName(), chainNodeMetaData.getRegion());
                }
                node.buildUp = true;
                nodes.put(integer, node);
            });

            establishedNodes.forEach((integer, chainNodeMetaData) -> {
                List<Chain> chains = new LinkedList<>();
                Endpoint endPoint = chainNodeMetaData.getEndPoint();


                String address = null;
                try {
                    address = endPoint.getAddress().toString();
                } catch (UnknownHostException e) {
                    address = "";
                }


                int port = endPoint.getPort();
                chains.add(new Chain(address, port));
                Node node = nodes.get(integer);
                if (node == null) {
                    node = new Node(chains, integer, chainNodeMetaData.getInstanceId(),
                            chainNodeMetaData.getDomainName(), chainNodeMetaData.getRegion());
                }
                node.established = true;
                nodes.put(integer, node);
            });
        }
        Collection<Node> nodes() {
            return nodes.values();
        }

        static class Node {
            List<Chain> chains;
            long id;
            boolean buildUp;
            boolean established;
            String instanceId;
            String domainName;
            String region;

            Node(List<Chain> chains, long id, String instanceId, String domainName, String region) {
                this.chains = chains;
                this.id = id;
                this.instanceId = instanceId;
                this.domainName = domainName;
                this.region = region;
            }

            List<Chain> chains() {
                return chains;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Node node = (Node) o;

                if (id != node.id) return false;

                return true;
            }

            @Override
            public int hashCode() {
                return (int) (id ^ (id >>> 32));
            }
        }

        static class Chain {
            String address;
            int port;

            public Chain(String address, int port) {
                this.address = address;
                this.port = port;
            }
        }
    }
}
