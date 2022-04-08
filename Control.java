package client_robi;

import static java.util.logging.Level.SEVERE;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

public class Control {
	public TextArea ipTextArea;
	public TextArea portTextArea;
	public Label errorLabel;
	public Button stopButton;
	private File fic;
    public Button execButton;
    public TextArea commandTextArea;
    public TextArea traceTextArea;
	Socket clientSocket = null;
    
    public void initialize() {
    }

    public void execButton_exec(ActionEvent event) {
		String ip;
		Message command;
		int port;

		if (portTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas de port");
		} else if (ipTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas d'IP");
		} else if (commandTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas de commande");
		}else {
			errorLabel.setText("");
			traceTextArea.setText("");
			ip = ipTextArea.getText();
			port = Integer.parseInt(portTextArea.getText());
			command = new Message("command",commandTextArea.getText());
			try {
				clientSocket = new Socket(ip, port);
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				final Message handshakeGreet = new Message("handshake", "Connected ?");
				final Message handshakeResponse = new Message("handshake", "Connected !");
				writer.println(Message.toJson(handshakeGreet));
				if (reader.readLine().equals(Message.toJson(handshakeResponse))) {
					System.out.println("Handshake successfully executed");

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					writer.println(Message.toJson(command));
					System.out.println(Message.toJson(command));
					stopButton.setDisable(false);

					Thread readerThread = new traceReader(writer, reader);
					readerThread.start();
				} else {
					traceTextArea.setText("Erreur : échec connection");
					writer.close();
					reader.close();
					clientSocket.close();
					System.out.println("Erreur : échec connection");
				}
			} catch (UnknownHostException e) {
				errorLabel.setText("Erreur : UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				errorLabel.setText("Erreur : IOException");
				e.printStackTrace();
			}
		}
		//System.out.println("execButton_exec");
    	//System.out.println("commandTextArea :\n" + commandTextArea.getText() + '\n');
		//System.out.println("traceTextArea :\n"+traceTextArea.getText() + '\n');
    }

	public class traceReader extends Thread{
		BufferedReader reader;
		PrintWriter writer;

		public traceReader(PrintWriter writer, BufferedReader reader) {
			this.reader = reader;
			this.writer = writer;
		}

		@Override
		public void run() {
			Message traceLine;
			while (true) {
				try {
					String line;
					StringBuilder traceStringBuilder = new StringBuilder();
					while ((line = reader.readLine()) != null) {
						if (line.endsWith("}")) {
							traceStringBuilder.append(line);
							traceLine = Message.fromJson(traceStringBuilder.toString());
							System.out.println(Message.toJson(traceLine));
							if (traceLine.getType().equals("trace")) {
								traceTextArea.setText(traceTextArea.getText() + traceLine.getMess());
							} else if (traceLine.getType().equals("commandDone")) {
								traceTextArea.setText(traceTextArea.getText() + traceLine.getMess());
								stopButton.setDisable(true);
								writer.close();
								reader.close();
								clientSocket.close();
								return;
							}
							traceStringBuilder = new StringBuilder();
						} else {
							traceStringBuilder.append(line).append("\n");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}

	public void stopButton_exec(ActionEvent actionEvent) {
		if (!clientSocket.isClosed()) {
			try {
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
				writer.println(Message.toJson(new Message("stopCommand", "")));
				System.out.println(Message.toJson(new Message("stopCommand", "")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public void openFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        //only allow text files to be selected using chooser
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
        );

        //set initial directory somewhere user will recognise
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        //let user select file
        File fileToLoad = fileChooser.showOpenDialog(null);

        if (fileToLoad != null) {
            System.out.println("file = "+fileToLoad.getPath());
        }
        else {
            System.out.println("file = null");
        }
    }
    public void saveFile(ActionEvent event) {
    }

	public void exit(ActionEvent actionEvent) {
		Platform.exit();
	}
}