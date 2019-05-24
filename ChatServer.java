import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			@SuppressWarnings("resource") // Server를 정상적으로 종료시키는 분기를 만드는 대신 임시로 경고를 제거함
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap<String, PrintWriter> hm = new HashMap<String, PrintWriter>();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap<String, PrintWriter> hm; // HashMap을 ChatThread에서 만드는 대신 남(main)이 만든 hm을 가져온다..!
	//private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap<String, PrintWriter> hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			//initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // Constructor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}
				if(line.equals("/userlist")){
					send_userlist();
				}
				else {
					broadcast(id + " : " + line);
				}
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		// 현재 broadcast 하려는 pw가 자신의 pw와 동일한지 체크. 동일하지 않을 경우에만 채팅 문장을 출력하도록 변경.
		synchronized(hm){
			Collection<PrintWriter> collection = hm.values();
			Iterator<PrintWriter> iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if (pw!=hm.get(id)) {
					pw.println(msg);
					pw.flush();
				}
			}
		}
	} // broadcast
	public void send_userlist() {
		/* 우선 hm에서 모든 유저의 id를 hm.keyset을 통해 받아와서 set에 저장하고 set이 끝날 때까지 id를 꺼내오기 위해 set.iterator를 만들어둠
		 * 그 후 자기 자신의 pw를 hm.get(id)를 통해 가져온 다음 iterator에 next가 없을 때까지 user의 번호와 아이디를 출력
		 */
		Set<String> set = hm.keySet();
		Iterator<String> iterator = set.iterator();
		PrintWriter pw = hm.get(id);
		int i = 1;
		while(iterator.hasNext()){
			String username = (String)iterator.next();
			pw.println("User#" + i + ": " + username);
			pw.flush();
			i++;
		}
	} // send_userlist
}
