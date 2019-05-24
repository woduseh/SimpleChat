import java.net.*;
import java.io.*;

public class ChatClient {
	/* 우선 금지어를 string array로 만듬. 그 후  입력한 문장에 금지어가 포함되었는지 체크하는 boolean is_Ban을 만듬
	 * for문을 통해서 입력한 문장에 금지어가  포함되어있는지 체크
	 * 금지어가 포함되어있다면 어떤 금지어가 포함되어있는지 사용자에게 알려주고 메시지를 보내지 않음
	 * 금지어가 포함되어있지 않다면 정상적으로 실행함  */
	static String[] Banwords = {"학점", "성적", "안드로이드", "팀플", "조별과제"};
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
						System.out.println("'" + Ban + "'" + "은 금지어입니다!");
						is_Ban = true;
					}
				} // 입력한 문장에 금지어가 포함되었는지 체크
				if (is_Ban != true) {
					pw.println(line);
					pw.flush();
					if(line.equals("/quit")){
						endflag = true;
						break;
					}
				} // 금지어가 포함되어있지 않을 경우 정상적으로 출력 진행
				else {
					is_Ban = false;
				} // 금지어가 포함되어있다면 출력을 진행하지 않고 다시 금지어 체크 변수를 false로 변경
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
