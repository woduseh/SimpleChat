import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			@SuppressWarnings("resource") // Server�� ���������� �����Ű�� �б⸦ ����� ��� �ӽ÷� ��� ������
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
	private HashMap<String, PrintWriter> hm; // HashMap�� ChatThread���� ����� ��� ��(main)�� ���� hm�� �����´�..!
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
		// ���� broadcast �Ϸ��� pw�� �ڽ��� pw�� �������� üũ. �������� ���� ��쿡�� ä�� ������ ����ϵ��� ����.
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
		/* �켱 hm���� ��� ������ id�� hm.keyset�� ���� �޾ƿͼ� set�� �����ϰ� set�� ���� ������ id�� �������� ���� set.iterator�� ������
		 * �� �� �ڱ� �ڽ��� pw�� hm.get(id)�� ���� ������ ���� iterator�� next�� ���� ������ user�� ��ȣ�� ���̵� ���
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
