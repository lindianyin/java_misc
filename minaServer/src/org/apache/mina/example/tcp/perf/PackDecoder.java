package org.apache.mina.example.tcp.perf;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

public class PackDecoder extends CumulativeProtocolDecoder  {

	@Override
	protected boolean doDecode(IoSession session, IoBuffer in,
			ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		if(in.remaining() >= 4){
			in.mark();
			int len = in.getInt();
			if(len > in.remaining()){
				in.reset();
				return false;
			}
			byte[] buf = new byte[len];
			in.get(buf,0,len);
			out.write(buf);
			return true;
		}else{
			return false;
		}
	}

}
