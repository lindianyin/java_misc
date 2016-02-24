/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.mina.example.tcp.perf;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * An TCP server used for performance tests.
 * 
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class TcpServer extends IoHandlerAdapter {
    /** The listening port (check that it's not already in use) */
    public static final int PORT = 18567;

    /** The number of message to receive */
    public static final int MAX_RECEIVED = 100000;

    /** The starting point, set when we receive the first message */
    private static long t0;

    /** A counter incremented for every recieved message */
    private AtomicInteger nbReceived = new AtomicInteger(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
        session.close(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

/*        int nb = nbReceived.incrementAndGet();

        if (nb == 1) {
            t0 = System.currentTimeMillis();
        }

        if (nb == MAX_RECEIVED) {
            long t1 = System.currentTimeMillis();
            System.out.println("-------------> end " + (t1 - t0));
        }

        if (nb % 10000 == 0) {
            System.out.println("Received " + nb + " messages");
        }*/

        //System.out.println("Message : " + ((IoBuffer) message).getInt());

        //((IoBuffer) message).flip();

        // If we want to test the write operation, uncomment this line
        //session.write(message);
    	if(message instanceof byte[])
    	{
    		byte[] msg = (byte[])message;
        	System.out.println(new String(msg));
        	System.out.println(msg.length);
    	}
    	

    	
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");

        // Reinitialize the counter and expose the number of received messages
        System.out.println("Nb message received : " + nbReceived.get());
        nbReceived.set(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Session created...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("Session idle...");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionOpened(IoSession session) throws Exception {
        System.out.println("Session Opened...");
    }

    /**
     * Create the TCP server
     */
    public TcpServer() throws IOException {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(this);
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("logger", new LoggingFilter());
        PrefixedStringCodecFactory prefixedStringCodecFactory = new PrefixedStringCodecFactory();
        prefixedStringCodecFactory.setDecoderMaxDataLength(10*1024);
        prefixedStringCodecFactory.setDecoderPrefixLength(4);
        prefixedStringCodecFactory.setEncoderMaxDataLength(10*1024);
        prefixedStringCodecFactory.setEncoderPrefixLength(4);
        //chain.addLast("protocol", new ProtocolCodecFilter(prefixedStringCodecFactory));
        chain.addLast("protocol", new ProtocolCodecFilter(new PackProtocolCodecFactory()));
        //chain.addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        
        
        
        SocketSessionConfig scfg = acceptor.getSessionConfig();
        acceptor.bind(new InetSocketAddress(PORT));
        
        
        
        System.out.println("Server started...");
    }

    /**
     * The entry point.
     */
    public static void main(String[] args) throws IOException {
    	Gson gson = new Gson();
    	byte[] buff = new byte[10];
    	String json = gson.toJson(buff);
    	System.out.println(json);
    	byte[] fromJson = gson.fromJson(json,buff.getClass());
    	
    	JsonArray js = new JsonArray();
    	JsonObject jsonObject = new JsonObject();
    	jsonObject.addProperty("age",10);
    	jsonObject.addProperty("age1",10);
    	jsonObject.addProperty("age2",10);
    	js.add(jsonObject);
    	System.out.println(js.toString());
    	String string = js.toString();
    	JsonParser parser = new JsonParser(); 
    	JsonArray Jarray = parser.parse(string).getAsJsonArray(); 
    	for (int i = 0; i < Jarray.size(); i++) {
			System.out.println(Jarray.get(i).getAsJsonObject().get("age1").getAsInt());
		}
    	
    	
        new TcpServer();
        //new UdpServer();
    }
}



