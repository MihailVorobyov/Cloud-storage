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
import java.nio.channels.ReadableByteChannel;
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
	Logger logger = Logger.getLogger(this.getClass().getName());
	private final DataOutputStream out = Network.getDataOutputStream();
	private final ReadableByteChannel rbc = Network.getRbc();
	private final ByteBuffer byteBuffer = Network.getByteBuffer();
	private final long doubleClickTime = 300;
	private long lastClickTime;
	
	User user = Static.getUser();
	
	private String selectedFileName;
	private String selectedPath;
	private String selectedFileType;
	private TableView<FileProperties> selectedTableView;
	private TableView<FileProperties> from;
	
	@FXML	MenuItem closeWindow;
	@FXML	TableView<FileProperties> serverFileList;
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
	@FXML   Button serverUpButton;
	@FXML   Button localUpButton;
	
	@FXML	TableView<FileProperties> localFileList;
	@FXML	TableColumn<FileProperties, String> localTableName;
	@FXML	TableColumn<FileProperties, String> localTableType;
	@FXML	TableColumn<FileProperties, Long> localTableSize;
	@FXML	TableColumn<FileProperties, String> localTableLastModify;
	
	@FXML	TextArea viewTextArea;
	@FXML	TextField searchField;
	
	@FXML
	private void initialize() {
		
		getLocalFileList();
		getServerFileList("ls");
	}
	
	@FXML
	private void setServerSelectedFileName() {
		selectedTableView = serverFileList;
		setSelectedFileName(serverFileList);
		
		if (System.currentTimeMillis() - lastClickTime < doubleClickTime) {
			getServerFileList("open " + selectedFileName);
		}
		lastClickTime = System.currentTimeMillis();
	}
	
	@FXML
	private void setClientSelectedFileName() {
		selectedTableView = localFileList;
		setSelectedFileName(localFileList);
		setSelectedFileType(localFileList);
		
		if (System.currentTimeMillis() - lastClickTime < doubleClickTime) {
			if ("dir".equals(selectedFileType)) {
				user.setCurrentLocalPath(Paths.get(user.getCurrentLocalPath(), selectedFileName).toString());
				getLocalFileList();
			}
		}
		lastClickTime = System.currentTimeMillis();
	}
	
	private void writeCommand(String s) {
		try {
			out.write(s.getBytes(StandardCharsets.UTF_8));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void copy() {
		try {
			if (selectedTableView.equals(serverFileList)) {
				
				writeCommand("copy " + selectedFileName);
				read();
				
			} else if (selectedTableView.equals(localFileList)) {
				
				selectedPath = Paths.get(user.getCurrentLocalPath(), selectedFileName).toString();
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void paste() {
		if (from.equals(serverFileList) && selectedTableView.equals(serverFileList)) {
			getServerFileList("paste " + selectedFileName);
		} else if (from.equals(localFileList) && selectedTableView.equals(localFileList)) {
			String copiedFileName = new File(selectedPath).getName();
			String tempName = copiedFileName;
			int suffix = 1;
			String finalCopiedFileName = copiedFileName;
			while (Arrays.stream(new File(user.getCurrentLocalPath()).listFiles())
				.map(f -> f.getName())
				.anyMatch(n -> n.equals(finalCopiedFileName))) {
				tempName = copiedFileName + suffix++;
			}
			copiedFileName = tempName;
			new File(user.getCurrentLocalPath() + File.separator + copiedFileName);
		}
		// TODO ещё 2 проверки
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
		logger.info("server answer: " + serverAnswer);
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
				if ("/upload accepted".equals(answer)) {
					sendFile(file);
					
					answer = read();
					if (answer.startsWith("/upload complete")) {
//						logger.info("answer from server: " + answer);
					} else if ("/upload failed".equals(answer)) {
//						logger.info("answer from server: " + answer);
					}
					getServerFileList();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getLocalFileList() {
		Path currentPath = Paths.get(user.getCurrentLocalPath());
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
		observableList.addAll(fpList);
		
		try {
			localTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
			localTableType.setCellValueFactory(new PropertyValueFactory<>("type"));
			localTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));
			localTableLastModify.setCellValueFactory(new PropertyValueFactory<>("lmDate"));

			localFileList.setItems(observableList);
			
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Метод преобразует список файлов, полученный от сервера,
	 * из String в List<FileProperties>. Разделителем между файлами
	 * служит последовательность "<>", а между свойствами файла - ";;"
	 */
	private void getServerFileList() {
		List<FileProperties> result = new ArrayList<>();
		
		try {
			writeCommand("setCurrentPath " + user.getCurrentServerPath());
			logger.info(read());
			
			writeCommand("ls");
			String fileList = read();
			if (!" ".equals(fileList)) {
				logger.info("filelist = " + fileList);
				result = Arrays.stream(fileList.split("<>"))
					.map(s -> s.split(";;"))
					.map(s -> new FileProperties(s[0], s[1], Long.parseLong(s[2]), new Date(Long.parseLong(s[3]))))
					.collect(Collectors.toList());
			}
			
			renewServerTable(result);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private void getServerFileList(String fileList) {
//		List<FileProperties> result = new ArrayList<>();
//
//		if (!" ".equals(fileList)) {
//			logger.info("filelist = " + fileList);
//			result = Arrays.stream(fileList.split("<>"))
//				.map(s -> s.split(";;"))
//				.map(s -> new FileProperties(s[0], s[1], Long.parseLong(s[2]), new Date(Long.parseLong(s[3]))))
//				.collect(Collectors.toList());
//		}
//		renewServerTable(result);
//	}
	
	private void getServerFileList(String command) {
		List<FileProperties> result = new ArrayList<>();
		
		try {
			writeCommand(command);
			String fileList = read();
			if (!" ".equals(fileList)) {
				logger.info("filelist = " + fileList);
				result = Arrays.stream(fileList.split("<>"))
					.map(s -> s.split(";;"))
					.map(s -> new FileProperties(s[0], s[1], Long.parseLong(s[2]), new Date(Long.parseLong(s[3]))))
					.collect(Collectors.toList());
			}
			
			renewServerTable(result);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void renewServerTable(List<FileProperties> fpList) {
		
		ObservableList<FileProperties> observableList = FXCollections.observableArrayList();
		observableList.addAll(fpList);
		
		try {
			serverTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
			serverTableType.setCellValueFactory(new PropertyValueFactory<>("type"));
			serverTableSize.setCellValueFactory(new PropertyValueFactory<>("size"));
			serverTableLastModify.setCellValueFactory(new PropertyValueFactory<>("lmDate"));
			
			serverFileList.setItems(observableList);
			
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
	
	private void setSelectedFileName(TableView<FileProperties> tv) {
		if (tv.getSelectionModel().getSelectedItem() != null) {
			selectedFileName = tv.getSelectionModel().getSelectedItem().getName();
		}
	}
	private void setSelectedFileType(TableView<FileProperties> tv) {
		if (tv.getSelectionModel().getSelectedItem() != null) {
			selectedFileType = tv.getSelectionModel().getSelectedItem().getType();
		}
	}
	
	@FXML
	private void localListUp() {
		if (Paths.get(user.getCurrentLocalPath()).getParent() != null) {
			user.setCurrentLocalPath(Paths.get(user.getCurrentLocalPath()).getParent().toString());
			logger.info("Go to parent");
			getLocalFileList();
		}
		logger.info("no changes");
	}
	
	@FXML
	private void serverListUp() {
		writeCommand("cd ..");
		try {
			getServerFileList(read());
		} catch (IOException e) {
			e.printStackTrace();
		}
//		if (Paths.get(user.getCurrentServerPath()).getParent() != null) {
//			user.setCurrentServerPath(Paths.get(user.getCurrentServerPath()).getParent().toString());
//
//		}
	}
	
}
