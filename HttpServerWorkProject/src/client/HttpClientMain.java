package client;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpClientMain {

	public static void main(String args[]) throws Exception{
		new HttpClientMain().exec();
	}
	
	private void exec() throws Exception{
		
		// 普通に、TCPソケットを張りに行く
		Socket sock = new Socket("www.google.co.jp", 80);		
		
		InputStream is = sock.getInputStream();
		OutputStream os = sock.getOutputStream();
		
		/*
		 * 送信 : クライアントヘッダ
		 */
		String header = "";
		header += "GET HTTP/1.1";
		header += "\r\n\r\n";
		
		byte headerB[] = header.getBytes("Shift_JIS");
		for(int i = 0; i < headerB.length; i++)
			os.write(headerB[i]);
		
		/*
		 * 受信 : レスポンスヘッダ
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
			System.out.print((char) buf[j] + " ");
			if((j+1)%16==0) System.out.println();
		}
		
		/*
		 * 処理 : コンテンツを取得
		 */
		
		// まずコンテンツの長さを知る
		// Content-Length で始まる部分を探す 
		byte contentLength[] = "Content-Length".getBytes("Shift_JIS");
		int startPoint = 0;
		for(int i = contentLength.length; i  < buf.length; i++){
			
			boolean discovered = true;
			
			for(int j = -contentLength.length; j < 0; j++)
				if(buf[i+j] != contentLength[j+contentLength.length])
					discovered = false;
			
			if(discovered){
				startPoint = i;
				break;
			}
		}
				
		String tmp = "";
		for(int i = 0; i < 4; i++)
			tmp += (char) buf[i + 1 + startPoint];
		
		int len = Integer.parseInt(tmp.trim());
		
		// この長さのバッファ用意
		byte contentsB[] = new byte[len];
		
		/*
		 * 受信 : コンテンツ
		 */
		for(int i = 0; i < len; i++)
			contentsB[i] = (byte) is.read();
		
		System.out.println("byte のまま出力");
		for(int j = 0; j < contentsB.length; j++){
			System.out.print(contentsB[j] + " ");
			if((j+1)%16==0) System.out.println();
		}
		
		System.out.println("char にして出力");
		for(int j = 0; j < contentsB.length; j++){
			System.out.print((char) contentsB[j] + " ");
			if((j+1)%16==0) System.out.println();
		}
		
		System.out.println();
		System.out.println("String にキャストして出力");
		String contentsStr = new String(contentsB, "Shift_JIS");
		System.out.println(contentsStr);
		
		sock.close();
		
		
	}
	
}
