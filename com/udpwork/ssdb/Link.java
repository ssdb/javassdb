package com.udpwork.ssdb;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Link {
	private Socket sock;
	private MemoryStream input = new MemoryStream();
	
	public Link(){
		
	}

	public Link(String host, int port) throws Exception{
		this(host, port, 0);
	}

	public Link(String host, int port, int timeout_ms) throws Exception{
		sock = new Socket(host, port);
		if(timeout_ms > 0){
			sock.setSoTimeout(timeout_ms);
		}
		sock.setTcpNoDelay(true);
	}
	
	public void close(){
		try{
			sock.close();
		}catch(Exception e){
			//
		}
	}

	public Response request(String cmd, byte[]...params) throws Exception{
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for(byte[] s : params){
			list.add(s);
		}
		return this.request(cmd, list);
	}
	
	public Response request(String cmd, String...params) throws Exception{
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		for(String s : params){
			list.add(s.getBytes());
		}
		return this.request(cmd, list);
	}

	public Response request(String cmd, List<byte[]> params) throws Exception{
		MemoryStream buf = new MemoryStream(4096);
		Integer len = cmd.length();
		buf.write(len.toString());
		buf.write('\n');
		buf.write(cmd);
		buf.write('\n');
		for(byte[] bs : params){
			len = bs.length;
			buf.write(len.toString());
			buf.write('\n');
			buf.write(bs);
			buf.write('\n');
		}
		buf.write('\n');
		send(buf);
		
		List<byte[]> list = recv();
		return new Response(list);
	}
	
	private void send(MemoryStream buf) throws Exception{
		OutputStream os = sock.getOutputStream();
		os.write(buf.toArray());
		os.flush();
	}
	
	private List<byte[]> recv() throws Exception{
		input.nice();
		InputStream is = sock.getInputStream();
		while(true){
			List<byte[]> ret = parse();
			if(ret != null){
				return ret;
			}
			byte[] bs = new byte[8192];
			int len = is.read(bs);
			//System.out.println("<< " + (new MemoryStream(bs, 0, len)).printable());
			input.write(bs, 0, len);
		}
	}
	
	public void testRead(byte[] data) throws Exception{
		input.write(data, 0, data.length);
		System.out.println("<< " +input.repr());
		
		List<byte[]> ret = parse();
		if(ret != null){
			System.out.println("---------------------");
			for (byte[] bs : ret) {
				System.out.println(String.format("%-15s", MemoryStream.repr(bs)));
			}
		}
	}
	
	private List<byte[]> parse() throws Exception{
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		
		int idx = 0;
		// ignore leading empty lines
		while(idx < input.size && (input.chatAt(idx) == '\r' || input.chatAt(idx) == '\n')){
			idx ++;
		}
		
		while(idx < input.size){
			int data_idx = input.memchr('\n', idx);
			if(data_idx == -1){
				break;
			}
			data_idx += 1;
			
			int head_len = data_idx - idx;
			if(head_len == 1 || (head_len == 2 && input.chatAt(idx) == '\r')){
				input.decr(data_idx);
				return list;
			}
			String str = new String(input.copyOfRange(idx, data_idx));
			str = str.trim();
			int size;
			try{
				size = Integer.parseInt(str, 10);
			}catch(Exception e){
				throw new Exception("Parse body_len error");
			}
			
			idx = data_idx + size;

			int left = input.size - idx;
			if(left >= 1 && input.chatAt(idx) == '\n'){
				idx += 1;
			}else if(left >= 2 && input.chatAt(idx) == '\r' && input.chatAt(idx+1) == '\n'){
				idx += 2;
			}else if(left >= 2){
				throw new Exception("bad format");
			}else{
				break;
			}
			// System.out.println("size: " + size + " idx: " + idx + " left: " + (input.size - idx));
			
			byte[] data = input.copyOfRange(data_idx, data_idx + size);
			//System.out.println("size: " + size + " data: " + data.length);
			list.add(data);
		}
		return null;		
	}

}
