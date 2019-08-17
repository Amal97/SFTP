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
	private static int tester = 0;		// CHANGE THIS TO BE ABLE TO PRINT IN CONSOLE
	
	public static String currentDir() {
		return System.getProperty("user.dir");
	}
	
	public static String goBackDir() {
		return new File(System.getProperty("user.dir")).getParentFile().toString();
	}
	
	public static void clientPrinter(String message) throws IOException {
		if(tester == 0) {
			outToClient.writeBytes(message+"\n");
			outToClient.flush();
		}
		else {
			System.out.println(message);
		}
	}
	
	public static void main(String argv[]) throws Exception{
		try {
			ServerSocket welcomeSocket = new ServerSocket(6789);

			Account account = new Account();
			MyFiles myFiles = new MyFiles();
			String currentDirectory = currentDir();
			String fileToRename = "";
			String fileToSendLocation = "";
			String fullComand = "";
			String command = "";
			
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
				
				outToClient.writeBytes("+ Amals Server SFTP Service\n");
				outToClient.flush();


				while(running) {
					
				    fullComand = inFromClient.readLine();
					command = fullComand.substring(0,4);

					switch (command) {
						case "USER":
							String user = fullComand.substring(4) ;
							if(account.isLoggedIn(user)) {
								clientPrinter("!<user-id> logged in");
							}
							else if(account.validUser(user)) {
								clientPrinter("+User-id valid, send account and password");
							}
							else {
								clientPrinter("-Invalid user-id, try again");
							}
							break;
							
						case "ACCT":
							String accountName = fullComand.substring(4);
	
							if(account.isLoggedIn(accountName)) {
								clientPrinter("! Account valid, logged-in");
							}
							else if(account.validAccount(accountName)) {
								clientPrinter("+Account valid, send password");
							}
							else {
								clientPrinter("Invalid account, try again");
							}		
							break;
							
						case "PASS":
							String password = fullComand.substring(4);
	//						String password = UserCommand.substring(4);
							if(account.alreadyInAccount() && account.validPassword(password)){
								clientPrinter("! Logged in");
							}
							else if(!account.alreadyInAccount() && account.validPassword(password)) {
								clientPrinter("Password ok but you haven't specified the account\"");
							}
							else {
								clientPrinter("-Wrong password, try again");
							}	
							break;
						
						case "TYPE":
							String type = fullComand.substring(4);
	//						String type = UserCommand.substring(4);
							if(type == "A") {
								clientPrinter("+Using Ascii mode");
							}
							else if(type == "B") {
								clientPrinter("+Using Binary mode");
							}
							else if(type =="C") {
								clientPrinter("+Using Continuous mode");
							}
							else {
								clientPrinter("-Type not valid");
							}
							break;
						
						case "LIST":
							String format = fullComand.substring(5,6);
	//						String format = UserCommand.substring(5,6);
							String dir = inFromClient.readLine().substring(6).trim();
							if(format.contentEquals("F")) {
								String toPrint = myFiles.listAllFiles(dir,"F");
								clientPrinter(toPrint);
							}
							else if(format.contentEquals("V")) {
								String toPrint = myFiles.listAllFiles(dir,"V");
								clientPrinter(toPrint);
							}
							break;
						
						case "CDIR":
						    if(account.alreadyInAccount()) {
								String newDir = fullComand.substring(5);
	//							String newDir = UserCommand.substring(5);
	                            String checkNewDir = Paths.get(currentDirectory, newDir).toString();
								Path path = Paths.get(checkNewDir);
								
								if(newDir.equals("..")) {
									currentDirectory = goBackDir();
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
						    break;
						    
						case "KILL":
						    if(account.alreadyInAccount()) {
								String fileToDelete = fullComand.substring(5);
	//							String fileToDelete = UserCommand.substring(5);
		                        String fileLocation = Paths.get(currentDirectory, fileToDelete).toString();
	//							Path path = Paths.get(fileLocation);
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
							break;
						
						case "NAME":
						    if(account.alreadyInAccount()) {
								String tempFileToRename = fullComand.substring(5);
	//							String tempFileToRename = UserCommand.substring(5);
		                        String fileLocation = Paths.get(currentDirectory, fileToRename).toString();
								Path path = Paths.get(fileLocation);
						        File file = new File(fileLocation); 
								if(Files.exists(path)) {
									fileToRename = tempFileToRename;
									clientPrinter("+File exists");
								}
								else {
									clientPrinter("-Can't find "+ fileToRename +"\n NAME command is aborted, don't send TOBE.");
								}
						    }
							else {
								clientPrinter("-You must send ACCT and PASS to use CDIR");
						    }
							break;
							
						case "TOBE":
							String newFileName = fullComand.substring(5);
	//						String newFileName = UserCommand.substring(5);
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
							break;
						
						case "DONE":
							clientPrinter("+Connection Closed");
							running = false;
							break;
							
						case "RETR":
							String fileName = fullComand.substring(5);
	//						String fileName = UserCommand.substring(5);
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
							break;
						
						case "SEND":
							  File fileToSend = new File(fileToSendLocation);
						      byte[] mybytearray = new byte[(int) fileToSend.length()];
						      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend));
						      bis.read(mybytearray, 0, mybytearray.length);
						      os.write(mybytearray, 0, mybytearray.length);
						      os.flush();	
						      
						      break;
						      
	                    default:
	                        user = "-Unknown Command";
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
