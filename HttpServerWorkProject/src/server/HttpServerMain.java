package server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServerMain {

	public static void main(String args[]) throws Exception{
		new HttpServerMain().exec();
	}

	private void exec() throws Exception{

		// 普通に、TCPソケットを待ち受ける
		ServerSocket servSock = new ServerSocket();
		servSock.setReuseAddress(true);
		servSock.bind(new InetSocketAddress(80));

		while(true){

			Socket clntSock = servSock.accept();
			InputStream is = clntSock.getInputStream();
			OutputStream os = clntSock.getOutputStream();

			/*
			 * 受信 : クライアントヘッダ
			 */

			// ヘッダの終了コードは CR/LF/CR/LF/
			byte endCd[] = {13, 10, 13, 10};

			// 512 というサイズは適当
			byte buf[] = new byte[512];

			for(int i = 0; ; i++){

				buf[i] = (byte) is.read();

				if(i < 3) continue;

				// 終了コードか検証
				boolean endFlg = true;

				// ここ最近の4文字が endCd と一致するなら、ヘッダ終了を示す
				for(int j = -3; j <= 0; j++)
					if(buf[i+j] != endCd[j+3])
						endFlg = false;

				// 終了なら抜ける
				if(endFlg) break;

			}

			System.out.println("byte のまま出力");
			for(int j = 0; j < buf.length; j++){
				System.out.print(buf[j] + " ");
				if((j+1)%16==0) System.out.println();
			}

			System.out.println("char にして出力");
			for(int j = 0; j < buf.length; j++){
				System.out.print((char)buf[j] +" ");
				if((j+1)%16==0) System.out.println();
			}

			/*
			 * 処理 : 適当な文字列
			 */

			String contents = "";
			contents += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">";
			contents += "\r\n";
			contents += "<html lang=\"ja-JP\"> <head>";
			contents += "\r\n";
			contents += "<meta htt-equiv=\"Content-Type\" content=\"text/html; charset=shift_jis\">";
			contents += "\r\n";
			contents += "<title>ここがタイトル</title>";
			contents += "\r\n";
			contents += "</head>";
			contents += "\r\n";
			contents += "<body>";
			contents += "\r\n";
			contents += "<h1>稼動しています。</h1>";
			contents += "\r\n";
			contents += "</body> </html>";

			/*
			 * 送信 : レスポンスヘッダ
			 */

			// 文字列をbyteで取得
			byte contentsB[] = contents.getBytes("Shift_JIS");

			String response = "";
			response += ("HTTP/1.1 200 OK\r\n");
			response += ("Connection: close\r\n");
			response += ("Content-Length: ");
			response += (contentsB.length);
			response += ("\r\n");
			response += ("Content-Type: ");
			response += ("text/html");
			response += ("\r\n\r\n");

			// ヘッダ送信
			byte responseB[] = response.getBytes("Shift_JIS");
			for(int i = 0; i < responseB.length; i++)
				os.write(responseB[i]);

			// この後クライアントは contentsB.length 分のメッセージが来るまで待機状態となる
			for(int i = 0; i < contentsB.length; i++)
				os.write(contentsB[i]);

			// やりとり終了
			clntSock.close();
		}

	}

}
