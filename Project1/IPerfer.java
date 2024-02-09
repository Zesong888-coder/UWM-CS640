import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class IPerfer {
    private static final int PACKET_SIZE = 1000;
    private static final int MIN_PORT_NUMBER = 1024;
    private static final int MAX_PORT_NUMBER = 65535;

    private static void runClient(String hostname, int portNumber, int time) {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + (time * 1000);
        double sent_byte = 0;

        try (Socket clienSocket = new Socket()) {
            InetSocketAddress host = new InetSocketAddress(hostname, portNumber);
            clienSocket.connect(host);
            
            byte[] packet = new byte[PACKET_SIZE];

            // Send data in chunks until the specified time elapses
            while (System.currentTimeMillis() < endTime) {
                clienSocket.getOutputStream().write(packet);
                sent_byte += PACKET_SIZE;
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to connect to the server!");
            System.exit(1);
        }

        // Calculate and display the summary
        long elapsedTime = System.currentTimeMillis() - startTime;
        double rate = (8.0 * sent_byte / 1000.0) / (double) elapsedTime;
        System.out.println("sent=" + (int) (sent_byte / 1000) + " KB rate=" + rate + " Mbps");
    }

    private static void runServer(int portNumber) {
        long startTime = System.currentTimeMillis();
        double received = 0;

        try (ServerSocket serverSocket = new ServerSocket()) {
            InetSocketAddress host = new InetSocketAddress(portNumber);
            serverSocket.bind(host);

            Socket clienSocket = serverSocket.accept();
            byte[] packet = new byte[PACKET_SIZE];
            int bytesRead;

            // Receive data until the client closes the connection
            while ((bytesRead = clienSocket.getInputStream().read(packet)) != -1) {
                received += bytesRead;
            }
            clienSocket.close();
        } catch (IOException e) {
            System.out.println("Error: Unable to start the server!");
            System.exit(1);
        }

        // Calculate and display the summary
        long elapsedTime = System.currentTimeMillis() - startTime;
        double rate = (8.0 * received / 1000.0) / (double) elapsedTime;
        System.out.println("received=" + (int) (received / 1000) + " KB rate=" + rate + " Mbps");
    }

    public static void main(String[] args) {
        if (args.length < 3 || args.length > 7) {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }

        boolean clientMode = false;
        boolean serverMode = false;
        String hostName = null;
        int portNumber = 0;
        int time = 0;

        // Parse command-line arguments
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.equals("-c")) {
                clientMode = true;
            } else if (arg.equals("-s")) {
                serverMode = true;
            } else if (arg.equals("-h")) {
                if (++i < args.length) {
                    hostName = args[i];
                } else {
                    System.out.println("Error: missing or additional arguments");
                    System.exit(1);
                }
            } else if (arg.equals("-p")) {
                if (++i < args.length) {
                    // Handle port number parsing and validation
                    try {
                        portNumber = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: port number must be an integer");
                        System.exit(2);
                    }

                    if (portNumber < MIN_PORT_NUMBER || portNumber > MAX_PORT_NUMBER) {
                        System.out.println("Error: port number must be in the range 1024 to 65535");
                        System.exit(2);
                    }
                } else {
                    System.out.println("Error: missing or additional arguments");
                    System.exit(1);
                }
            } else if (arg.equals("-t")) {
                if (++i < args.length) {
                    try {
                        time = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        System.out.println("Error: time must be an integer");
                        System.exit(2);
                    }
                } else {
                    System.out.println("Error: missing or additional arguments");
                    System.exit(1);
                }
            } else {
                System.out.println("Error: unrecognized option -" + arg);
                System.exit(1);
            }
        }

        // Execute corresponding mode based on command line arguments
        if (clientMode && hostName != null && portNumber != 0 && time != 0) {
            runClient(hostName, portNumber, time);
        } else if (serverMode && portNumber != 0) {
            runServer(portNumber);
        } else {
            System.out.println("Error: missing or additional arguments");
            System.exit(1);
        }
    }
}
