package client;

import java.io.*; 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths; 

public class client {

	public static String currentDir() {
		return System.getProperty("user.dir");
	}

	// FIX PATH IN ALL FILES

	public static void main(String argv[]) throws Exception{
		Socket clientSocket = new Socket("127.0.0.1", 6789);
		OutputStream os = clientSocket.getOutputStream();
		String clientFiles = "\\clientFiles\\";


		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = 
				new DataOutputStream(os); 

		int receivedFileSize = 0;
		String receivedFileName = "";
		String fileType = "B";

		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); 
		String outgoingMessage;
		String r;

		String fileToSend = "";

		r = inFromServer.readLine();
		System.out.println(r+"\n");

		while(true) {
			outgoingMessage = inFromUser.readLine();

			if(outgoingMessage.contains("TYPE")) {
				outToServer.writeBytes(outgoingMessage + '\n');
				outToServer.flush();
				fileType = outgoingMessage.substring(5);
			}

			else if(outgoingMessage.contains("SEND")) {
				outToServer.writeBytes(outgoingMessage + '\n');
				outToServer.flush();

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
						r = inFromServer.readLine();
						System.out.println(r);
					}
				}catch (SocketTimeoutException e) {
					// Stop socket timeout immediately
					clientSocket.setSoTimeout(0);
					System.out.println("Could not receive file");
				}catch(Exception e) {
					clientSocket.setSoTimeout(0);
				}
			}

			else if(outgoingMessage.contains("STOR")) {
				fileToSend = outgoingMessage.substring(9);
				outToServer.writeBytes(outgoingMessage + '\n');
				outToServer.flush();
			}

			else if(outgoingMessage.contains("SIZE")) {
				String locationOfFile = currentDir() + clientFiles + fileToSend; 

				File file = new File(locationOfFile);		

				outgoingMessage = "SIZE " + Long.toString(file.length());
				outToServer.writeBytes(outgoingMessage + '\n');
				outToServer.flush();

				r = inFromServer.readLine();
				System.out.println(r);

				try {
					byte[] content = Files.readAllBytes(file.toPath());
					os.write(content);

				} catch(IOException e) {
				}	
			}else {	
				outToServer.writeBytes(outgoingMessage + '\n');
				outToServer.flush();
			}

			r = inFromServer.readLine();
			System.out.println(r);

			while(inFromServer.ready()) {
				r = inFromServer.readLine();
				System.out.println(r);
			}


			if(outgoingMessage.contains("RETR")) {
				try {
					receivedFileName = outgoingMessage.substring(5);				
					receivedFileSize = Integer.parseInt(r);
					long totalFreeSpace =  new File("c:").getFreeSpace() ;
					System.out.println("total free space" + totalFreeSpace);
					if(totalFreeSpace < receivedFileSize) {
						outgoingMessage = "STOP";
						outToServer.writeBytes(outgoingMessage + '\n');
						outToServer.flush();
					}
				}
				catch (Exception e) {

				}
			}
		}
	}
}
