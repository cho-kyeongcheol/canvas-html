package aaaa;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class aaa {//서버가 한명의 클라이언트와 통신을 하기위한 기능을 정의
   
   Socket socket;//소켓 라이브러리 임포트
   
   public Client(Socket socket) { 
      this.socket = socket;
      recive();
   }
   //클라이언트로부터 메시지 전달받는 메소드
   public void recive() {
      Runnable thread = new Runnable() { //클라이언트가 접속하면 쓰레드 생성(하나의 쓰레드를 만들때는 Runnable을 사용함)
         @Override
         public void run() { //Runnable 라이브러리에서는 내부적으로 run 함수를 반드시 가져야함.
            try {          //하나의 쓰레드가 어떠한 모듈로 동작을 할건지 run안에서 정의
               while(true) { //반복적으로 클라이언트로부터 어떠한 내용을 전달 받음 (기본적으로 예외가 발생 할 수 있기때문에 try catch문으로 감싸줌)
                  InputStream in = socket.getInputStream(); //내용전달받기위해 InputStream객체 사용
                  byte[] buffer = new byte[512]; //한번에 512바이트만큼 전달 받을 수 있게 buffer사용
                  int length = in.read(buffer); //length로 클라이언트한테 내용 전달받아서 buffer에 담아줌 
                  while(length == -1) throw new IOException(); //메세지를 읽어들일때 오류발생시 예외처리
                  System.out.println("[메세지 수신 성공]"
                        +socket.getRemoteSocketAddress() //현재 접속한 클라이언트의 주소정보출력
                        +": "+ Thread.currentThread().getName()); //쓰레드의 이름값 출력
                  String message = new String(buffer, 0, length, "UTF-8"); //전달받은 값을 한글도 포함할 수 있게 인코딩처리
                  for(Client client : Main.clients) {
                     client.send(message); //전달받은 메세지를 다른 클라이언트들에게도 그대로 전달
                  }
               }
            } catch(Exception e) {//예외처리
               try {
                  System.out.println("[메세지 수신 오류] "
                     +socket.getRemoteSocketAddress()
                     +": " + Thread.currentThread().getName());
               }catch (Exception e2) { //오류발생시 메세지를 보낸 클라이언트의 주소를 출력
                  e2.printStackTrace();//오류문구출력
               }               
            }            
         }
      };
      Main.threadPool.submit(thread); //위에서 만들어진 쓰레드를 threadPool에 등록.
   }
   
   //클라이언트에게 메세지를 전송하는 메소드
   public void send(String message) {
      Runnable thread = new Runnable() {
         @Override 
         public void run() {
            try {
               OutputStream out = socket.getOutputStream();
               byte[] buffer = message.getBytes("UTF-8");
               out.write(buffer);//오류가 발생하지 않았을때 처리(버퍼에 담긴 내용을 클라이언트에 전송)
               out.flush();
            } catch (Exception e) {
               try {
                  System.out.println("[메세지 송신 오류]"
                        +socket.getRemoteSocketAddress()
                        +": " + Thread.currentThread().getName());
                  Main.clients.remove(Client.this); //클라이언트 배열에서 해당 오류가 생긴 클라이언트를 제거(서버가 끊긴 클라이언트정보를 처리)
                  socket.close();//오류 발생 시 생긴 클라이언트의 소켓을 닫음
               } catch (Exception e2) {
                  e2.printStackTrace();
               }
            }
         }
      };
      Main.threadPool.submit(thread);
   }
}