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
package com.nfl.kfb.tcp;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.prefixedstring.PrefixedStringCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An TCP server used for performance tests.
 * 
 * It does nothing fancy, except receiving the messages, and counting the number of
 * received messages.
 * 
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class TcpServer extends IoHandlerAdapter {
    private static final String METHOD_MAP = "methodMap";

	/** The listening port (check that it's not already in use) */
    public static final int PORT = 18567;

    /** The number of message to receive */
    public static final int MAX_RECEIVED = 100000;

    /** The starting point, set when we receive the first message */
    private static long t0;

    /** A counter incremented for every recieved message */
    private AtomicInteger nbReceived = new AtomicInteger(0);

    
    public static HashMap<Integer,Method> methodMap = new HashMap<Integer,Method>();
    
    
    
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

    	if(message instanceof byte[])
    	{
    		byte[] msg = (byte[])message;
    		System.out.println("#######"+msg.length);
    		byte[] btTag_arr =  Arrays.copyOfRange(msg, 0, 4);
    		byte[] btPackId_arr =  Arrays.copyOfRange(msg, 4,8);
    		byte[] btCmd_arr =  Arrays.copyOfRange(msg, 8,12);
    		byte[] btData_arr = Arrays.copyOfRange(msg, 12,msg.length);
    		int byteArray2Integer = ByteUtil.byteArray2Integer(btPackId_arr);
//    		Object recvId = session.getAttribute("recvId");
//    		if(recvId == null){
//    			session.setAttribute("recvId", byteArray2Integer);
//    		}
//    		int iPackId = (int)session.getAttribute("recvId");
//    		if(byteArray2Integer <= iPackId){
//    			session.close(true);
//    			return;
//    		}
    		int iCmd = ByteUtil.byteArray2Integer(btCmd_arr);
    		String strData = new String(btData_arr,Charset.forName("UTF-8"));
    		Object methodMap = session.getAttribute(METHOD_MAP);
    		if(methodMap == null){
    			methodMap = new MethodMap(session);
    			session.setAttribute(METHOD_MAP, methodMap);
    		}
    		Method invokeMethod = ((MethodMap) methodMap).getInvokeMethod(iCmd);
    		Object invokeObject = ((MethodMap) methodMap).getInvokeObject(iCmd);
    		ObjectMapper objectMapper = new ObjectMapper();
    		System.out.println("######param"+strData);
    		Object[] params = objectMapper.readValue(strData, Object[].class);
    		System.out.println("params.length:"+params.length);
    		Object obj = invokeMethod.invoke(invokeObject, params);
    		boolean isVoidMethod = invokeMethod.getReturnType().equals(Void.TYPE);
    		System.out.println("getmethodreturntype:"+invokeMethod.getReturnType());
    		//byte[] result = objectMapper.writeValueAsBytes(obj);
    		byte[] result1 = null;
    		if(!isVoidMethod){
    			String result = objectMapper.writeValueAsString(obj);
    			System.out.println("sendMethodRet:"+iCmd+""+result);
    			result1 = result.getBytes("UTF-8");
    		}else{
    			result1 = new byte[0];
    		}
    		
    		byte[] retResult = new byte[4+4+4+result1.length];
    		System.arraycopy(btTag_arr, 0, retResult, 0, 4);
    		System.arraycopy(btPackId_arr, 0, retResult, 4, 4);
    		System.arraycopy(btCmd_arr, 0, retResult, 8, 4);
    		System.arraycopy(result1, 0, retResult, 12, result1.length);
    		session.write(retResult);
    		System.out.println("length:"+retResult.length);
    		
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("Session closed...");
        System.out.println("SessionID: "+session.getId());
        
        // Reinitialize the counter and expose the number of received messages
        System.out.println("Nb message received : " + nbReceived.get());
        nbReceived.set(0);
        
        String appId = (String)session.getAttribute(Arena.APP_ID);
        SessionMgr.getInstance().removeSession(appId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("Session created...");
        System.out.println("SessionID: "+session.getId());
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
        System.out.println("SessionID: "+session.getId());
    }

    /**
     * Create the TCP server
     */
    public TcpServer() throws IOException {
        NioSocketAcceptor acceptor = new NioSocketAcceptor();
        acceptor.setHandler(this);
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("logger", new LoggingFilter());
       // PrefixedStringCodecFactory prefixedStringCodecFactory = new PrefixedStringCodecFactory();
      //  prefixedStringCodecFactory.setDecoderMaxDataLength(10*1024);
       // prefixedStringCodecFactory.setDecoderPrefixLength(4);
       // prefixedStringCodecFactory.setEncoderMaxDataLength(10*1024);
        //prefixedStringCodecFactory.setEncoderPrefixLength(4);
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
    public static void startTcpServer(String[] args) throws IOException {
//    	Gson gson = new Gson();
//    	byte[] buff = new byte[10];
//    	String json = gson.toJson(buff);
//    	System.out.println(json);
//    	byte[] fromJson = gson.fromJson(json,buff.getClass());
//    	
//    	JsonArray js = new JsonArray();
//    	JsonObject jsonObject = new JsonObject();
//    	jsonObject.addProperty("age",10);
//    	jsonObject.addProperty("age1",10);
//    	jsonObject.addProperty("age2",10);
//    	js.add(jsonObject);
//    	System.out.println(js.toString());
//    	String string = js.toString();
//    	JsonParser parser = new JsonParser(); 
//    	JsonArray Jarray = parser.parse(string).getAsJsonArray(); 
//    	for (int i = 0; i < Jarray.size(); i++) {
//			System.out.println(Jarray.get(i).getAsJsonObject().get("age1").getAsInt());
//		}
    	
    	
        new TcpServer();
        //new UdpServer();
    }
    
//    public static void Init(){
//    	TcpServer.methodMap.put(key, value)
//    	
//    }
    
    
    
}



