package client;

import java.io.*; 
import java.net.*;
import java.nio.file.Paths; 

public class client {

	public static String currentDir() {
		return System.getProperty("user.dir");
	}

	public static void main(String argv[]) throws Exception{
		Socket clientSocket = new Socket("127.0.0.1", 6789);

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = 
				new DataOutputStream(clientSocket.getOutputStream()); 

		int receivedFileSize = 0;
		String receivedFileName = "";
		String fileType = "B";

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		String outgoingMessage;
		String r;
		r = inFromServer.readLine();
		System.out.println(r+"\n");
		while(true) {
			outgoingMessage = inFromUser.readLine();
			outToServer.writeBytes(outgoingMessage + '\n');
			outToServer.flush();

			if(outgoingMessage.contains("TYPE")) {
				fileType = outgoingMessage.substring(5);
			}

			if(outgoingMessage.contains("SEND")) {
				byte[] bytes = new byte[(int)receivedFileSize]; // Declare byte array with file size
				boolean worked = false;
				boolean binary = false;
				clientSocket.setSoTimeout(5*1000); // Set timeout in case data doesn't come

				DataInputStream in = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

				try {
					for (int i = 0; i < receivedFileSize; i++) {
						bytes[i] = (byte) in.read();

						if ((int)bytes[i] < 0) { // File is binary if MSB is negative
							binary = true;
						}
					}

					// Stop socket timeout immediately
					clientSocket.setSoTimeout(0);

					// Check if file type is correct before storing
					boolean correctType = (!binary && fileType.equals("A")) || fileType.equals("B") || fileType.equals("C");

					// Write file if it is correct
					if (correctType) { //
						FileOutputStream createdFile = new FileOutputStream(currentDir() + "/" + receivedFileName);
						createdFile.write(bytes);
						createdFile.close();	  
					}
				}catch (SocketTimeoutException e) {
					// Stop socket timeout immediately
					clientSocket.setSoTimeout(0);
					System.out.println("Could not receive file");
				}
			}

			r = inFromServer.readLine();
			System.out.println(r);

			while(inFromServer.ready()) {
				r = inFromServer.readLine();
				System.out.println(r);
			}
				


			if(outgoingMessage.contains("RETR")) {
				receivedFileName = outgoingMessage.substring(5);				
				receivedFileSize = Integer.parseInt(r);
				long totalFreeSpace =  new File("c:").getFreeSpace() ;
				System.out.println("total free space" + totalFreeSpace);
				if(totalFreeSpace > receivedFileSize) {
					outgoingMessage = "STOP";
					outToServer.writeBytes(outgoingMessage + '\n');
					outToServer.flush();
				}
			}
		}
	}
}
