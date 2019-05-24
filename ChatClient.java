import java.net.*;
import java.io.*;

public class ChatClient {
	/* �켱 ����� string array�� ����. �� ��  �Է��� ���忡 ����� ���ԵǾ����� üũ�ϴ� boolean is_Ban�� ����
	 * for���� ���ؼ� �Է��� ���忡 �����  ���ԵǾ��ִ��� üũ
	 * ����� ���ԵǾ��ִٸ� � ����� ���ԵǾ��ִ��� ����ڿ��� �˷��ְ� �޽����� ������ ����
	 * ����� ���ԵǾ����� �ʴٸ� ���������� ������  */
	static String[] Banwords = {"����", "����", "�ȵ���̵�", "����", "��������"};
	public static void main(String[] args) {
		if(args.length != 2){
			System.out.println("Usage : java ChatClient <username> <server-ip>");
			System.exit(1);
		}
		Socket sock = null;
		BufferedReader br = null;
		PrintWriter pw = null;
		boolean endflag = false;
		try{
			boolean is_Ban = false; // is_Ban
			sock = new Socket(args[1], 10001);
			pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
			// send username.
			pw.println(args[0]);
			pw.flush();
			InputThread it = new InputThread(sock, br);
			it.start();
			String line = null;
			while((line = keyboard.readLine()) != null){
				for (String Ban: Banwords) {
					if (line.contains(Ban)) {
						System.out.println("'" + Ban + "'" + "�� �������Դϴ�!");
						is_Ban = true;
					}
				} // �Է��� ���忡 ����� ���ԵǾ����� üũ
				if (is_Ban != true) {
					pw.println(line);
					pw.flush();
					if(line.equals("/quit")){
						endflag = true;
						break;
					}
				} // ����� ���ԵǾ����� ���� ��� ���������� ��� ����
				else {
					is_Ban = false;
				} // ����� ���ԵǾ��ִٸ� ����� �������� �ʰ� �ٽ� ������ üũ ������ false�� ����
			}
			System.out.println("Connection closed.");
		}catch(Exception ex){
			if(!endflag)
				System.out.println(ex);
		}finally{
			try{
				if(pw != null)
					pw.close();
			}catch(Exception ex){}
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		} // finally
	} // main
} // class

class InputThread extends Thread{
	private Socket sock = null;
	private BufferedReader br = null;
	public InputThread(Socket sock, BufferedReader br){
		this.sock = sock;
		this.br = br;
	}
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				System.out.println(line);
			}
		}catch(Exception ex){
		}finally{
			try{
				if(br != null)
					br.close();
			}catch(Exception ex){}
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // InputThread
}
