import com.jcraft.jsch.*;

import java.io.*;

/*If no exception is thrown, then the file transfers completed successfully, 
 * ignore the return code for the file transfer commands.
 * However, the return codes for the executeCommand function are valid.*/
public class sshTest {

	static String userName = "";
	static String host = "";
	static String password = "";

	public static void main(String args[])
	{
		try
		{
			int exit_value = -999;
			exit_value = copyFileToRemote("C:\\Users\\Nicholas\\workspace\\test\\I1.jpg","test.jpg");
			System.out.println("Copy up exit value: "+ exit_value);
			System.out.println("Copy up Complete");
			
			executeCommand("stat -c %s test.jpg");
			executeCommand("ls");
			
			exit_value = copyFileFromRemote("test.jpg","C:\\Users\\Nicholas\\workspace\\test\\test_returned.jpg");
			System.out.println("Copy down exit value: "+ exit_value);
			System.out.println("Copy down Complete");
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
	}
	public static int copyFileToRemote(String localFile,String remoteFile)throws Exception
	{
		JSch jsch=new JSch();
	      Session session=jsch.getSession(userName, host, 22);

	      session.setPassword(password);
	      jsch.setConfig("StrictHostKeyChecking", "no");
	      session.connect();
	      ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
	      sftpChannel.connect();
	      
	      //upload the file
	      sftpChannel.put(localFile, remoteFile);
	      
	      sftpChannel.exit();
	      sftpChannel.disconnect();
	      session.disconnect();
	      return sftpChannel.getExitStatus();

	}
	public static int copyFileFromRemote(String remoteFile, String localFile)throws Exception
	{
		JSch jsch=new JSch();
	      Session session=jsch.getSession(userName, host, 22);

	      session.setPassword(password);
	      jsch.setConfig("StrictHostKeyChecking", "no");
	      session.connect();
	      ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
	      sftpChannel.connect();
	      
	      //download the file
	      sftpChannel.get(remoteFile, localFile);
	      
	      sftpChannel.exit();
	      sftpChannel.disconnect();
	      session.disconnect();
	      return sftpChannel.getExitStatus();

	}
	/*public static int copyFileToRemote(String localFile,String remoteFile) throws Exception
	{
	      JSch jsch=new JSch();
	      Session session=jsch.getSession(userName, host, 22);

	      session.setPassword(password);
	      jsch.setConfig("StrictHostKeyChecking", "no");
	      session.connect();

	      // exec 'scp -f rfile' remotely
	      boolean ptimestamp = true;
	      String command="scp " + (ptimestamp ? "-p" :"") +" -t "+remoteFile;
	      Channel channel=session.openChannel("exec");
	      ((ChannelExec)channel).setCommand(command);

	      // get I/O streams for remote scp
	      OutputStream out=channel.getOutputStream();
	      InputStream in=channel.getInputStream();

	      channel.connect();
	      
	      File _lfile = new File(localFile);

	      if(ptimestamp){
	        command="T "+(_lfile.lastModified()/1000)+" 0";
	        // The access time should be sent here,
	        // but it is not accessible with JavaAPI ;-<
	        command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
	        out.write(command.getBytes()); out.flush();
	        if(checkAck(in)!=0){
	  	  System.exit(0);
	        }
	      }

	      // send "C0644 filesize filename", where filename should not include '/'
	      long filesize=_lfile.length();
	      command="C0644 "+filesize+" ";
	      command+=_lfile.getName();
	      command+="\n";
	      out.write(command.getBytes());
	      out.flush();
	      
	      if(checkAck(in)!=0){
		System.exit(0);
	      }
	        // read a content of lfile
		    FileInputStream file_input = new FileInputStream(_lfile);
		    byte[] buffer= new byte[(int) filesize];
		    System.out.println("FS "+ filesize);
		    byte[] buf=new byte[1024];
		    file_input.read(buffer, 0, buffer.length);
		    System.out.println("AVB: "+file_input.available());
		    FileOutputStream st = new FileOutputStream("C:\\Users\\Nicholas\\workspace\\test\\local_wb.jpg");
		    st.write(buffer, 0, buffer.length);
		    st.flush();
		    st.close();
		    
	        out.write(buffer, 0, buffer.length);
	        out.flush();
	        file_input.close();

		if(checkAck(in)!=0){
		  System.exit(0);
		}

	        // send '\0'
	        buf[0]=0; 
	        out.write(buf, 0, 1); 
	        out.flush();
	        out.close();
	      session.disconnect();
		return 0;
	}*/
	/*
	public static int copyFileFromRemote(String remoteFile, String localFile)throws Exception
	{
		 FileOutputStream fos=null;
		      JSch jsch=new JSch();
		      Session session=jsch.getSession(userName, host, 22);

		      session.setPassword(password);
		      jsch.setConfig("StrictHostKeyChecking", "no");
		      session.connect();

		      // exec 'scp -f rfile' remotely
		      String command="scp -f "+remoteFile;
		      Channel channel=session.openChannel("exec");
		      ((ChannelExec)channel).setCommand(command);

		      // get I/O streams for remote scp
		      OutputStream out=channel.getOutputStream();
		      InputStream in=channel.getInputStream();

		      channel.connect();

		      byte[] buf=new byte[1024];

		      // send '\0'
		      buf[0]=0; 
		      out.write(buf, 0, 1); 
		      out.flush();

		      //Get Return values: filesize and filename, accessed by sending '\0' character to server
		      while(true){
			int c=checkAck(in);
		        if(c!='C'){
			  break;
			}

		        // read '0644 '
		        in.read(buf, 0, 5);
		        long filesize=0L;
		        while(true){
		          if(in.read(buf, 0, 1)<0){
		            // error
		            break; 
		          }
		          if(buf[0]==' ')break;
		          filesize=filesize*10L+(long)(buf[0]-'0');
		        }
		        //get the file name to clear the input stream so we can get to the file data
		        byte[] fname_byte = new byte[in.available()];
		        in.read(fname_byte);
		        String f_name = new String(fname_byte);
		        System.out.println(f_name);
		        
		        // send null terminator
		        buf[0]=0; 
		        out.write(buf, 0, 1); 
		        out.flush();

		        // read a content of the file from the input stream from the server
		        //now grab in one chunk instead of 1K chunks
		        
		        fos=new FileOutputStream(localFile);
		        byte[] buffer = new byte[(int)filesize];
		        in.read(buffer);
		        fos.write(buffer);
		        
		        fos.close();
		        fos=null;

			if(checkAck(in)!=0){
				return 0;
			}

		        // send '\0'
		        buf[0]=0; 
		        out.write(buf, 0, 1); 
		        out.flush();
		      }

		      session.disconnect();
		return 0;
	}*/
	public static int executeCommand(String remoteCommand)throws Exception
	{
		JSch jsch=new JSch();
		Session session=jsch.getSession(userName, host, 22);
		session.setPassword(password);
	    jsch.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		
		 Channel channel=session.openChannel("exec");
	     ((ChannelExec)channel).setCommand(remoteCommand);
	     
	     channel.setInputStream(null);

	      InputStream in=channel.getInputStream();

	      channel.connect();
	      
	      //Get return data, channel will close once all data has been returned
	      byte[] tmp=new byte[1024];
	      while(true){
	        while(in.available()>0){
	          int i=in.read(tmp, 0, 1024);
	          if(i<0)break;
	          //output the returned data to the console
	          System.out.print(new String(tmp, 0, i));
	        }
	        if(channel.isClosed()){
	        	//all data has been returned
	          break;
	        }
	        //if the remote system is processing, go to sleep and try again to see if data is available
	        try{Thread.sleep(1000);}catch(Exception ee){}
	      }
	      channel.disconnect();
	      session.disconnect();
	      
		return channel.getExitStatus();
	}
	static int checkAck(InputStream in) throws IOException{
	    int b=in.read();
	    // b may be 0 for success,
	    //          1 for error,
	    //          2 for fatal error,
	    //          -1
	    if(b==0) return b;
	    if(b==-1) return b;

	    if(b==1 || b==2){
	      StringBuffer sb=new StringBuffer();
	      int c;
	      do {
		c=in.read();
		sb.append((char)c);
	      }
	      while(c!='\n');
	      if(b==1){ // error
		System.out.print(sb.toString());
	      }
	      if(b==2){ // fatal error
		System.out.print(sb.toString());
	      }
	    }
	    return b;
	  }
}
