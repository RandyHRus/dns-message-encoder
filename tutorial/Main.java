import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

public class Main {
    private static class DNSMessage {
        private ByteBuffer message;

        public DNSMessage(Path path) throws IOException {
            message = ByteBuffer.wrap(Files.readAllBytes(path));
        }

        public boolean getQR() {
            byte b = message.get(2);
            return (b & 0x80) >> 7 == 1;
        }

        public void setQR(boolean qr) {
            byte b = message.get(2);
            if (qr) {
                b = (byte) (b | 0x80);
            } else {
                b = (byte) (b & 0x7F);
            }
            message.put(2, b);
        }

        public boolean getAA() {
            byte b = message.get(2);
            return (b & 0x04) >> 2 == 1;
        }

        public void setAA(boolean aa) {
            byte b = message.get(2);
            if (aa) {
                b = (byte) (b | 0x04);
            } else {
                b = (byte) (b & 0xFB);
            }
            message.put(2, b);
        }

        public void printByte(int index) {
            byte b = message.get(index);
            System.out.println(String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b))).replace(' ', '0'));
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("./dns_message.bin");
        DNSMessage message = new DNSMessage(path);
        // TODO: Test your getters and setters here
        // E.g. System.out.println(message.getQR())
        message.printByte(2);
        System.out.printf("QR: %s\n", message.getQR());
        message.setQR(true);
        System.out.printf("QR: %s\n", message.getQR());
        message.setQR(false);
        System.out.printf("QR: %s\n", message.getQR());
        System.out.printf("AA: %s\n", message.getAA());
        message.setAA(true);
        System.out.printf("AA: %s\n", message.getAA());
        message.setAA(false);
        System.out.printf("AA: %s\n", message.getAA());
        message.printByte(2);
    }
}
