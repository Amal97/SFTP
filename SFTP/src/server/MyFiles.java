package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyFiles {
	
	private String path = "C:\\Users\\Amal\\Downloads\\Uni\\Semester 2\\COMPSYS 725\\Assignments\\Assignment-1\\SFTP\\";
	
//    public static void sendFile(String fileName, BufferedWriter outToClient, String transferType) {
//		FileInputStream fis = new FileInputStream(fileName);
//		byte[] buffer = new byte[4096];
//		
//		while (fis.read(buffer) > 0) {
//			outToClient.write(); (buffer);
//		}
//    }
	
	
	public String listAllFiles(String dir, String format) {
		String fullPath = path+dir+"\\";
		try (Stream<Path> walk = Files.walk(Paths.get(fullPath))) {
			String allFiles = fullPath+"\r\n" ;

	        File folder = new File(dir);
	        File[] listOfFiles = folder.listFiles();
			
			List<String> result = walk.filter(Files::isRegularFile).map(x -> x.toString()).collect(Collectors.toList());

			if(format.equals("F")) {
				for(int i = 0; i < result.size(); i++) {
					String file = result.get(i).replace(fullPath, "");
					allFiles = allFiles + file + "\r\n";
				}
			}
			else if(format.equals("V")) {
				for(int i = 0; i < result.size(); i++) {
					String file = result.get(i).replace(fullPath, "");
	
	                BasicFileAttributes info = Files.readAttributes(Paths.get(listOfFiles[i].getPath()), BasicFileAttributes.class);
	
	                DateFormat df = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
	                String creationTime = "    file created time: " + df.format(info.creationTime().toMillis());
	                String lastAccessTime = "    file last accessed time: " + df.format(info.lastAccessTime().toMillis());
	                String lastModifiedTime = "    file last modified time: " + df.format(info.lastModifiedTime().toMillis());
	
	                allFiles = allFiles + file + creationTime + lastAccessTime + lastModifiedTime + "\r\n";
	                }
				}
			
			return allFiles;
			
			} catch (IOException e) {
				//e.printStackTrace();
				return "-Invalid directory";
				}
		}
	}
