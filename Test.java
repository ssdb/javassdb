import com.udpwork.ssdb.*;

/**
 * SSDB Java client SDK demo.
 */
public class Test {
	public static void main(String[] args) throws Exception {
		String k = "k";
		String v = "1";
		int i = 0;
		while (true) {
			SSDB ssdb = null;
			try {           
				ssdb = new SSDB("127.0.0.1", 8888,5000);                       
				ssdb.set(k,v);  //这里的k确保每次都不同，v 平均 1M大小
				ssdb.close();
			} catch (Exception e) {
				if(ssdb != null){
					ssdb.close();
				}           
				continue;
			}finally{
				if(ssdb != null){
					ssdb.close();
				}
			}
			if(ssdb != null){
				ssdb.close();
			}           

			if(++i % 1000 == 1){
				System.out.println("press Enter");
				System.in.read();
			}
		}

	}
}
