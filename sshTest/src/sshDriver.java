import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;


public class sshDriver {

	public static String status(int num)
	{
		//  0 for success,
		//  1 for error,
		//  2 for fatal error,
		// -1

		if (num == 0)
			return "success";
		if (num == 1)
			return "error";
		if (num == 2)
			return "fatal error";

		return "wtf is -1";
	}

	public static void main(String[] args) 
	{
		sshSession s = new sshSession();
		String userName = "testuser";
		String password = "abc123";
		String host = "192.168.123.9";
		String command = "touch testfile_1234";
		String local_file = "/tmp/adb.log";
		String remote_file = "/home/testuser/remote_file.txt";
		int status = 0;

		s.openSession(userName, host, password);
		if (s.session.isConnected())
		{
			try {
				status = s.copyFileToRemote(s.session, remote_file, local_file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(status);


			s.closeSession();
			System.out.println("Session closed");
		}
		else
		{
			System.out.println("connection failed");
		}

	}

}
