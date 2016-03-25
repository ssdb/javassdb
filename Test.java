import com.udpwork.ssdb.*;

/**
 * SSDB Java client SDK demo.
 */
public class Test {
	public static void main(String[] args) throws Exception {
		String s = "3\r\nget\n1\r\nk\r\n\r\n";
		s += s;
		s += s;
		byte[] bytes = s.getBytes();
		Link link = new Link();
		link.testRead(bytes);
		for(int i=0; i<bytes.length; i++){
			byte[] bs = {bytes[i]};
			link.testRead(bs);
		}
	}
}
