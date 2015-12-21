package com.georgegalt.tinysqueezeplayer;

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

    public final static int DISCONNECTED = 0;
    public final static int CONNECTED = 1;
    public final static int BUFFERING = 2;
    public final static int PLAYING = 3;
    public final static int PAUSED = 4;
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

    @Override
    public void slimprotoCmd(String cmd, byte[] buf, int off, int len) {

    }

    @Override
    public void slimprotoConnected() {

    }

    @Override
    public void slimprotoDisconnected() {

    }
}
