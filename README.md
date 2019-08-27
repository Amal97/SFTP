# COMPSYS 725 : Assignment 1 - SFTP
Author: Amal Chandra
UPI: acha932
ID: 890345562

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)

Implement SFTP described in RFC 913 using Java. 
SFTP is a simple file transfer protocol.  It fills the need of people wanting a protocol that is more useful than TFTP but easier to implement (and less powerful) than FTP.  SFTP supports user access control, file transfers, directory listing, directory changing, file renaming and deleting.

The Protocol: 
SFTP is used by opening a TCP connection to the remote hosts' SFTP port (115 decimal).  You then send SFTP commands and wait for replies.  SFTP commands sent to the remote server are always 4 ASCII letters (of any case) followed by a space, the argument(s), and a <NULL>.  The argument can sometimes be null in which case the command is just 4 characters followed by <NULL>.  Replies from the server are always a response character followed immediately by an ASCII message string terminated by a <NULL>. A reply can also be just a response character and a <NULL>.

      <command> : = <cmd> [<SPACE> <args>] <NULL>
      <cmd> : =  USER ! ACCT ! PASS ! TYPE ! LIST ! CDIR
                 KILL ! NAME ! DONE ! RETR ! STOR
      <response> : = <response-code> [<message>] <NULL>
      <response-code> : =  + | - |   | !

For more info: https://tools.ietf.org/html/rfc913
# File Directory 

The files in the directory are:
-SFTP/
- clientFiles/
- files/
- src/
    - client/  
    -client.java
    - server/  
    -Account.java  
    -data.txt  
    -MyFiles.java  
    -server.java  

# Comiling and Running

# This has been tested on Windows

Navigate to src:

    cd src

To build the java files :

    javac server/*.java
    javac client/*.java
    
Go back to root :
      
      cd ..

To run the server and client (run server first) in different console: 

    java -cp src/ server/server
    java -cp src/ client/client

Assumptions:
   - Server runs on PORT 6789   
   - Commands and parameters must be one space apart to work succesfully
    
# Test
The 1st line of text is from the client while the indented lines below are from the server. eg:
    
    Client message
        Server response
# Login Information
All login information are saved in the data.txt which is saved in the server. The data.txt contains:
- super
- tom,account1,tom123
- dick,account2,dick123
- harry,account3,harry123

-The "super" user does not require ACCT or PASS   
-Each login is composed of USER,ACCT,PASS

## USER
Enter USER:

    USER <user-id>
    
### Test:

If a super-user logs in the user doesnt need to enter ACCT and PASS 

    USER super
        ! a1 valid, logged in
If a valid user logs in:

    USER tom
        +User-id valid, send account and password
    
If a non-valid user logs in:

    USER pam    
        -Invalid user-id, try again

        

        
## ACCT
Enter ACCT (Account):

    USER <user-id>
    
### Test:

If a user enters a valid USER and ACCT

    ACCT account1
        +Account valid, send password

If a user enters an invalid account

    ACCT myAccount
        -Invalid account, try again
        
## PASS
Enter PASS (Password):

    PASS <password>
    
### Test:

If a user enters valid user and valid account

    PASS tom123
        ! Logged in

If a user enters a correct password but didn't specify the account

    PASS tom123
        +Send account
        
If a user enters wrong password for the account   

    PASS potatoe
        -Wrong password, try again
        
## TYPE
The mapping of the stored file to the transmission byte stream is controlled by the type.  The default is binary if the type is not specified.

    TYPE {A | B | C}
    
### Test:

Requested for Ascii mode:

    TYPE A
        +Using Ascii mode
 
 Requested for Binary mode:
   
    TYPE B
        +Using Binary mode

Requested for Continuous mode:
    
    TYPE C
        +Using Continuous mode

Invalid request:

    TYPE D
        -Type not valid

## LIST
Lists contents in the given directory

    LIST { F | V } dir-path
    
### Test:
###### This is performed from the "SFTP" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\ )
\
Test F which specifies a standard formatted directory listing.

    LIST F files
    +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files
    \deleteThisFile.txt
    \sendThisFile.txt

Test  V which specifies a verbose directory listing.

    LIST V files
    +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files
    \deleteThisFile.txt    file created time: 18:43:02 26/08/2019    file last accessed time: 18:43:02 26/08/2019    file last modified time: 18:43:02 26/08/2019
    \sendThisFile.txt    file created time: 17:24:56 26/08/2019    file last accessed time: 17:24:56 26/08/2019    file last modified time: 18:45:28 26/08/2019
    
LIST V randomFile

    -Invalid directory

        
## CDIR
To change to a different directory

    CDIR <new-dir>
    
### Test:
###### This is performed from the “SFTP” folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\ )

\
If user is logged in and CDIR into "files"   

    CDIR files
        !Changed working dir to C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files

If user is not logged in and CDIR into "files"   

    CDIR files
        +directory ok, send account/password
        
If user is not logged in and CDIR into "files"   

    CDIR randomFolder
        -Can't connect to directory because: directory doesn't exist
        
        
## KILL
To delete a file

    KILL <file-spec>
    
### Test:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )

\
Try to delete a file which exists

    KILL deleteThisFile.txt
        +deleteThisFile.txt deleted

Try to delete a file which does not exists

    KILL noFile.txt
        -Not deleted because file doesn't exist
        
## NAME
To rename a file

    NAME <old-file-spec>
    
    If successful then:

    TOBE <new-file-spec>
    
### Test:
###### This is performed from the "clientFiles" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\clientFiles\ )
\
Renaming a file which exists in the current folder. CDIR to "clientFiles"
Select a file to rename

    NAME renameThisFile.txt
        +File exists

Rename the file to a new name

    TOBE newName.txt
        +renameThisFile.txt renamed to newName.txt
        
If file to rename does not exist
    
    NAME amal.txt
        -Can't find 
        NAME command is aborted, don't send TOBE.
        
## DONE
    DONE
    
Test:
To close the connection between the server and the client

    DONE
        +Closing Connection
        
## RETR
Save files from the server to the client

    RETR <file-spec>
    
### Test:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )

\
File to retrive from the server

    RETR retrThisFile.txt
        29
    
If successfull:
    
    SEND
        File Saved on Client's side
    
If the client doesn't have enough storage space, a "STOP" command will be automatically sent to the server

    STOP
        +ok, RETR aborted
       

## STOR
Store files in the server

    STOR { NEW | OLD | APP } <file-spec>
    
### Test for NEW:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )
\
If a file already exists in the server and STOR NEW command is executed

    STOR NEW sendThisFile.txt
        +File exists, will create new generation of file

If a file does not exists in the server and STOR NEW command is executed

    STOR NEW sendThisFile.txt
        +File does not exist, will create new file
    
### Test for OLD:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )
\
If a file already exists in the and STOR OLD command is executed

    STOR OLD sendThisFile.txt
        +Will write over old file

If a file does not exists in the and STOR OLD command is executed
        
    STOR OLD sendThisFile.txt
        +Will create new file
        
### Test for APP:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )
\
If a file already exists in the and STOR APP command is executed

    STOR APP sendThisFile.txt
        +Will append to file

If a file does not already exists in the and STOR APP command is executed
    
    STOR APP sendThisFile.txt
        +Will create file
        
### Test for SIZE:
###### This is performed from the "files" folder (e.g. +C:\Users\Amal\Downloads\Uni\Semester 2\COMPSYS 725\Assignments\Assignment_1\SFTP\SFTP\files\ )
\
If user tries to store a file in server and theres enough space in the server

    SIZE 
        +ok, waiting for file
        +Saved sendThisFile.txt

If user tries to store a file in server and there is not enough space in the server
        
    SIZE
        -Not enough room, don't send it
        
        
        
