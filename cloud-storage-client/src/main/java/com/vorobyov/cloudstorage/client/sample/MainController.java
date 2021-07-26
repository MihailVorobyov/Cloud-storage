package com.vorobyov.cloudstorage.client.sample;

import com.vorobyov.cloudstorage.client.utils.FileProperties;
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
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
	
	private final DataInputStream in = new DataInputStream(Network.getInputStream());
	private final DataOutputStream out = new DataOutputStream(Network.getOutputStream());
	private final ReadableByteChannel rbc = Channels.newChannel(in);
	private final ByteBuffer byteBuffer = ByteBuffer.allocate(8 * 1024);
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	User user = Static.getUser();
	
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
	@FXML	TableColumn<FileProperties, Date> localTableLastModify;
	@FXML	TextArea viewTextArea;
	@FXML	TextField searchField;
	
	private void write(String s) {
		try {
			out.write(s.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void write(Object obj) {
		try {
			oos = new ObjectOutputStream(Network.getOutputStream());
			
			oos.writeObject(obj);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Object read() {
		Object o = null;
		try {
			ois = new ObjectInputStream(Network.getInputStream());
			
			o = ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*
		int readNumberBytes;
		while ((readNumberBytes = rbc.read(byteBuffer)) != -1) {

			String serverAnswer = new String(Arrays.copyOfRange(byteBuffer.array(), 0, readNumberBytes));
			byteBuffer.clear();
		}
		*/
		
		return o;
	}
	
	@FXML
	private void download() {
	
	}
	
	@FXML
	private void upload() {
	
		String localPath = user.getCurrentLocalPath();
		String serverPath = user.getCurrentServerPath();
		//TODO
		String filePath = "";
		File file = new File(Paths.get(localPath, filePath).toString());
		long fileSize = file.length();
		
		try {
			String command = "upload " + Paths.get(serverPath, filePath) + fileSize;
			out.write(command.getBytes(StandardCharsets.UTF_8));
			if ("OK".equals(read())) {
				oos.writeObject(file);
			}
		} catch (IOException e) {
			e.printStackTrace();
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
//					.sorted((p1, p2) -> {
//						String name1 = p1.getFileName().toString();
//						String name2 = p2.getFileName().toString();
//						return name1.compareTo(name2);
//					})
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
//			localFIleList.getColumns().add(localTableName);
			
			localTableType.setCellValueFactory(new PropertyValueFactory<>("type"));
//			localFIleList.getColumns().add(localTableType);
			
			localTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));
//			localFIleList.getColumns().add(localTableSize);
			
			localTableLastModify.setCellValueFactory(new PropertyValueFactory<>("date"));
//			localFIleList.getColumns().add(localTableLastModify);
			
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
}
