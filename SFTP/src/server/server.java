package server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.awt.*;
import java.awt.event.*;

public class server {

	static DataOutputStream  outToClient;
	static Account account = new Account();


	public static String currentDir() {
		return System.getProperty("user.dir");
	}

	public static void clientPrinter(String message) throws IOException {
		try {
			outToClient.writeBytes(message+"\n");
			outToClient.flush();
		}catch (Exception e) {
			System.out.println("Error");
		}
	}

	private static void handleUser(String fullCommand) throws IOException {
		String user = fullCommand.substring(5) ;
		if(account.validUser(user)) {
			if(account.isLoggedIn(user)) {
				clientPrinter("!"+ user + " logged in");
			}
			else {
				clientPrinter("+User-id valid, send account and password");
			}
		}
		else {
			clientPrinter("-Invalid user-id, try again");
		}
	}

	private static void handleAcct(String fullCommand) throws IOException {
		String accountName = fullCommand.substring(4);

		if(account.isLoggedIn(accountName)) {
			clientPrinter("! Account valid, logged-in");
		}
		else if(account.validAccount(accountName)) {
			clientPrinter("+Account valid, send password");
		}
		else {
			clientPrinter("Invalid account, try again");
		}	
	}

	private static void handlePass(String fullCommand) throws IOException {
		String password = fullCommand.substring(4);
		if(account.alreadyInAccount() && account.validPassword(password)){
			clientPrinter("! Logged in");
		}
		else if(!account.alreadyInAccount() && account.validPassword(password)) {
			clientPrinter("Password ok but you haven't specified the account\"");
		}
		else {
			clientPrinter("-Wrong password, try again");
		}		
	}

	private static void handleType(String fullCommand) throws IOException {
		String type = fullCommand.substring(5);

		if(type.equals("A")) {
			clientPrinter("+Using Ascii mode");
		}
		else if(type.equals("B")) {
			clientPrinter("+Using Binary mode");
		}
		else if(type.equals("C")) {
			clientPrinter("+Using Continuous mode");
		}
		else {
			clientPrinter("-Type not valid");
		}	
	}

	private static void handleList(String fullCommand, MyFiles myFiles, String currentDirectory) throws IOException {
		String format = fullCommand.substring(5,6);
		String dir = currentDirectory + "\\" + fullCommand.substring(6).trim();
		if(format.contentEquals("F")) {
			String toPrint = myFiles.listAllFiles(dir,"F");
			clientPrinter(toPrint);
		}
		else if(format.contentEquals("V")) {
			String toPrint = myFiles.listAllFiles(dir,"V");
			clientPrinter(toPrint);
		}
	}

	private static String handleCDIR(String fullCommand, Account account, String currentDirectory) throws IOException {
		if(account.alreadyInAccount()) {
			String newDir = fullCommand.substring(5);
			String checkNewDir = Paths.get(currentDirectory, newDir).toString();
			Path path = Paths.get(checkNewDir);

			if(newDir.equals("..")) {
				currentDirectory =  new File(System.getProperty("user.dir")).getParentFile().toString();
				clientPrinter("!Changed working dir to "+currentDirectory);
			}
			else if(newDir.equals("/")) {
				currentDirectory = "C:\\";
				clientPrinter("!Changed working dir to "+path);
			}
			else {
				if(Files.exists(path)) {
					currentDirectory = checkNewDir;
					clientPrinter("!Changed working dir to "+path);
				}
				else {
					clientPrinter("-Can't connect to directory because: directory doesn't exist");
				}
			}
		}
		else {
			clientPrinter("-You must send ACCT and PASS to use CDIR");
		}
		return currentDirectory;

	}

	private static void handleKill(String fullCommand, Account account, String currentDirectory) throws IOException {
		if(account.alreadyInAccount()) {
			String fileToDelete = "";
			try {
				fileToDelete = fullCommand.substring(5);
			} catch (Exception e) {
				clientPrinter("-Not deleted because file doesn't exist"); 
			}
			String fileLocation = Paths.get(currentDirectory, fileToDelete).toString();
			File file = new File(fileLocation); 
			if(file.delete()) { 
				clientPrinter("+" + fileToDelete + " deleted"); 
			} 
			else { 
				clientPrinter("-Not deleted because file doesn't exist"); 
			}
		}
		else {
			clientPrinter("-You must send ACCT and PASS to use CDIR");
		}
	}

	private static String handleName(String fullCommand, Account account, String currentDirectory) throws IOException {
		String fileToRename = "";
		if(account.alreadyInAccount()) {
			String tempFileToRename = "";
			try {
				tempFileToRename = fullCommand.substring(5);

				String fileLocation = Paths.get(currentDirectory + "\\" + tempFileToRename).toString();
				Path path = Paths.get(fileLocation);
				File file = new File(fileLocation); 
				if(Files.exists(path)) {
					fileToRename = tempFileToRename;
					clientPrinter("+File exists");
				}
				else {
					clientPrinter("-Can't find "+ fileToRename +"\n NAME command is aborted, don't send TOBE.");
				}
			}catch (Exception e) {
				clientPrinter("-Can't find "+ fileToRename +"\n NAME command is aborted, don't send TOBE.");
			}
		}
		else {
			clientPrinter("-You must send ACCT and PASS to use CDIR");
		}
		return fileToRename;
	}

	private static void handleToBe(String fullCommand, String currentDirectory, String fileToRename) throws IOException {
		try {
			String newFileName = fullCommand.substring(5);
			if(fileToRename.equals("")) {
				clientPrinter("-File wasn't renamed because filename was not specified or was invalid");
			}
			else {
				String fileLocation = Paths.get(currentDirectory, fileToRename).toString();
				String newName = Paths.get(currentDirectory, newFileName).toString();
				File file = new File(fileLocation);
				File fileRenameTo = new File(newName);
				file.renameTo(fileRenameTo);
				newFileName = "";
			}
		} catch(Exception e) {
			clientPrinter("-File wasn't renamed because filename was not specified or was invalid");
		}
	}

	private static String handleRETR(String fullCommand, String currentDirectory, String fileToSendLocation) throws IOException {
		try {
			String fileName = fullCommand.substring(5);
			String fileLocation = Paths.get(currentDirectory, fileName).toString();
			File file = new File(fileLocation);
			Path path = Paths.get(fileLocation);
			if(Files.exists(path)) {
				fileToSendLocation = fileLocation;
				long fileSize = file.length() ;
				clientPrinter(String.valueOf(fileSize));
			}
			else {
				clientPrinter("-File doesn't exist");
			}
			return fileToSendLocation;
		} catch(Exception e) {
			clientPrinter("-File doesn't exist");
		}
		return null;
	}

	private static void handleSend(OutputStream os, String fileToSendLocation) throws IOException {
		File fileToSend = new File(fileToSendLocation);							  
		try {
			byte[] content = Files.readAllBytes(fileToSend.toPath());
			os.write(content);
			clientPrinter("File Saved on Client's side");
		} catch(IOException e) {
			clientPrinter("File Could not be saved");
		}	
	}

	private static int handleSTOR(String fullCommand, String serverFiles) throws IOException {
		String param = fullCommand.substring(5,8);
		String filename = fullCommand.substring(9);
		String locationOfFile = currentDir() + serverFiles + filename; 
		int storeType = 0;

		File file = new File(locationOfFile);

		if(param.equals("NEW")) {
			if(file.exists()) {
				storeType = 2;
				clientPrinter("+File exists, will create new generation of file");
			}
			else {
				storeType = 0 ;
				clientPrinter("+File does not exist, will create new file");
			}
		}
		else if(param.contentEquals("OLD")) {
			if(file.exists()) {
				storeType = 3;
				clientPrinter("+Will write over old file");
			}
			else {
				storeType = 1;
				clientPrinter("+Will create new file");
			}
		}
		else if(param.contentEquals("APP")) {
			if(file.exists()) {
				storeType = 0;
				clientPrinter("+Will append to file");
			}
			else {
				storeType = 1;
				clientPrinter("+Will create file");
			}
		}
		return storeType;
	}

	private static void handleSize(String fullCommand, Socket connectionSocket, int storeType, String serverFiles, String fileNameToStore) throws IOException {
		String sizeOfFileString = fullCommand.substring(5);
		long sizeOfFile = Integer.parseInt(sizeOfFileString);

		long totalFreeSpace =  new File("c:").getFreeSpace() ;

		if(totalFreeSpace < sizeOfFile) {
			clientPrinter("-Not enough room, don't send it");
		}
		else {
			clientPrinter("+ok, waiting for file");
			byte[] receivedFile = new byte[(int) sizeOfFile];
			for (int i=0; i<sizeOfFile; i++) {
				receivedFile[i] = (byte) connectionSocket.getInputStream().read();
			}
			try {
				if ((storeType == 1) || (storeType == 3)) {
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + fileNameToStore);
					stream.write(receivedFile);
					stream.close();
				} else if (storeType == 2) {
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + "new-" + fileNameToStore);
					stream.write(receivedFile);
					stream.close();
				} else {
					FileOutputStream stream = new FileOutputStream(currentDir() + serverFiles + fileNameToStore, true);
					stream.write(receivedFile);
					stream.close();
				}
				clientPrinter("+Saved fileNameToStore");
			} catch (Exception e) {
				storeType = 0;
				clientPrinter("-Couldn't save");
			}
		}
	}


	public static void main(String argv[]) throws Exception{
		try {
			ServerSocket welcomeSocket = new ServerSocket(6789);

			MyFiles myFiles = new MyFiles();
			String serverFiles = "\\files\\";
			String currentDirectory = currentDir();
			String fileToRename = "";
			String fileToSendLocation = "";
			String fullcommand = "";
			String command = "";
			int storeType = 0;
			String fileNameToStore = "";

			boolean running = true;

			while(true) {
				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("Connected");

				OutputStream os = connectionSocket.getOutputStream();
				BufferedReader inFromClient = 
						new BufferedReader(new
								InputStreamReader(connectionSocket.getInputStream())); 
				outToClient = 
						new DataOutputStream(os); 

				clientPrinter("+ Amals Server SFTP Service");

				while(running) {

					fullcommand = inFromClient.readLine();
					try {
						command = fullcommand.substring(0,4);
					}catch (IndexOutOfBoundsException e) {
						command = "";
					}

					switch (command) {
					case "USER":
						handleUser(fullcommand);
						break;

					case "ACCT":
						handleAcct(fullcommand);
						break;

					case "PASS":
						handlePass(fullcommand);
						break;

					case "TYPE":
						handleType(fullcommand);
						break;

					case "LIST":
						handleList(fullcommand, myFiles, currentDirectory);
						break;

					case "CDIR":
						currentDirectory = handleCDIR(fullcommand, account, currentDirectory);
						break;

					case "KILL":
						handleKill(fullcommand, account, currentDirectory);
						break;

					case "NAME":
						fileToRename = handleName(fullcommand, account, currentDirectory);
						break;

					case "TOBE":
						handleToBe(fullcommand, currentDirectory, fileToRename);
						break;

					case "DONE":
						clientPrinter("+Connection Closed");
						running = false;
						break;

					case "RETR":
						fileToSendLocation = handleRETR(fullcommand, currentDirectory, fileToSendLocation);
						break;

					case "SEND":
						handleSend(os, fileToSendLocation);
						break;

					case "STOP":
						clientPrinter("+ok, RETR aborted");
						break;

					case "STOR":
						fileNameToStore = fullcommand.substring(9);
						storeType = handleSTOR(fullcommand, serverFiles);
						break;

					case "SIZE":
						handleSize(fullcommand, connectionSocket, storeType, serverFiles, fileNameToStore); 
						break;

					default:
						clientPrinter("-Unknown Command");
						break;
					}
				}
			}

		}catch(Exception ioException) {
			System.out.println("server ERROR");
			ioException.printStackTrace();
		}
	}

}
