import com.jcraft.jsch.*;

import java.io.*;

public class sshTest {

	String userName;
	String password;
	String command;
	String local_file;
	String remote_file;
	
	JSch jsch=new JSch();
    Session session;
    OutputStream out;
    InputStream in;
    Channel channel = null;
    
    File _lfile;
    FileInputStream fis=null;
    FileOutputStream fos=null;
	
	public void openSession(String usrName,String host, String pswd)
	{
		try
		{
		session=jsch.getSession(usrName, host, 22);
		session.connect();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		try
		{
	      channel=session.openChannel("exec");
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		//return session;
	}
	public void closeSession()
	{
		channel.disconnect();
	    session.disconnect();
	}
	public int copyFileToRemote(Session session,String remoteFile, String localFile) throws Exception
	{
		boolean ptimestamp = true;
		String command="scp " + (ptimestamp ? "-p" :"") +" -t "+remoteFile;
		
		
	      ((ChannelExec)channel).setCommand(command);

	      // get I/O streams for remote scp
	      try
	      {
	      out=channel.getOutputStream();
	      in=channel.getInputStream();
	      }
	      catch(IOException e)
	      {
	    	  System.out.println(e.toString());
	      }
	      channel.connect();
	      
	      _lfile = new File(localFile);

	      if(ptimestamp){
	        command="T "+(_lfile.lastModified()/1000)+" 0";
	        // The access time should be sent here,
	        // but it is not accessible with JavaAPI ;-<
	        command+=(" "+(_lfile.lastModified()/1000)+" 0\n"); 
	        out.write(command.getBytes()); out.flush();
	      }
	      
	      long filesize=_lfile.length();
	      command="C0644 "+filesize+" ";
	      if(localFile.lastIndexOf('/')>0){
	        command+=localFile.substring(localFile.lastIndexOf('/')+1);
	      }
	      else{
	        command+=localFile;
	      }
	      command+="\n";
	      out.write(command.getBytes()); out.flush();
	      
	      fis=new FileInputStream(localFile);
	      byte[] buf=new byte[1024];
	      while(true){
	        int len=fis.read(buf, 0, buf.length);
		if(len<=0) break;
	        out.write(buf, 0, len); //out.flush();
	      }
	      fis.close();
	      fis=null;
	      
	      buf[0]=0; out.write(buf, 0, 1); out.flush();
	      out.close();  
	      return(checkAck(in));
	}
	public int copyFileFromRemote(Session session,String remoteFile, String localFile)throws Exception
	{
		String prefix=null;
	      if(new File(localFile).isDirectory()){
	        prefix=localFile+File.separator;
	      }
	      String command="scp -f "+remoteFile;
	      
	      ((ChannelExec)channel).setCommand(command);
	      
	      out=channel.getOutputStream();
	      in=channel.getInputStream();
	      
	      channel.connect();
	      
	      byte[] buf=new byte[1024];

	      // send '\0'
	      buf[0]=0; out.write(buf, 0, 1); out.flush();

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

	        String file=null;
	        for(int i=0;;i++){
	          in.read(buf, i, 1);
	          if(buf[i]==(byte)0x0a){
	            file=new String(buf, 0, i);
	            break;
	  	  }
	        }
	        buf[0]=0; out.write(buf, 0, 1); out.flush();

	        // read a content of localFile
	        fos=new FileOutputStream(prefix==null ? localFile : prefix+file);
	        int foo;
	        while(true){
	          if(buf.length<filesize) foo=buf.length;
		  else foo=(int)filesize;
	          foo=in.read(buf, 0, foo);
	          if(foo<0){
	            // error 
	            break;
	          }
	          fos.write(buf, 0, foo);
	          filesize-=foo;
	          if(filesize==0L) break;
	        }
	        fos.close();
	        fos=null;

		

	        // send '\0'
	        buf[0]=0; out.write(buf, 0, 1); out.flush();
	      }

	      session.disconnect();
	      return checkAck(in);
	      
	}
	public int executeCommand(String remoteCommand)throws Exception
	{
		((ChannelExec)channel).setCommand(remoteCommand);
		    channel.setInputStream(null);
	      ((ChannelExec)channel).setErrStream(System.err);

	      InputStream in=channel.getInputStream();
	      
	      byte[] tmp=new byte[1024];
	      while(true){
	        while(in.available()>0){
	          int i=in.read(tmp, 0, 1024);
	          if(i<0)break;
	          System.out.print(new String(tmp, 0, i));
	        }
	        if(channel.isClosed()){
	          System.out.println("exit-status: "+channel.getExitStatus());
	          break;
	        }
	        try{Thread.sleep(1000);}catch(Exception ee){}
	      }
	      return 0;
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
