package snmpagent;

import java.io.IOException;
import org.snmp4j.Snmp;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import static org.snmp4j.mp.SnmpConstants.sysDescr;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 *
 * @author Kyna Ghafar
 */
public class SnmpAgent {

    Snmp snmp = null;
    String address = null;
    
/**
* Constructor
* @param plus
*/
public SnmpAgent(String plus){
    address = plus;
}
    public static void main(String[] args) throws IOException {

    /*Start listening to udp:127.0.0.1 and using port 161 */
    SnmpAgent cl = new SnmpAgent("udp:127.0.0.1/161");
    cl.start();

    /**
    * sysDescr (Gives full name and version identification of system's hardware type, software os-system and networking software)
    * sysObjectID (The vendor's authoritative identification of thenetwork management subsystem contained in the  entity)
    * sysUpTime(The time (in hundredths of a second) since the network management portion of the system was last re-initialized)
    * sysName(An administratively-assigned name for this managed node.  By convention, this is the node's fully-qualified domain name)
    * sysServices(Gives a value which indicates the set of services that this entity primarily offers)
    */
    String sysDescr = cl.getAsString(new OID(".1.3.6.1.2.1.1.1.0"));
    String sysObjectID = cl.getAsString(new OID(".1.3.6.1.2.1.1.2.0"));
    String sysUpTime = cl.getAsString(new OID(".1.3.6.1.2.1.1.3.0"));
    String sysName = cl.getAsString(new OID(".1.3.6.1.2.1.1.5.0"));
    String sysServices = cl.getAsString(new OID(".1.3.6.1.2.1.1.7.0"));
 
    System.out.println(sysDescr);
    System.out.println(sysObjectID);
    System.out.println(sysUpTime);
    System.out.println(sysName);
    System.out.println(sysServices);
 }

    /** 
    * @throws IOException
    * The transport mapping just receives the SNMP message as a stream of bytes and forwards the message 
    * to associated MessageDispatcher instances.
    */
    private void start() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        //listen for answer
        transport.listen();
    }

    /**
    * Method which takes a single OID and returns the response from the agent as a String.
    * @param oid
    * @return
    * @throws IOException
    */
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[] { oid });
        return event.getResponse().get(0).getVariable().toString();
    }

    /**
    * This method can handle multiple OIDs
    * @param oids
    * @return
    * @throws IOException
    */
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids) {
            pdu.add(new VariableBinding(oid));
        }
        pdu.setType(PDU.GET);
    
        ResponseEvent event = snmp.send(pdu, getTarget(), null);
    
        if(event != null) {
            return event;
        }
    
        throw new RuntimeException("Timed out");
        }

    /**
    * This method returns a Target, which contains information about
    * where the data should be fetched and how.
    * @return
    * User Target
    */
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(2000);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

}