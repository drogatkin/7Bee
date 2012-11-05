// $Id: scp.java,v 1.3 2010/07/22 04:23:18 dmitriy Exp $
//Bee Copyright (c) 2004-2009 Dmitriy Rogatkin
// Created on Oct 12, 2009

package org.bee.func;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bee.util.Misc;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class scp {
	private static final boolean DEBUG_ = false;

	public static List<String> eval(String... copyPairs) {
		List<String> result = new ArrayList<String>();

		if (DEBUG_)
			System.out.printf("scp: %s %s%n", copyPairs[0], copyPairs[1]);
		String lfile = copyPairs[0];
		int pat = copyPairs[1].lastIndexOf('@');
		if (pat < 0) {
			System.err.printf("No '@' user host separator in 2nd parameter%n", copyPairs[1]);
			return result;
		}
		String user = copyPairs[1].substring(0, pat);
		copyPairs[1] = copyPairs[1].substring(pat + 1);
		pat = copyPairs[1].indexOf(':');
		String host = copyPairs[1].substring(0, pat);
		String rfile = copyPairs[1].substring(pat + 1);
		pat = user.indexOf(':');
		String password = null;
		if (pat > 0) {
			password = user.substring(pat + 1);
			user = user.substring(0, pat);
		}
		JSch jsch = new JSch();
		int port = 22;
		if (copyPairs.length == 3)
			port = Integer.parseInt(copyPairs[2]);
		try {

			Session session = jsch.getSession(user, host, port);

			// username and password will be given via UserInfo interface.
			final String fPassword = password;
			UserInfo ui = new UserInfo() {
				boolean passwdWasRequested;
				char[] consolePass;
				@Override
				public String getPassphrase() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getPassword() {
					if (fPassword != null)
						return fPassword;
					if (consolePass != null)
						return new String(consolePass);
					return null;
				}

				@Override
				public boolean promptPassphrase(String mess) {
					System.out.printf("%s%n", mess);
					return false;
				}

				@Override
				public boolean promptPassword(String mess) {
					System.out.printf("%s", mess);
					if (fPassword == null) {
						consolePass = System.console().readPassword();
						if (consolePass == null)
							return false;
					} else {
						if (passwdWasRequested)
							return false;
						System.out.printf("%s%n", "********");
						passwdWasRequested = true;
					}
					return true;
				}

				@Override
				public boolean promptYesNo(String mess) {
					System.out.printf("%sY%n", mess);
					return true;
				}

				@Override
				public void showMessage(String mess) {
					System.out.printf("%s%n", mess);
				}
			};
			session.setUserInfo(ui);
			session.connect();

			// exec 'scp -t rfile' remotely
			String command = "scp -p -t " + rfile;
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out = channel.getOutputStream();
			InputStream in = channel.getInputStream();

			channel.connect();

			if (checkAck(in) != 0) {
				System.err.printf("No Ack%n");
				return result;
			}

			// send "C0644 filesize filename", where filename should not include '/'
			long filesize = (new File(lfile)).length();
			command = "C0644 " + filesize + " ";
			if (lfile.lastIndexOf('/') > 0) {
				command += lfile.substring(lfile.lastIndexOf('/') + 1);
			} else {
				command += lfile;
			}
			command += "\n";
			out.write(command.getBytes());
			out.flush();
			if (checkAck(in) != 0) {
				System.err.printf("No Ack%n");
				return result;
			}

			FileInputStream fis;
			// send a content of lfile
			Misc.copyStream(fis = new FileInputStream(lfile), out, 0);
			fis.close();
			fis = null;
			// send '\0'
			out.write(0);
			out.flush();
			if (checkAck(in) != 0) {
				System.err.printf("No Ack%n");
				return result;
			}
			out.close();

			channel.disconnect();
			session.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSchException e) {
			e.printStackTrace();
		} finally {
			
		}

		return result;
	}

	static int checkAck(InputStream in) throws IOException {
		int b = in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if (b == 0)
			return b;
		if (b == -1)
			return b;

		if (b == 1 || b == 2) {
			StringBuffer sb = new StringBuffer();
			int c;
			do {
				c = in.read();
				sb.append((char) c);
			} while (c != '\n');
			if (b == 1) { // error
				System.out.print(sb.toString());
			}
			if (b == 2) { // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}

}
