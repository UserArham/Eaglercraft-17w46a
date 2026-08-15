import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PaperSignalSender {
    private static final int TYPE_AUTH = 3;
    private static final int TYPE_EXEC = 2;

    public static void main(String[] args) {
        String ip = "127.0.0.1"; // Replace with your server IP
        int port = 25575;
        String password = "YourSecurePassword123";
        String command = "say Hello from Java Link!"; // The signal command

        try (Socket socket = new Socket(ip, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // 1. Authenticate with the server
            sendRconPacket(out, 1, TYPE_AUTH, password);
            readRconPacket(in); // Read auth response

            // 2. Send the execution signal
            sendRconPacket(out, 2, TYPE_EXEC, command);
            String response = readRconPacket(in);
            
            System.out.println("Server Response: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendRconPacket(DataOutputStream out, int id, int type, String payload) throws IOException {
        byte[] body = payload.getBytes(StandardCharsets.US_ASCII);
        int length = 4 + 4 + body.length + 2;

        ByteBuffer buffer = ByteBuffer.allocate(4 + length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(length);
        buffer.putInt(id);
        buffer.putInt(type);
        buffer.putDouble(0); // Appends Null padding bytes required by Valve RCON
        out.write(buffer.array(), 0, 4 + 8 + body.length);
        out.write(new byte[]{0, 0}); // Packet terminator bytes
        out.flush();
    }

    private static String readRconPacket(DataInputStream in) throws IOException {
        byte[] sizeBytes = new byte[4];
        in.readFully(sizeBytes);
        int length = ByteBuffer.wrap(sizeBytes).order(ByteOrder.LITTLE_ENDIAN).getInt();

        byte[] payload = new byte[length];
        in.readFully(payload);

        ByteBuffer buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        int id = buffer.getInt();
        int type = buffer.getInt();

        return new String(payload, 8, length - 10, StandardCharsets.UTF_8);
    }
}
