package com.georgegalt.tinysqueezeplayer;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by ggalt on 12/18/15.
 */
public class SlimProto {
    private static final String TAG = "SlimProto.Java";

    private TcpSocket tcpSocket;
    private static final int READ_WRITE_BUFFER = 4096;
    private boolean helosent = false;
    private static int threadCount = 0;
    private long epoch;

    private HashMap commandListeners = new HashMap();
    private HashSet connectionListeners = new HashSet();

    SlimProto(){
        epoch = System.currentTimeMillis();

    }

    public void addProtocolListener(String cmd, ProtocolListener listener) {
        ArrayList l = (ArrayList)commandListeners.get(cmd);
        if (l == null) {
            l = new ArrayList();
            commandListeners.put(cmd, l);
        }
        l.add(listener);
        connectionListeners.add(listener);
    }

    public void removeProtocolListener(String cmd, ProtocolListener listener) {
        ArrayList l = (ArrayList)commandListeners.get(cmd);
        if (l == null)
            return;
        l.remove(listener);
        // FIXME shouldn't this be "remove" not "add"??
        connectionListeners.add(listener);
    }

    /**
     * Create a new connection to the slim server using the given address and
     * port.
     */
    public void connect(InetAddress addr, int port) {
        if (addr == null || port < 0) {
            Log.d(TAG, "Invalid server parameters");
            return;
        }

        tcpSocket = new TcpSocket(addr, port);
        tcpSocket.start();
    }

    /**
     * Returns true if connected to the slim server.
     */
    public boolean isConnected() {
        if (tcpSocket == null)
            return false;
        return tcpSocket.isConnected();
    }

    /**
     * Returns the number of jiffies since the player started.
     */
    public int getJiffies() {
        return (int) (System.currentTimeMillis() - epoch);
    }

    /**
     * Send a hello message to the slim server.
     * From server/Slim/Networking/Slimproto.pm from 7.5r28596
     ** squeezebox(2)
     ** softsqueeze(3)
     ** squeezebox2(4)
     ** transporter(5)
     ** softsqueeze3(6)
     ** receiver(7)
     ** squeezeslave(8)
     ** controller(9)
     ** boom(10)
     ** softboom(11)
     ** squeezeplay(12)
     ** radio(13)
     ** touch(14)
    */
    public void sendHELO(int deviceID, int revision, byte[] macaddress,
                         boolean isGraphics, boolean isReconnect) {
        if (!isConnected())
            return;

        try {
            byte args[] = new byte[10];
            args[0] = (byte) deviceID;
            args[1] = (byte) revision;
            System.arraycopy(macaddress, 0, args, 2, 6);

            int channelList = 0;
            if (isGraphics)
                channelList |= 0x8000;
            if (isReconnect)
                channelList |= 0x4000;
            packN2(args, 8, channelList);

            sendCommand("HELO", args);
            helosent = true;
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendHelo", e);
        }
    }


    /**
     * Send a status update to the slim server.
     */
    public void sendStat(String code, byte crlf, byte masInit, byte masMode,
                         int rptr, int wptr, long bytesRx, byte wirelessSignal,
                         int outputBufferSize, int outputBufferFullness, long elapsedMilliseconds, int timestamp) {
        if (!isConnected() || !helosent)
            return;

        try {
            byte args[] = new byte[51];
            System.arraycopy(code.getBytes(), 0, args, 0, 4);
            args[4] = crlf;
            args[5] = masInit;
            args[6] = masMode;
            packN4(args, 7, rptr);
            packN4(args, 11, wptr);
            packN8(args, 15, bytesRx);
            packN2(args, 23, wirelessSignal);
            packN4(args, 25, getJiffies());
            packN4(args, 29, outputBufferSize);
            packN4(args, 33, outputBufferFullness);
            packN4(args, 37, (int)(elapsedMilliseconds/1000));
            packN4(args, 41, 0); // voltage
            packN4(args, 43, (int)elapsedMilliseconds);
            packN4(args, 47, timestamp);

            sendCommand("STAT", args);
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendStat", e);
        }
    }

    /**
     * Send disconnection
     *
     * 0    connection closed normally (FIN)
     * 1    connection reset by local host
     * 2    connection reset by remote host
     * 3    unreachable
     * 4    timed out
     *
     * @param code
     */
    public void sendDsco(int code) {
        if (!isConnected() || !helosent)
            return;

        try {
            byte args[] = new byte[1];
            args[0] = (byte) code;

            sendCommand("DSCO", args);
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendDsco", e);
        }
    }

    /**
     * Sent stream headers
     */
    public void sendBody(String body) {
        if (!isConnected() || !helosent)
            return;

        try {
            sendCommand("BODY", body.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendMeta", e);
        }
    }

    /**
     * Sent stream headers
     */
    public void sendResp(String headers) {
        if (!isConnected() || !helosent)
            return;

        try {
            sendCommand("RESP", headers.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendMeta", e);
        }
    }

    /**
     * Sent stream meta-data
     */
    public void sendMeta(String metadata) {
        if (!isConnected() || !helosent)
            return;

        try {
            sendCommand("META", metadata.getBytes());
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendMeta", e);
        }
    }

    /**
     * Send a bye command.
     */
    public void sendBye() {
        if (!isConnected() || !helosent)
            return;

        try {
            sendCommand("BYE!", new byte[1]);
            tcpSocket.close();
            tcpSocket = null;
        } catch (IOException e) {
            Log.d(TAG, "Exception in sendBye", e);
        }
    }

    /**
     * Send a command to slim server
     */
    private void sendCommand(String cmd, byte[] args) throws IOException {
        int len = args.length;
        byte buf[] = new byte[len + 8];

        System.arraycopy(cmd.getBytes(), 0, buf, 0, 4);
        packN4(buf, 4, len);
        System.arraycopy(args, 0, buf, 8, args.length);

        tcpSocket.write(buf);

        Log.d(TAG, "tcp send: " + cmd + " length=" + len);
    }

    private void socketConnected(TcpSocket socket) {
        if (tcpSocket != socket)
            return;

        Log.d(TAG, "command socket connected");
        for (Iterator i=connectionListeners.iterator(); i.hasNext(); ) {
            ProtocolListener p = (ProtocolListener) i.next();
            p.slimprotoConnected();
        }
    }

    private void socketDisconnected(TcpSocket socket) {
        if (tcpSocket != socket)
            return;

        Log.d(TAG, "command socket disconnected");
        for (Iterator i=connectionListeners.iterator(); i.hasNext(); ) {
            ProtocolListener p = (ProtocolListener) i.next();
            p.slimprotoDisconnected();
        }
    }

    private void socketCommand(byte buf[], int offset, int len) {
        String cmd = new String(buf, offset, 4);
        offset += 4;

        Log.d(TAG, "tcp recv: " + cmd + " length=" + len);

        ArrayList l = (ArrayList)commandListeners.get(cmd);
        if (l == null)
            return;

//        for (Iterator j=l.iterator(); j.hasNext(); ) {
//            ProtocolListener p = (ProtocolListener) j.next();
//            p.slimprotoCmd(cmd, buf, offset, len);
//        }
    }

    /*
     *  Thread for managing reads and writes to SqueezeServer
     */
    private class TcpSocket extends Thread {
        private InetAddress address;
        private int port;
        private Socket socket;
        private boolean isConnected = false;
        private boolean closeSocket = false;

        public boolean isConnected() {
            return isConnected;
        }

        public boolean isCloseSocket() {
            return closeSocket;
        }

        TcpSocket(InetAddress addr, int port) {
            this.address = addr;
            this.port = port;
        }

        void close() throws IOException {
            closeSocket = true;
            if( socket != null )
                socket.close();
        }

        void write( byte buff[] ) throws IOException {
            OutputStream stream = socket.getOutputStream();
            stream.write(buff);
        }

        int blockingRead(InputStream stream, byte[] buf, int offset, int len) throws IOException {
            int totalRead = 0;

            while(totalRead < len) {
                int result = stream.read(buf, offset+totalRead, len-totalRead);
                if(result < 0) {
                    return result;
                }
                totalRead += result;
            }
            return totalRead;
        }

        public synchronized void run() {
            byte buf[] = new byte[READ_WRITE_BUFFER];
            int result;
            int len;

            Log.d(TAG, "Starting TCP Socket");

            while (!closeSocket) {
                while (!isConnected) {
                    Log.d(TAG, "Connecting TCP Socket to "+ address +":"+ port);
                    try {
                        socket= new Socket(address, port);
                        socket.setTcpNoDelay(true);

                        isConnected = true;
                        helosent = false;
                    } catch (IOException e){
                        Log.e(TAG,"Cannot connect to " + address +":"+ port);
                        Log.e(TAG, "Error Message: " + e.getMessage());
                        e.printStackTrace();
                    }

                    if(!isConnected) {
                        try {
                            Thread.sleep(5000);
                            Log.d(TAG, "Sleeping before retry");
                        } catch (InterruptedException e) {
                            Log.e(TAG, "error: " + e.getMessage());
                        }
                    }
                }

                socketConnected(this);

                while (true) {
                    try {
                        InputStream stream = socket.getInputStream();

                        result = blockingRead(stream, buf, 0, 2);
                        if (result < 0) {
                            Log.d(TAG, "end of stream detected reading header");
                            break; // end of stream
                        }
                        len = unpackN2(buf, 0);
                        result = blockingRead(stream, buf, 0, len);
                        if (result < 0 || result != len) {
                            Log.d(TAG, "end of stream detected reading frame");
                            break; // end of stream
                        }

                        socketCommand(buf, 0, len);
                    } catch (IOException e) {
                        Log.d(TAG, "ioexception reading from slimproto", e);
                        break; // end of stream
                    } catch (Exception e) {
                        Log.e(TAG, "Exception processing frame ", e);
                    }

                }
            }
        }
    }

    public static int unpackN2(byte[] buf, int pos) {
        return ((buf[pos++] & 0xFF) << 8) | (buf[pos] & 0xFF);
    }

    public static int unpackN4(byte[] buf, int pos) {
        return ((buf[pos++] & 0xFF) << 24) | ((buf[pos++] & 0xFF) << 16)
                | ((buf[pos++] & 0xFF) << 8) | (buf[pos] & 0xFF);
    }

    public static float unpackFixedPoint(byte[] buf, int pos) {
        int v = unpackN4(buf, pos);
        return ((v & 0xFFFF0000) >> 16) + ((v & 0xFFFF) / (float)0xFFFF);
    }

    public static void packN2(byte[] buf, int pos, int arg) {
        buf[pos++] = (byte) ((arg >> 8) & 0xFF);
        buf[pos] = (byte) ((arg >> 0) & 0xFF);
    }

    public static void packN4(byte[] buf, int pos, int arg) {
        buf[pos++] = (byte) ((arg >> 24) & 0xFF);
        buf[pos++] = (byte) ((arg >> 16) & 0xFF);
        buf[pos++] = (byte) ((arg >> 8) & 0xFF);
        buf[pos] = (byte) ((arg >> 0) & 0xFF);
    }

    public static void packN8(byte[] buf, int pos, long arg) {
        buf[pos++] = (byte) ((arg >> 56) & 0xFF);
        buf[pos++] = (byte) ((arg >> 48) & 0xFF);
        buf[pos++] = (byte) ((arg >> 40) & 0xFF);
        buf[pos++] = (byte) ((arg >> 32) & 0xFF);
        buf[pos++] = (byte) ((arg >> 24) & 0xFF);
        buf[pos++] = (byte) ((arg >> 16) & 0xFF);
        buf[pos++] = (byte) ((arg >> 8) & 0xFF);
        buf[pos++] = (byte) ((arg >> 0) & 0xFF);
    }

    public long getEpoch() { return epoch;}

}
