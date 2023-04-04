package ca.ubc.cs.cs317.dnslookup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class DNSMessageTest {
    @Test
    public void testConstructor() {
        DNSMessage message = new DNSMessage((short) 23);
        assertFalse(message.getQR());
        assertFalse(message.getRD());
        assertEquals(0, message.getQDCount());
        assertEquals(0, message.getANCount());
        assertEquals(0, message.getNSCount());
        assertEquals(0, message.getARCount());
        assertEquals(23, message.getID());
    }

    @Test
    public void testConstructorUnsigned() {
        DNSMessage message = new DNSMessage((short) 65534);
        assertEquals(65534, message.getID());
    }

    @Test
    public void fieldAccesscareful() {
        DNSMessage message = new DNSMessage((short) 65534);
        message.setOpcode(15);
        message.setQDCount(32768);
        message.setANCount(32768);
        message.setARCount(32768);
        message.setNSCount(32768);

        assertEquals(15, message.getOpcode());
        assertEquals(32768, message.getQDCount());
        assertEquals(32768, message.getANCount());
        assertEquals(32768, message.getNSCount());
        assertEquals(32768, message.getARCount());
    }

    @Test
    public void testBasicFieldAccess() {
        DNSMessage message = new DNSMessage((short) 23);
        message.setID(64);
        message.setQR(true);
        message.setAA(true);
        message.setOpcode(11);
        message.setTC(true);
        message.setRD(true);
        message.setRcode(11);
        message.setQDCount(100);
        message.setARCount(100);

        assertEquals(64, message.getID());
        assertTrue(message.getQR());
        assertTrue(message.getAA());
        assertEquals(11, message.getOpcode());
        assertTrue(message.getTC());
        assertTrue(message.getRD());
        assertEquals(11, message.getRcode());
        assertEquals(100, message.getQDCount());
        assertEquals(100, message.getARCount());
    }

    @Test
    public void testAddQuestion() {
        DNSMessage request = new DNSMessage((short) 23);
        DNSQuestion question = new DNSQuestion("www.northeastern.edu.", RecordType.A, RecordClass.IN);
        request.addQuestion(question);
        byte[] content = request.getUsed();
        ByteBuffer buffer = ByteBuffer.wrap(content);

        int[] expected = new int[] {
                // header
                0x0017, 0x0000, 0x0001,
                0x0000, 0x0000, 0x0000,
                // data
                0x0377, 0x7777, 0x0c6e,
                0x6f72, 0x7468, 0x6561,
                0x7374, 0x6572, 0x6e03,
                0x6564, 0x7500, 0x0001,
                0x0001 };

        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], buffer.getShort());
        }
    }

    @Test
    public void testAddQuestion2() {
        DNSMessage request = new DNSMessage((short) 23);
        DNSQuestion question = new DNSQuestion("norm.cs.ubc.ca", RecordType.A, RecordClass.IN);
        request.addQuestion(question);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        assertEquals(request.getID(), reply.getID());
        assertEquals(request.getQDCount(), reply.getQDCount());
        assertEquals(request.getANCount(), reply.getANCount());
        assertEquals(request.getNSCount(), reply.getNSCount());
        assertEquals(request.getARCount(), reply.getARCount());
        DNSQuestion replyQuestion = reply.getQuestion();
        assertEquals(question, replyQuestion);
    }

    @Test
    public void testAddManyQuestions() {
        DNSMessage request = new DNSMessage((short) 23);
        DNSQuestion question1 = new DNSQuestion("norm.cs.ubc.ca", RecordType.A, RecordClass.IN);
        DNSQuestion question2 = new DNSQuestion("www.google.com", RecordType.AAAA, RecordClass.OTHER);
        request.addQuestion(question1);
        request.addQuestion(question2);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        DNSQuestion replyQuestion = reply.getQuestion();
        assertEquals(question1, replyQuestion);
        DNSQuestion replyQuestion2 = reply.getQuestion();
        assertEquals(question2, replyQuestion2);
    }

    @Test
    public void testAddResourceRecord() {
        DNSMessage request = new DNSMessage((short) 23);
        DNSQuestion question = new DNSQuestion("norm.cs.ubc.ca", RecordType.NS, RecordClass.IN);
        ResourceRecord rr = new ResourceRecord(question, RecordType.NS.getCode(), "ns1.cs.ubc.ca");
        request.addResourceRecord(rr);
        byte[] content = request.getUsed();

        DNSMessage reply = new DNSMessage(content, content.length);
        assertEquals(request.getID(), reply.getID());
        assertEquals(request.getQDCount(), reply.getQDCount());
        assertEquals(request.getANCount(), reply.getANCount());
        assertEquals(request.getNSCount(), reply.getNSCount());
        assertEquals(request.getARCount(), reply.getARCount());
        ResourceRecord replyRR = reply.getRR();
        assertEquals(rr, replyRR);
    }

    @Test
    public void testAddAResourceRecord() {
        DNSMessage request = new DNSMessage((short) 24);
        DNSQuestion question = new DNSQuestion("norm.cs.ubc.ca", RecordType.A, RecordClass.IN);
        try {
            ResourceRecord rr = new ResourceRecord(question, RecordType.A.getCode(), InetAddress
                    .getByName("142.103.10.41"));
            request.addResourceRecord(rr);
            byte[] content = request.getUsed();

            DNSMessage reply = new DNSMessage(content, content.length);
            ResourceRecord replyRR = reply.getRR();
            assertEquals(rr, replyRR);
        } catch (Exception e) {
        }
    }

    @Test
    public void testAddMXResourceRecord() {
        DNSMessage request = new DNSMessage((short) 24);
        DNSQuestion question = new DNSQuestion("ubc.ca", RecordType.MX, RecordClass.IN);
        try {
            ResourceRecord rr = new ResourceRecord(question, RecordType.MX.getCode(), "mail.ubc.ca");
            ResourceRecord rr2 = new ResourceRecord(question, RecordType.MX.getCode(), "norm.cs.ubc.ca");
            request.addResourceRecord(rr);
            request.addResourceRecord(rr2);
            byte[] content = request.getUsed();

            DNSMessage reply = new DNSMessage(content, content.length);
            ResourceRecord replyRR = reply.getRR();
            assertEquals(rr, replyRR);
            assertEquals(RecordType.MX, replyRR.getRecordType());

            replyRR = reply.getRR();
            assertEquals(rr2, replyRR);
            assertEquals(RecordType.MX, replyRR.getRecordType());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    public void testAddNameCompression() {
        DNSMessage request = new DNSMessage((short) 24);
        assertEquals(6, request.addNameGetSize("test"));
        assertEquals(4, request.addNameGetSize("a.test"));
    }
}
