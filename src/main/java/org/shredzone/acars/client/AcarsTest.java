package org.shredzone.acars.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * A small test class that sends acarsd commands and shows the received data.
 *
 * @author Richard "Shred" Körber
 */
public class AcarsTest {

    public AcarsTest() throws IOException {
        System.out.println("Connecting...");
        Socket sock = new Socket("acarsd-rjbb.no-ip.info", 2202);
        sock.setTcpNoDelay(true);
        System.out.println("Connected!");

        InputStream input = sock.getInputStream();
        final ReceiverDaemon daemon = new ReceiverDaemon(input);
        System.out.println("Receiver Daemon is running");

        final AcarsOutputStream aco = new AcarsOutputStream(sock.getOutputStream());

        Thread th = new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        try {
                            Command cmd = daemon.getCommandQueue().nextCommand();
                            if (cmd instanceof StringCommand) {
                                StringCommand ccmd = (StringCommand) cmd;
                                System.out.println("RECEIVED: " + ccmd.getOperation());
                                System.out.println(ccmd.getValue());
                            } else if (cmd instanceof ValueCommand) {
                                ValueCommand ccmd = (ValueCommand) cmd;
                                System.out.println("RECEIVED: " + ccmd.getOperation()
                                                + " VALUE=" + ccmd.getValue());
                            } else if (cmd instanceof BinaryCommand) {
                                System.out.println("RECEIVED: " + cmd.getOperation() + " BINARY");
                            } else {
                                System.out.println("RECEIVED: " + cmd.getOperation());
                            }
                            System.out.println("----------------------------------");


                            if (cmd.getOperation() == Operation.AS_WELXML) {
                                System.out.println("SEND: AS_CLIENT");
                                aco.writeString(Operation.AS_CLIENT, "1 65 0");
                                System.out.println("SEND: AS_USECOMP");
                                aco.writeString(Operation.AS_USECOMP, "CXML");
                                System.out.println("SEND: AS_HEART");
                                aco.writeOperation(Operation.AS_HEART);
                                System.out.println("SEND: AS_HEART");
                                aco.writeOperation(Operation.AS_HEART);
                                System.out.println("SEND: AS_REG");
                                aco.writeString(Operation.AS_REG, "JA854A");
                                System.out.println("SEND: AS_HEART");
                                aco.writeOperation(Operation.AS_HEART);
                                System.out.println("SEND: AS_HEART");
                                aco.writeOperation(Operation.AS_HEART);
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                } finally {
                    try {
                        aco.close();
                    } catch (IOException ex) {
                        // ignore, we cannot do anything else
                    }
                }
            }
        });
        th.start();


    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            new AcarsTest();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
