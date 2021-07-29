package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.FileProperties;
import com.vorobyov.cloudstorage.client.utils.Network;
import com.vorobyov.cloudstorage.client.utils.Static;
import com.vorobyov.cloudstorage.client.utils.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MainController {
	Logger logger = Logger.getLogger("com.vorobyov.cloudstorage.client.sample.MainController");
	private final DataInputStream in = Network.getDataInputStream();
	private final DataOutputStream out = Network.getDataOutputStream();
	private final ReadableByteChannel rbc = Network.getRbc();
	private final ByteBuffer byteBuffer = Network.getByteBuffer();
	private FileInputStream fileInputStream;
	private FileOutputStream fileOutputStream;
	
	User user = Static.getUser();
	
	private String selectedFileName;
	
	@FXML	MenuItem closeWindow;
	@FXML	TableView<FileProperties> serverFIleList;
	@FXML	TableColumn<FileProperties, String> serverTableName;
	@FXML	TableColumn<FileProperties, String> serverTableType;
	@FXML	TableColumn<FileProperties, Long> serverTableSize;
	@FXML	TableColumn<FileProperties, Date> serverTableLastModify;
	@FXML	Button downloadButton;
	@FXML	Button uploadButton;
	@FXML	Button copyButton;
	@FXML	Button pasteButton;
	@FXML	Button cutButton;
	@FXML	Button deleteButton;
	@FXML	Button makeDirButton;
	@FXML	Button renameButton;
	@FXML	Button searchButton;
	
	@FXML	TableView<FileProperties> localFIleList;
	@FXML	TableColumn<FileProperties, String> localTableName;
	@FXML	TableColumn<FileProperties, String> localTableType;
	@FXML	TableColumn<FileProperties, Long> localTableSize;
	@FXML	TableColumn<FileProperties, String> localTableLastModify;
	@FXML	TextArea viewTextArea;
	@FXML	TextField searchField;
	
	private void writeCommand(String s) {
		try {
			out.write(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void sendFile(File file) {
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")){
			byte[] bytes = new byte[1024 * 8];
			int read;
			while ((read = raf.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String read() throws IOException {
		int readNumberBytes = rbc.read(byteBuffer);
		String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes))
			.replaceAll("\\n", "").replace("\r", "");
		byteBuffer.clear();
		return serverAnswer;
	}
	
	@FXML
	private void download() {
	
	}
	
	@FXML
	private void upload() {

		String localPath = user.getCurrentLocalPath();
		String serverPath = user.getCurrentServerPath();
		File file = new File(Paths.get(localPath, selectedFileName).toString());
		if (file.isFile()) {
			long fileSize = file.length();
			try {
				String command = "upload " + Paths.get(serverPath, selectedFileName) + " " + fileSize;
				out.write(command.getBytes(StandardCharsets.UTF_8));
				out.flush();
				String answer = read();
				logger.info("answer from server: " + answer);
				if ("/upload accepted".equals(answer)) {
					sendFile(file);
					
					answer = read();
					if (answer.startsWith("/upload complete")) {
						logger.info("answer from server: " + answer);
					} else if ("/upload failed".equals(answer)) {
						logger.info("answer from server: " + answer);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	private void copy() {
	
	}
	
	@FXML
	private void paste() {
	
	}
	
	@FXML
	private void cut() {
	
	}
	
	@FXML
	private void delete() {
	
	}
	
	@FXML
	private void makeDir() {
	
	}
	
	@FXML
	private void rename() {
	
	}
	
	@FXML
	private void search() {
	
	}
	
	@FXML
	public void initialize() {
		getFilesList(Paths.get(user.getCurrentLocalPath()));
	}
	
	private void getFilesList(Path currentPath) {
		List<FileProperties> result = new ArrayList<>();
		
		try {
			String[] s = new File(currentPath.toString()).list();
			if (s != null && s.length > 0) {
				result = Files.list(currentPath)
					.map(p -> new FileProperties(p.getFileName().toString(),
													getFileExtension(p),
													new File(p.toString()).length(),
													new Date(new File(p.toString()).lastModified())))
					.collect(Collectors.toList());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		renewLocalTable(result);
		
	}
	
	private void renewLocalTable(List<FileProperties> fpList) {
	
		ObservableList<FileProperties> observableList = FXCollections.observableArrayList();
		fpList.stream().forEach(fp -> observableList.add(fp));
		
		try {
			localTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
			localTableType.setCellValueFactory(new PropertyValueFactory<>("type"));
			localTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));
			localTableLastModify.setCellValueFactory(new PropertyValueFactory<>("lmDate"));

			localFIleList.setItems(observableList);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private String getFileExtension(Path path) {
		String pathFile = path.toString();
		if (new File(pathFile).isDirectory()) {
			return "dir";
		}
		int lastIndexOf = pathFile.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return pathFile.substring(lastIndexOf);
	}
	
	@FXML
	private void setSelectedFileName() {
		TableView.TableViewSelectionModel<FileProperties> selectionModel = localFIleList.getSelectionModel();
		selectedFileName = selectionModel.getSelectedItem().getName();
	}
}
