package com.sc.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Scanner;

import com.sc.utilities.Pair;

/**
 * @author Felipe
 *
 */
public class Client {

	/**
	 * Run client
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Check if folder for server files exist else create.
		File serverFolder = new File("Client");
		if (!serverFolder.exists()) {
			if (!serverFolder.mkdir())
				// Folder creation failed
				System.err.println("[" + LocalDateTime.now() + "] " + "Failed creating server folder!");
			return;
		}

		// Folder created we can start our server.
		try {
			System.out.println("[" + LocalDateTime.now() + "] " + "Starting Client.");
			startClient(args);
		} catch (SocketException se) {
			System.err.println(se.getMessage());
		}

	}

	private static void startClient(String[] args) throws SocketException {
		try {
			Socket socket;
			Scanner input = new Scanner(System.in);

			// Make connection
			String[] untreatedSv = args[2].split(":");
			String ip = untreatedSv[0];
			int sock = Integer.parseInt(untreatedSv[1]);
			Pair<String, Integer> connectionSettings = new Pair<String, Integer>(ip, sock);
			socket = new Socket(connectionSettings.first(), connectionSettings.second());
			System.out.println("[" + LocalDateTime.now() + "] " + "Found Server!");
			// Log in or exit if wrong password.
			handleLogIn(input, socket, args[0], args[1]);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleLogIn(Scanner input, Socket socket, String username, String pwd) {
		try {
			ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

			// Send our user and pw.
			out.writeObject(username);
			out.writeObject(pwd);

			// Check our answer.
			// Wacky stuff happens with serialization.
			boolean first = (boolean) in.readObject();
			String second = (String) in.readObject();

			Pair<Boolean, String> res = new Pair<Boolean, String>(first, second);
			if (res.first()) {
				System.out.println("[" + LocalDateTime.now() + "] " + "Welcome " + username);
				String userDir = "Client/" + username;
				File dir = new File(userDir);
				if (!dir.exists())
					dir.mkdir();

				// All good and logged in lets handle commands
				handleCommands(input, socket, username, pwd, in, out);
			} else {
				System.out.println(res.second());
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static void handleCommands(Scanner input, Socket socket, String username, String pwd, ObjectInputStream in,
			ObjectOutputStream out) {
		boolean quit = false;
		while (!quit) {
			System.out.println("[" + LocalDateTime.now() + "] " + "Escolha uma operacao:");
			System.out.println(
					"[" + LocalDateTime.now() + "] " + "[ -a <photo> | -l <userId> | -i <userId> | -g <userId> \n"
							+ "| -f <followUserIds> | -r <followUserIds> | -quit ]");
			String op = input.nextLine();
			String[] args = op.split(" ");

			switch (args[0]) {
			case "-a":
				sendPhoto(username, pwd, args, out, in);
				break;
			case "-l":
				getList(args, out, in);
				break;
			case "-i":
				isFollower(args, out, in);
				break;
			case "-g":
				getPhotos(args, in, out);
				break;
			case "-f":
				follow(args, in, out);
				break;
			case "-r":
				unfollow(args, out, in);
				break;
			case "-quit":
				quit = !quit;
				System.out.println("[" + LocalDateTime.now() + "] " + "Closing");
				break;
			default:
				System.out.println("[" + LocalDateTime.now() + "] " + "Invalid operation");
			}
		}

		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void unfollow(String[] args, ObjectOutputStream out, ObjectInputStream in) {
		try {
			// Send our operation.
			out.writeObject(args[0]);
			// Send our operation parameters
			out.writeObject(args[1]);

			// Read answer
			String answer = (String) in.readObject();
			System.out.println("[" + LocalDateTime.now() + "] " + answer);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void follow(String[] args, ObjectInputStream in, ObjectOutputStream out) {
		try {
			// Send our operation.
			out.writeObject(args[0]);
			// Send our operation parameters
			out.writeObject(args[1]);

			// Read answer
			String answer = (String) in.readObject();
			System.out.println("[" + LocalDateTime.now() + "] " + answer);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getPhotos(String[] args, ObjectInputStream in, ObjectOutputStream out) {
		try {
			// Send our operation.
			out.writeObject(args[0]);
			// Send our operation parameters
			out.writeObject(args[1]);

			// Read answer
			// TODO
			String answer = (String) in.readObject();
			System.out.println("[" + LocalDateTime.now() + "] " + answer);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void isFollower(String[] args, ObjectOutputStream out, ObjectInputStream in) {
		try {
			// Send our operation.
			out.writeObject(args[0]);
			// Send our operation parameters
			out.writeObject(args[1]);

			// Read answer
			String answer = (String) in.readObject();
			System.out.println("[" + LocalDateTime.now() + "] " + answer);
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void getList(String[] args, ObjectOutputStream out, ObjectInputStream in) {
		try {
			// Send our operation.
			out.writeObject(args[0]);

			// Send our operation parameters
			out.writeObject(args[1]);

			// If we're following the wanted user we can see the details
			Boolean following = (Boolean) in.readObject();
			if (following) {

				// Read answer how many times we need for our wanted details.
				String answer = (String) in.readObject();
				while (answer.equals("")) {
					System.out.println("[" + LocalDateTime.now() + "] " + answer);
				}
			} else {
				System.out.println("[" + LocalDateTime.now() + "] " + "You are not following that user");
			}
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void sendPhoto(String localUser, String localPw, String[] args, ObjectOutputStream out,
			ObjectInputStream in) {
		try {
			// Get our photo which needs to be on our client user folder.
			File photo = new File("Client/" + localUser + "/" + args[1]);
			long bytesToSend = photo.length();

			if (photo.exists()) {
				// Send our operation
				out.writeObject(args[0]);

				// Send our picture name to check if its already there or not.
				out.writeObject(args[1]);
				Boolean allowed = (Boolean) in.readObject();

				if (allowed) {
					// Means we can send the picture because it's non existent server side
					FileInputStream photoStream = new FileInputStream(photo);

					// Send 1Mb at a time
					byte[] buffer = new byte[1024];
					int bytesRead = 1024;

					// Send how many bytes server should expect from us
					out.writeObject(photo.length());
					while ((bytesRead = photoStream.read(buffer, 0,
							(int) (bytesToSend < 1024 ? bytesToSend : 1024))) > 0) {
						out.write(buffer, 0, bytesRead);
						// Update how manye bytes left to be sent.
						bytesToSend -= bytesRead;
						out.flush();
					}
					String res = (String) in.readObject();
					System.out.println("[" + LocalDateTime.now() + "] " + res);
					photoStream.close();

				} else {
					String res = (String) in.readObject();
					System.out.println("[" + LocalDateTime.now() + "] " + res);
				}

			} else {
				System.out.println("[" + LocalDateTime.now() + "] Given photo not found.");
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
