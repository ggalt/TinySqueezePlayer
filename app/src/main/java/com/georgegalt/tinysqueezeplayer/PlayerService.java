package com.georgegalt.tinysqueezeplayer;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by ggalt on 12/18/15.
 */
public class PlayerService implements ProtocolListener {
    private static final String TAG = "Player.Java";

    private byte format;
    private int crossfade;
    private int state;
    private boolean autostart;
    private boolean directStream;
    private long statusTime;
    private Object lock = new Object();
    private boolean running = true;
    private int pcmSampleSize;
    private int pcmSampleRate;
    private int pcmChannels;
    private boolean pcmEndian;
    private int autostartThreshold;
    private byte transitionPeriod;
    private byte transitionType;
    private boolean loopSong;
    private float replayGain;
    private int interval;
    private String ipaddr;
    private int port;
    private String httpHeaders;
    private float leftLevel = 0;
    private float rightLevel = 0;
    private String lastMetaData = null;

    priate int firmwareversion = 11;

    public final static int INIT = 0;
    public final static int DISCONNECTED = INIT+1;
    public final static int CONNECTED = INIT+2;
    public final static int BUFFERING = INIT+3;
    public final static int PLAYING = INIT+4;
    public final static int PAUSED = INIT+5;
    private final static int DECODER_BUFFER_SIZE = 1048576;
    private final static int OUTPUT_BUFFER_SIZE = 10*2*44100*4;
    private final static int OUTPUT_BUFFER_THRESHOLD = 1; //882000; // 0.25 secs
    public static final int DEFAULT_BUFFER_SIZE = 128000;

    private MainActivity squeeze;

    PlayerService(MainActivity activity) {
        this.squeeze = activity;
        state = DISCONNECTED;
        squeeze.getSlimProto().addProtocolListener("strm", this);
        squeeze.getSlimProto().addProtocolListener("cont", this);
        squeeze.getSlimProto().addProtocolListener("body", this);
        squeeze.getSlimProto().addProtocolListener("stat", this);
        squeeze.getSlimProto().addProtocolListener("audg", this);
    }
    
    /*
     * Set up audio buffers and status
     */
    private void connect(){
        Log.d(TAG, "Connecting, state = " + state);
        synchronized (lock) {

        }
    }

    private void sendStatus(String status, int timestamp) throws IOException {
        byte crlf = (byte) 0; // debug, not used by server
        byte masInit = format;
        byte masMode = (byte) 1; // debug, not used by server

		/*
         * To get sync working Softsqueeze starts decoding as soon as the stream
         * is connected. The Squeezebox 2 however only starts decoding when the
         * buffer threshold is reached. To allow the 5in5 rule to work for
         * internet radio we must report the total write count while buffering.
         */
        int decoderFullness = 0;
//        if (decoderBuffer == null) {
//            decoderFullness = 0;
//            Log.d(TAG, "buffer null state " + state);
//        }
//        else if (state == BUFFERING) {
//            decoderFullness = (int) decoderBuffer.getWriteCount();
//            Log.d(TAG, "write count " + decoderBuffer.getWriteCount() + " available " + decoderBuffer.available());
//        }
//        else {
//            decoderFullness = decoderBuffer.available();
//            vLog.d(TAG, "write count " + decoderBuffer.getWriteCount() + " available " + decoderBuffer.available());
//        }
//
//        long bytesRx = (decoderBuffer == null) ? 0 : decoderBuffer.getWriteCount();
        long bytesRx = 0;
        byte signal = (byte) 0xFF; // wired squeezebox
//        int outputFullness = outputBuffer.available();
        int outputFullness = 0;
        long elapsedMilliseconds = 0;       // should be amount of time played

        statusTime = System.currentTimeMillis();

            Log.d(TAG, "decode: " + ((decoderBuffer.available() / (float) decoderBuffer.getBufferSize()) * 100.0) + " avail=" + decoderBuffer.available() + " size=" + decoderBuffer.getBufferSize());
            Log.d(TAG, "output: " + ((outputBuffer.available() / (float) outputBuffer.getBufferSize()) * 100.0) + " avail=" + outputBuffer.available() + " size=" + outputBuffer.getBufferSize());
            vLog.d(TAG, "status=" + status + " fullness=" + decoderFullness + " bytesRx=" + bytesRx + " elapsedMilliseconds=" + elapsedMilliseconds);

        squeeze.getProtocol().sendStat(status, crlf, masInit, masMode,
                DECODER_BUFFER_SIZE, decoderFullness, bytesRx, signal,
                OUTPUT_BUFFER_SIZE, outputFullness, elapsedMilliseconds, timestamp);
    }

    @Override
    public void slimprotoCmd(String cmd, byte[] buf, int off, int len) {
        if (cmd.equals("strm")) {
            char scmd = parseStream(buf, off, len);

            try {
                switch (scmd) {

                    case 's': // start
//                        connect();
//                        autostart();
                        break;
                    case 'u': // unpause
//                        start(interval);
                        break;
                    case 'p': // pause
//                        pause(interval);
                        break;
                    case 'q': // quit			        
//                        disconnect();
                        break;
                    case 'f': // flush			        
//                        flush();
                        break;
                    case 't': // status
                        sendStatus("STMt", interval);
                        break;
                    case 'a': // status
//                        skipAhead(interval);
                        break;
                    default:
                        Log.w(TAG, "Unknown strm command " + scmd);
                }
            } catch (IOException e) {
                Log.e(TAG, "strm IO error", e);
            } catch (AudioException e) {
                Log.e(TAG, "strm Audio error", e);
            }
        }
        else if (cmd.equals("stat")) {
            try {
                sendStatus("stat", 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (cmd.equals("cont")) {
            Log.d(TAG, "slimProtoCommand = " + cmd);
//            try {
//                int metaint = SlimProto.unpackN4(buf, off);
//                loopSong = ((buf[off + 4] & 0x01) == 0x01);
//                cont(metaint);
//            } catch (AudioException e) {
//                Log.e(TAG, "cont Audio error", e);
//            }
        }
        else if (cmd.equals("body")) {
            Log.d(TAG, "slimProtoCommand = " + cmd);
//            try {
//                int length = SlimProto.unpackN4(buf, off);
//                body(length);
//            } catch (IOException e) {
//                Log.e(TAG, "cont Audio error", e);
//            }
        }
        else if (cmd.equals("audg")) {
            int oldLeft = SlimProto.unpackN4(buf, off);
            int oldRight = SlimProto.unpackN4(buf, off+4);
            // off+8; digital volume control
            // off+9; preamp
            leftLevel = SlimProto.unpackFixedPoint(buf, off+10);
            rightLevel = SlimProto.unpackFixedPoint(buf, off+14);

            Log.d(TAG, "slimProtoCommand = " + cmd);
            Log.d(TAG, "audg oldLeft=" + oldLeft + " oldRight=" + oldRight + " newLeft=" + leftLevel + " newRight=" + rightLevel);

//            audioMixer.setVolume(leftLevel, rightLevel);
        }
        else if (cmd.equals("visu")) {
            Log.d(TAG, "slimProtoCommand = " + cmd);
//            int frameLen = len - off;
//
//            // Ignore visu frame if this is a repeat of the last frame
//            if (frameLen == visualizerFrame.length) {
//                int i = 0;
//                while (i < visualizerFrame.length) {
//                    if (buf[off+i] != visualizerFrame[i++])
//                        break;
//                }
//                if (frameLen == i)
//                    return;
//            }
//
//            visualizerFrame = new byte[frameLen];
//            System.arraycopy(buf, off, visualizerFrame, 0, frameLen);
//
//            // Change visualizer
//            int visType = buf[off];
//            switch (visType) {
//                case 1:
//                    visualizer = new VisualizerVUMeter(squeeze, buf, off, len);
//                    break;
//                case 2:
//                    visualizer = new VisualizerSpectrumAnalyser(squeeze, buf, off, len);
//                    break;
//                default:
//                    visualizer = null;
//            }
//
//            audioMixer.setVisualizer(visualizer);
        }
        else if (cmd.equals("visg")) {
            Log.d(TAG, "slimProtoCommand = " + cmd);
//            VisualizerVUMeter.uploadGraphic(buf, off, len);
        }
    }

    @Override
    public void slimprotoConnected() {
        Log.d(TAG,"slimprotoConnected");
        // disconnect and clean up if we are currently connected, then
        // set up audio buffers
        if (state == CONNECTED) {
            squeeze.getSlimProto().sendBye();
        }

        byte[] macaddress = parseMacAddress(ServerInfo.getThisPlayerID());

        squeeze.getSlimProto().sendHELO(12, 0, macaddress, false, (state != INIT));
    }

    @Override
    public void slimprotoDisconnected() {

    }

    private char parseStream(byte buf[], int start, int len) {
        char cmd = (char)buf[start];
        byte autostartFlag = buf[start + 1];
        String spdifEnable = new String(buf, start + 8, 1);
        transitionPeriod =   buf[start + 9];
        transitionType   =   buf[start + 10];
        loopSong         =  (buf[start + 11] == '1');
        // buf[start + 12]; reserved

        switch (cmd) {
            case 's':
                format             = buf[start + 2];
                pcmSampleSize      = buf[start + 3] - '0';
                pcmSampleRate      = buf[start + 4] - '0';
                pcmChannels        = buf[start + 5] - '0';
                pcmEndian          = "0".equals(new String(buf, start + 6, 1));
                autostart = (autostartFlag == '1' || autostartFlag == '3');
                directStream = (autostartFlag == '2' || autostartFlag == '3' || autostartFlag == '4');
                autostartThreshold = (buf[start + 7] & 0xFF) * 1024;
                replayGain         = SlimProto.unpackFixedPoint(buf, start + 14);
                if (replayGain == 0.0f)
                    replayGain = 1.0f; // Use 0.00dB with no replay gain
                break;
            case 'p':
            case 'u':
            case 'a':
            case 't':
                interval = SlimProto.unpackN4(buf, start + 14);
                break;
        }

        // reserved
        port = SlimProto.unpackN2(buf, start + 18);
        StringBuffer ipaddrBuf = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            ipaddrBuf.append(Integer.toString( buf[start + 20 + i] & 0xFF));
            if (i < 3)
                ipaddrBuf.append(".");
        }
        ipaddr = ipaddrBuf.toString();
        if (ipaddr.equals("0.0.0.0"))
            ipaddr = Config.getSlimServerAddress();

        httpHeaders = new String(buf, start + 24, len - start - 24);
        Log.d(TAG, "httpRequest=" + httpHeaders);

        Log.d(TAG, "parsed strm: command=" + cmd + " format=" + format
                + " crossfade=" + crossfade + " replygain=" + replayGain
                + " ipaddr=" + ipaddr + " port=" + port
                + " autostart=" + autostart + " autostartThreshold=" + autostartThreshold);

        return cmd;
    }
    /**
     * Parse a string mac address into a byte array.
     */
    public static byte[] parseMacAddress(String mac) {
        byte macAddress[] = new byte[6];

        String hex[] = Util.split(mac, ":-");
        for (int i = 0; i < Math.min(hex.length, macAddress.length); i++) {
            macAddress[i] = (byte) Integer.parseInt(hex[i], 16);
        }
        return macAddress;
    }
}
