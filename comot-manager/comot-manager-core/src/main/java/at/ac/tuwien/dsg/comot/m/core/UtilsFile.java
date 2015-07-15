package at.ac.tuwien.dsg.comot.m.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class UtilsFile {

	public static void upload(File fileLocal, String host, String fileRemote, String user, File keypem)
			throws JSchException, IOException {

		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, 22);
		jsch.addIdentity(keypem.getAbsolutePath());
		// session.setPassword("password");

		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.connect();

		boolean ptimestamp = true;

		// exec 'scp -t rfile' remotely
		String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + fileRemote;
		Channel channel = session.openChannel("exec");
		((ChannelExec) channel).setCommand(command);
		String localFileName = fileLocal.getAbsolutePath();

		try (FileInputStream fis = new FileInputStream(localFileName);
				OutputStream out = channel.getOutputStream();
				InputStream in = channel.getInputStream()) {

			channel.connect();
			checkAck(in);

			if (ptimestamp) {
				command = "T" + (fileLocal.lastModified() / 1000) + " 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command += (" " + (fileLocal.lastModified() / 1000) + " 0\n");
				out.write(command.getBytes());
				out.flush();
				checkAck(in);
			}

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = fileLocal.length();

			command = "C0644 " + filesize + " ";
			if (localFileName.lastIndexOf('/') > 0) {
				command += localFileName.substring(localFileName.lastIndexOf('/') + 1);
			} else {
				command += localFileName;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();

			checkAck(in);

			// send a content of lfile
			byte[] buf = new byte[1024];
			while (true) {
				int len = fis.read(buf, 0, buf.length);
				if (len <= 0)
					break;
				out.write(buf, 0, len); // out.flush();
			}
			fis.close();

			// send '\0'
			buf[0] = 0;
			out.write(buf, 0, 1);
			out.flush();
			checkAck(in);

		} finally {
			if (channel != null && !channel.isClosed()) {
				channel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
	}

	protected static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		// 1 for error,
		// 2 for fatal error,
		// -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			String msg = IOUtils.toString(in, CharEncoding.UTF_8);
			throw new RuntimeException(msg);
		}
		return b;
	}

}
