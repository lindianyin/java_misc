package org.apache.mina.example.tcp.perf;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class PackProtocolCodecFactory implements ProtocolCodecFactory {

	 private ProtocolEncoder encoder;
	 private ProtocolDecoder decoder;
	 
	 
	 
	public PackProtocolCodecFactory() {
		encoder = new PackEncoder();
		decoder = new PackDecoder();
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		return decoder;
	}

}
