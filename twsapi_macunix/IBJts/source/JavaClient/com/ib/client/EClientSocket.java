/* Copyright (C) 2024 Interactive Brokers LLC. All rights reserved. This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package com.ib.client;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.net.Socket;

public class EClientSocket extends EClient implements EClientMsgSink  {

	protected int m_defaultPort;
    protected DataInputStream m_dis;
	private boolean m_asyncEConnect = false;
	private boolean m_connected = false;
	private Socket m_socket;
		
	public void setAsyncEConnect(boolean asyncEConnect) {
		this.m_asyncEConnect = asyncEConnect;
	}

	public boolean isAsyncEConnect() {
		return m_asyncEConnect;
	}

	public EClientSocket(EWrapper eWrapper, EReaderSignal signal) {
		super(eWrapper, signal);
	}

	@Override
	protected Builder prepareBuffer() {
        Builder buf = new Builder( 1024 );
        if( m_useV100Plus ) {
            buf.allocateLengthHeader();
        }
        return buf;
    }

	@Override
	protected void closeAndSend(Builder buf) throws IOException {
    	if( m_useV100Plus ) {
    		buf.updateLength( 0 ); // New buffer means length header position is always zero
    	}
    	
    	EMessage msg = new EMessage(buf);
    	
    	sendMsg(msg);
    }

	protected synchronized void eConnect(Socket socket) throws IOException {
	    // create io streams
	    m_socketTransport = new ESocket(socket);
	    m_dis = new DataInputStream(socket.getInputStream());
	    m_defaultPort = socket.getPort();
	    m_socket = socket;
	
	    sendConnectRequest();
	
	    // start reader thread
	    EReader reader = new EReader(this, m_signal);
	
	    if (!m_asyncEConnect) {
	    	if (!reader.putMessageToQueue())
	    		return;

	    	while (m_serverVersion == 0) {
	    		m_signal.waitForSignal();
	    		reader.processMsgs();
	    	}       
	    }
	}

	public synchronized void eConnect(Socket socket, int clientId) throws IOException {
	    m_clientId = clientId;
	    eConnect(socket);
	}

	public synchronized void eConnect( String host, int port, int clientId) {
	    eConnect(host, port, clientId, false);
	}

	public synchronized void eConnect( String host, int port, int clientId, boolean extraAuth) {
        try {
            validateInvalidSymbols(host);
        }
        catch(EClientException e) {
            error(EClientErrors.NO_VALID_ID, e.error(), e.text());
            return;
        }

	    // already connected?
	    m_host = checkConnected(host);
	
	    m_clientId = clientId;
	    m_extraAuth = extraAuth;

	    if(m_host == null){
	        return;
	    }

		Socket socket;
		try {
			socket = new Socket(m_host, port);
		} catch (IOException e) {
			eDisconnect();
			m_eWrapper.error(EClientErrors.NO_VALID_ID, Util.currentTimeMillis(), EClientErrors.FAIL_CREATE_SOCK.code(),
					EClientErrors.FAIL_CREATE_SOCK.msg(), null);
			return;
		}

        try{
	        eConnect(socket);
	    }
	    catch( Exception e) {
	    	eDisconnect();
	        connectionError();
	    }
	}

	@Override
	public synchronized void serverVersion(int version, String time) {
		m_serverVersion = version;
		m_TwsTime = time;	
		
		if( m_useV100Plus && (m_serverVersion < MIN_VERSION || m_serverVersion > MAX_VERSION) ) {
			eDisconnect();
			m_eWrapper.error(EClientErrors.NO_VALID_ID, Util.currentTimeMillis(), EClientErrors.UNSUPPORTED_VERSION.code(), EClientErrors.UNSUPPORTED_VERSION.msg(), null);
			return;
		}
		
	    if( m_serverVersion < MIN_SERVER_VER_SUPPORTED) {
	    	eDisconnect();
	        m_eWrapper.error( EClientErrors.NO_VALID_ID, Util.currentTimeMillis(), EClientErrors.UPDATE_TWS.code(), EClientErrors.UPDATE_TWS.msg(), null);
	        return;
	    }

	    if ( m_serverVersion < MIN_SERVER_VER_LINKING) {
			try {
				send( m_clientId);
			} catch (IOException e) {
				m_eWrapper.error(e);
			}
		}
	    
	    
	    // set connected flag
	    m_connected = true;       

	    if (!m_asyncEConnect)
	    	startAPI();
	}

	@Override
    public synchronized void eDisconnect() {
	    eDisconnect( true );
	}

	private synchronized void eDisconnect( boolean resetState ) {
	    // not connected?
	    if( m_dis == null && m_socketTransport == null) {
	        return;
	    }
	
	    if ( resetState ) {
	    	m_connected = false;
	        m_extraAuth = false;
	        m_clientId = -1;
	        m_serverVersion = 0;
	        m_TwsTime = "";
	    }
	
	    FilterInputStream dis = m_dis;
	    m_dis = null;
	    if (m_socketTransport != null) {
			try {
				m_socketTransport.close();
			} catch (IOException ignored) {
			} finally {
				m_socketTransport = null;
			}
		}
	
	    try {
	        if (dis != null)
	        	dis.close();
	    } catch (Exception ignored) {
	    }
	    // Notify client: connection closed
	    m_eWrapper.connectionClosed();
	}

	public int read(byte[] buf, int off, int len) throws IOException {
        if (m_dis == null) {
            throw new EClientException(EClientErrors.FAIL_READ_MESSAGE, "");
        }
		return m_dis.read(buf, off, len);
	}

	public int readInt() throws IOException {
        if (m_dis == null) {
            throw new EClientException(EClientErrors.FAIL_READ_MESSAGE, "");
        }
		return m_dis.readInt();
	}

	@Override
	public synchronized boolean isConnected() {
		return m_socket != null && m_socket.isConnected() && m_connected;
	}
}
