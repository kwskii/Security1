package com.sc.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

import com.sc.utilities.Pair;

/**
 * Server class
 * 
 * @author Felipe
 *
 */
public class PhotoShare {

	// State of PhotoShare server.
	private UserCatalog uc;

	// Connection socket.
	private ServerSocket socket;

	public PhotoShare() {
		// Prepare the socket for listening.
		try {
			this.socket = new ServerSocket(23232);
			this.uc = new UserCatalog("Users.txt");
		} catch (IOException e) {
			System.err.println(
					"[" + LocalDateTime.now() + "] " + "Failed creating socket 23232\nMore info:" + e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Starts listening for connections and handles accepting logic
	 * 
	 * @throws IOException
	 */
	public void startListening() throws IOException {
		while (true) {
			if (this.socket.getLocalPort() != -1) {
				try {
					Socket client = this.socket.accept();
					ServerThread serverThread = new ServerThread(client, this);
					serverThread.start();
				} catch (Exception e) {
					e.printStackTrace();
					this.socket.close();
				}

			} else {
				System.err.println("[" + LocalDateTime.now() + "] " + "Port is still closed!Exiting..");
			}
		}
	}

	public boolean populateUsers(String users) {
		return uc.populate(users) ? true : false;
	}

	public Pair<Boolean, String> authUser(String inUser, String inPasswd) {
		return uc.authUser(inUser, inPasswd);
	}

	public void addPhoto(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut)
			throws ClassNotFoundException, IOException {
		clientOut.writeObject(this.uc.addPhoto(localUser, clientIn, clientOut).second());
	}

	public void checkFollower(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut)
			throws ClassNotFoundException, IOException {
		String userCheck = (String) clientIn.readObject();

		if (userCheck.equals(localUser.username)) {
			System.out.println("[" + LocalDateTime.now() + "] You can't follow yourself");
			clientOut.writeObject("You can't follow yourself ");
		} else {
			Pair<Boolean, String> result = this.uc.checkFollower(localUser, userCheck);
			System.err.println("[" + LocalDateTime.now() + "] " + result.second());
			clientOut.writeObject(result.second());
		}
	}

	public void listPhotos(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut) {
		try {
			String userCheck = (String) clientIn.readObject();
			// Prepare response
			Pair<Boolean, String> result = this.uc.checkFollower(localUser, userCheck);
			if (!result.second().contains("not found")) { // If localUser follows userCheck
				if (result.first() || localUser.username.equals(userCheck)) {
					// Make client get ready for list
					clientOut.writeObject(true);
					// Get our targets object
					User target = this.uc.get(userCheck);
					// For our loop
					int howManyPhotos = target.getPhotos().size();
					// Send client how big our list will be
					clientOut.writeObject(howManyPhotos);
					int count = 1;
					if (howManyPhotos > 0) {
						for (Photo p : target.getPhotos()) {
							clientOut.writeObject(
									"Photo [" + count + "]: Name " + p.photo + ", Date created " + p.dateCreated);
							count++;
						}
					} else {
						clientOut.writeObject("User has no pictures");
					}

				} else {
					// Doesnt follow
					// Say client isn't authorized.
					clientOut.writeObject(false);
					System.err.println("[" + LocalDateTime.now() + "] " + result.second());
					clientOut.writeObject(result.first());
				}
			} else {
				clientOut.writeObject(result.first());
				System.err.println("[" + LocalDateTime.now() + "] " + result.second());
				clientOut.writeObject(result.second());
			}

		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void sendPhotos(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut) {
		try {
			String userCheck = (String) clientIn.readObject();
			// See if userCheck is followed by localUser
			Pair<Boolean, String> result = this.uc.checkFollower(localUser, userCheck);

			// If localUser follows userCheck or localUser wants his photos
			if (result.first() || userCheck.equals(localUser.username)) {
				// Send to client he's authorized to get photos
				clientOut.writeObject(true);

				// Delegate to sendPhotos on our user class.
				User user = this.uc.get(userCheck);
				user.sendPhotos(clientOut, clientIn);
			} else {
				// Doesnt follow.
				clientOut.writeObject(false);
				System.err.println("[" + LocalDateTime.now() + "] " + result.second());
				clientOut.writeObject(result.second());
			}

		} catch (IOException |

				ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	public void unfollow(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut) {
		try {
			String userCheck = (String) clientIn.readObject();

			if (userCheck.equals(localUser.username)) {
				System.out.println("[" + LocalDateTime.now() + "] You can't follow yourself");
				clientOut.writeObject("You can't follow yourself ");
			} else {

				Pair<Boolean, String> result = this.uc.checkFollower(localUser, userCheck);
				// If not a valid user.
				if (result.second().contains("not found")) {
					System.out.println("[" + LocalDateTime.now() + "] " + userCheck + " is not a user.");
					clientOut.writeObject(userCheck + " is not a user.");
				} else {
					// If localUser follows then unfollow
					if (result.first()) {
						User user = this.uc.get(userCheck);
						user.removeFollower(localUser.username);
						System.out.println(
								"[" + LocalDateTime.now() + "] " + localUser + " no longer follows " + userCheck);
						clientOut.writeObject(localUser + " no longer follows "
								+ userCheck.substring(0, 1).toUpperCase() + userCheck.substring(1));
					} else {
						// Doesnt follow
						System.err.println("[" + LocalDateTime.now() + "] " + result.second());
						clientOut.writeObject(result.second());
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void follow(User localUser, ObjectInputStream clientIn, ObjectOutputStream clientOut) {
		try {
			String userCheck = (String) clientIn.readObject();

			if (userCheck.equals(localUser.username)) {
				System.out.println("[" + LocalDateTime.now() + "] You can't follow yourself");
				clientOut.writeObject("You can't follow yourself ");
			} else {
				Pair<Boolean, String> result = this.uc.checkFollower(localUser, userCheck);
				// If not an user return that error.
				if (result.second().contains("not found")) {
					System.out.println("[" + LocalDateTime.now() + "] " + userCheck + " is not a user.");
					clientOut.writeObject(userCheck + " is not a user.");
				} else {
					// If localUser doesnt follow then follow.
					if (!result.first()) {
						User user = this.uc.get(userCheck);
						user.addFollower(localUser.username);
						System.out.println("[" + LocalDateTime.now() + "] " + localUser + " now follows " + userCheck);
						clientOut.writeObject(localUser + " now follows " + userCheck);
					} else {
						// Already follows
						System.err.println("[" + LocalDateTime.now() + "] " + "Already follows");
						clientOut.writeObject("Already follows");
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
