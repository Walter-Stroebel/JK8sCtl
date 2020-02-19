/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.util.Arrays;

/**
 *
 * @author walter
 */
public class Proxy {

    final static boolean debug = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        System.in.close();
        InetAddress listen;
        InetAddress connect;
        int portFrom;
        int portTo;
        if (args[1].equals("any")) {
            listen = null;
        } else {
            listen = InetAddress.getByName(args[1]);
        }
        connect = InetAddress.getByName(args[3]);
        portFrom = Integer.parseInt(args[2]);
        portTo = Integer.parseInt(args[4]);
        switch (args[0]) {
            case "TCP":
                new TCP(listen, portFrom, connect, portTo).start();
                break;
            case "UDP":
                new UDP(listen, portFrom, connect, portTo).start();
                break;
        }
    }

    private static class Mem {

        final byte[] buf;
        int siz;

        public Mem(int size) {
            int rSize = size / 256;
            if (size % 256 > 0) {
                rSize++;
            }
            rSize *= 256;
            this.buf = new byte[rSize];
            Arrays.fill(this.buf, (byte) 126);
            siz = size;
        }
    }
    private static final TreeMap<Integer, LinkedList<Mem>> memory = new TreeMap<>();

    private static Mem alloc(int size) {
        int rSize = size / 256;
        if (size % 256 > 0) {
            rSize++;
        }
        rSize *= 256;
        synchronized (memory) {
            LinkedList<Mem> get = memory.get(rSize);
            if (null != get && !get.isEmpty()) {
                Mem ret = get.poll();
                ret.siz = size;
                return ret;
            }
            return new Mem(size);
        }
    }

    private static void free(Mem mem) {
        Arrays.fill(mem.buf, (byte) 126);
        synchronized (memory) {
            LinkedList<Mem> get = memory.get(mem.buf.length);
            if (null == get) {
                memory.put(mem.buf.length, get = new LinkedList<>());
            }
            get.add(mem);
        }
    }

    private static class TCP extends Thread {

        private final InetAddress connect;
        private final int portTo;

        final ServerSocket server;

        public TCP(InetAddress listen, int portFrom, InetAddress connect, int portTo) throws IOException {
            server = new ServerSocket(portFrom, 10, listen);
            this.connect = connect;
            this.portTo = portTo;
        }

        @Override
        public void run() {
            while (!server.isClosed()) {
                try {
                    Socket client = server.accept();
                    new Forward(client, connect, portTo).start();
                } catch (IOException ex) {
                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        server.close();
                    } catch (IOException ex1) {
                        Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        }

        private static class Forward extends Thread {

            final Socket client;

            private final InetAddress connect;
            private final int portTo;

            Forward(Socket client, InetAddress connect, int portTo) {
                this.client = client;
                this.connect = connect;
                this.portTo = portTo;
            }

            @Override
            public void run() {
                final LinkedList<Mem> outPending = new LinkedList<>();
                final LinkedList<Mem> inPending = new LinkedList<>();
                try (final Socket proxy = new Socket(connect, portTo)) {

                    new socketToQueue(client, inPending).start();
                    new socketToQueue(proxy, outPending).start();
                    new queueToSocket(inPending, proxy).start();
                    new queueToSocket(outPending, client).start();
                    while (true) {
                        if (client.isClosed()) {
                            if (debug) {
                                System.out.println("Client is closed " + client);
                            }
                            // client won't accept any more data
                            synchronized (outPending) {
                                while (!outPending.isEmpty()) {
                                    free(outPending.poll());
                                }
                                outPending.notifyAll();
                            }
                            if (!proxy.isClosed() && inPending.isEmpty()) {
                                proxy.close();
                                synchronized (inPending) {
                                    inPending.notifyAll();
                                }
                            }
                        } else if (debug) {
                            System.out.println("Client is active " + client);
                        }

                        if (proxy.isClosed()) {
                            if (debug) {
                                System.out.println("Proxy is closed " + proxy);
                            }
                            // proxy won't accept any more data
                            synchronized (inPending) {
                                while (!inPending.isEmpty()) {
                                    free(inPending.poll());
                                }
                                inPending.notifyAll();
                            }
                            if (!client.isClosed() && outPending.isEmpty()) {
                                client.close();
                                synchronized (outPending) {
                                    outPending.notifyAll();
                                }
                            }
                        } else if (debug) {
                            System.out.println("Proxy is active " + proxy);
                        }
                        if (client.isClosed() && proxy.isClosed()) {
                            break;
                        }
                        try {
                            sleep(100);
                            if (debug) sleep(2000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Proxy.class.getName()).log(Level.SEVERE, null, ex);
                }
                synchronized (inPending) {
                    while (!inPending.isEmpty()) {
                        free(inPending.poll());
                    }
                    inPending.notifyAll();
                }
                synchronized (outPending) {
                    while (!outPending.isEmpty()) {
                        free(outPending.poll());
                    }
                    outPending.notifyAll();
                }
                if (debug){
                    for (Map.Entry<Integer, LinkedList<Mem>> e:memory.entrySet()){
                        System.out.format("MEM %6d * %6d\n",e.getKey(),e.getValue().size());
                    }
                }
            }

            private static class socketToQueue extends Thread {

                private final Socket s;

                private final LinkedList<Mem> q;

                public socketToQueue(Socket s, LinkedList<Mem> q) {
                    this.s = s;
                    this.q = q;
                }

                @Override
                public void run() {
                    try {
                        final InputStream in = s.getInputStream();
                        final byte[] read = new byte[32768];
                        while (!s.isClosed()) {
                            int a = in.read(read);
                            if (a > 0) {
                                Mem mem = alloc(a);
                                System.arraycopy(read, 0, mem.buf, 0, a);
                                synchronized (q) {
                                    q.add(mem);
                                    q.notifyAll();
                                }
                            } else {
                                in.close();
                                break;
                            }
                        }
                    } catch (IOException ex) {
                        if (debug) {
                            System.out.println(ex);
                        }
                    }
                }
            }

            private static class queueToSocket extends Thread {

                private final LinkedList<Mem> q;
                private final Socket s;

                public queueToSocket(LinkedList<Mem> q, Socket s) {
                    this.q = q;
                    this.s = s;
                }

                @Override
                public void run() {
                    try (OutputStream out = s.getOutputStream()) {
                        while (!s.isClosed()) {
                            Mem tx = null;
                            synchronized (q) {
                                if (q.size() > 1) {
                                    int totSize = 0;
                                    for (Mem mem : q) {
                                        totSize += mem.siz;

                                    }
                                    if (0 == totSize) {
                                        q.wait();
                                    } else {
                                        tx = alloc(totSize);
                                        totSize = 0;
                                        for (Iterator<Mem> it = q.iterator(); it.hasNext();) {
                                            Mem mem = it.next();
                                            System.arraycopy(mem.buf, 0, tx.buf, totSize, mem.siz);
                                            totSize += mem.siz;
                                            free(mem);
                                            it.remove();
                                        }
                                    }
                                } else {
                                    tx = q.poll();
                                }
                            }
                            if (null != tx) {
                                out.write(tx.buf, 0, tx.siz);
                                free(tx);
                            }
                        }
                    } catch (Exception ex) {
                        if (debug) System.out.println(ex);
                    }
                }
            }
        }
    }

    private static class UDP extends Thread {

        private final InetAddress connect;
        private final int portTo;

        final DatagramSocket server;

        public UDP(InetAddress listen, int portFrom, InetAddress connect, int portTo) throws SocketException {
            server = new DatagramSocket(portFrom, listen);
            this.connect = connect;
            this.portTo = portTo;
        }
    }

}
