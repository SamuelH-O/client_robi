package client_robi;

import static java.util.logging.Level.SEVERE;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

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
	private File fic;

    public Button execButton;
    public TextArea commandTextArea;
    public TextArea traceTextArea;
    
    public void initialize() {
    }

    public void execButton_exec(ActionEvent event) {
		String ip;
		Message command;
		Message trace;
		int port;
		if (portTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas de port");
		} else if (ipTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas d'IP");
		} else if (commandTextArea.getText().isEmpty()) {
			errorLabel.setText("Erreur : pas de commande");
		}else {
			errorLabel.setText("");
			ip = ipTextArea.getText();
			port = Integer.parseInt(portTextArea.getText());
			command = new Message("t",commandTextArea.getText());
			try {
				Socket clientSocket = new Socket(ip, port);
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer.println("Connected ?");
				if (reader.readLine().equals("Connected !")) {
					writer.println(Message.toJson(command));
					trace = Message.fromJson(reader.readLine());
					traceTextArea.setText(trace.getMess());
				} else {
					traceTextArea.setText("Erreur : échec connection");
					System.out.println("Erreur : échec connection");
				}
				writer.close();
				reader.close();
				clientSocket.close();
			} catch (UnknownHostException e) {
				errorLabel.setText("Erreur : UnknownHostException");
				e.printStackTrace();
			} catch (IOException e) {
				errorLabel.setText("Erreur : IOException");
				e.printStackTrace();
			}
		}
		System.out.println("execButton_exec");
    	System.out.println("commandTextArea = "+commandTextArea.getText());
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