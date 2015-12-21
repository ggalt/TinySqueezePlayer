package com.georgegalt.tinysqueezeplayer;

/**
 * Created by ggalt on 12/18/15.
 */
public interface ProtocolListener {
    /**
     * Called when a slimproto command is received.
     *
     * @param cmd the slimproto command
     * @param buf command buffer
     * @param off offset into command buffer
     * @param len length of data in the command buffer
     */
    public void slimprotoCmd(String cmd, byte buf[], int off, int len);

    public void slimprotoConnected();

    public void slimprotoDisconnected();
}
