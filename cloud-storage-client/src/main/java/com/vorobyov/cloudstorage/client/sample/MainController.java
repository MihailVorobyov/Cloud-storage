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
import java.nio.file.*;
import java.util.*;
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
	private String source;
	private String target;
	private boolean isCutModeEnabled;
	
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
	
	@FXML	TextField searchField;
	
	@FXML
	private void initialize() {
		
		getLocalFileList();
		getServerFileList("ls");
	}
	
	@FXML
	private void serverFileListClicked() {
		selectedTableView = serverFileList;
		setSelectedFileName(serverFileList);
		
		if (System.currentTimeMillis() - lastClickTime < doubleClickTime) {
			getServerFileList("open " + selectedFileName);
		}
		lastClickTime = System.currentTimeMillis();
	}
	
	@FXML
	private void localFileListClicked() { //TODO проблема с прокруткой в новой папке
		selectedTableView = localFileList;
		setSelectedFileName(localFileList);
		setSelectedFileType(localFileList);
		selectedPath = user.getCurrentLocalPath() + File.separator + selectedFileName;
		
		logger.info("selectedPath is " + selectedPath);
		
		if (System.currentTimeMillis() - lastClickTime < doubleClickTime) {
			if ("dir".equals(selectedFileType)) {
				user.setCurrentLocalPath(Paths.get(user.getCurrentLocalPath(), selectedFileName).toString());
				getLocalFileList();
			} else if (".txt".equals(selectedFileType) || ".log".equals(selectedFileType)) {
				try {
					ViewTextFile vtf = new ViewTextFile();
					vtf.showWindow(new File(selectedPath));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		lastClickTime = System.currentTimeMillis();
		setSelectedFileName(null);
	}
	
	private void writeCommand(String s) {
		try {
			logger.warning("Send command: " + s);
			out.write(s.getBytes(StandardCharsets.UTF_8));
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void copy() {
		try {
			if (selectedTableView == serverFileList) {
				from = serverFileList;
				source = selectedFileName;
				writeCommand("copy " + source);
				logger.info(read());
				
			} else if (selectedTableView == localFileList) {
				from = localFileList;
				source = selectedPath;
			}
			
			logger.info("Source = " + source);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void paste() {
		
		logger.warning("from = " + from + " , to = " + selectedTableView);
		
		if (from == serverFileList && selectedTableView == serverFileList) {
			pasteFromServerToServer(source);
		} else if (from == localFileList && selectedTableView == localFileList) {
			pasteFromClientToClient();
		} else if (from == serverFileList && selectedTableView == localFileList) {
			pasteFromServerToClient();
		} else if (from == localFileList && selectedTableView == serverFileList) {
			pasteFromClientToServer();
		}
	}
	
	private void pasteFromServerToServer(String source) {
		
		if (isCutModeEnabled) {
			getServerFileList("move " + source);
			isCutModeEnabled = false;
		} else {
			getServerFileList("paste");
		}
		
	}
	
	private void pasteFromClientToClient() {
		File sourceFile = new File(source);
		target = user.getCurrentLocalPath() + File.separator + sourceFile.getName();
		File targetFile = new File(target);
		
		logger.warning("source is " + sourceFile.getPath());
		logger.warning("target is " + targetFile.getPath());
		
		// cut
		if (isCutModeEnabled) {
			if (!targetFile.equals(sourceFile)) {
				if (sourceFile.isDirectory()) {
					copyDirectory(sourceFile, targetFile);
				} else {
					copyFile(sourceFile, targetFile);
					delete(sourceFile.getPath());
				}
			}
			isCutModeEnabled = false;
			// copy
		} else {
			if (sourceFile.isDirectory()) {
				copyDirectory(sourceFile, targetFile);
			} else {
				copyFile(sourceFile, targetFile);
			}
			logger.info("Past to local directory complete");
		}
		
		getLocalFileList();
	}
	
	private void copyDirectory(File sourceFile, File targetFile) {
		Map<File, File> copyMap = new HashMap<>();
		
		fillCopyMap(copyMap, sourceFile, sourceFile, targetFile);
		
		//TODO сделать проверку свободного пространства
		//
		
		copyMap.forEach((s, t) -> {
			System.out.println(s + " -> " + t);
			boolean a = t.getParentFile().mkdirs();
			logger.warning("mkdirs for " + t + " is " + a);
			if (s.isFile()) {
				copyFile(s, t);
			} else {
				a = t.mkdir();
				logger.warning("mkdirs for " + t + " is " + a);
			}
		} );
		
	}
	
	private void fillCopyMap(Map<File, File> map, File dir, File sourceFile, File targetFile) {
		
		if (map != null && dir != null && sourceFile != null && targetFile != null) {
			File[] fileList = dir.listFiles();
			if (fileList != null) {
				for (File f : fileList) {
					if (f.isFile()) {
						map.put(f, relativize(f, sourceFile, targetFile));
					} else if (f.isDirectory()) {
						if (Objects.requireNonNull(f.listFiles()).length == 0) {
							map.put(f, relativize(f, sourceFile, targetFile));
						} else {
							fillCopyMap(map, f, sourceFile, targetFile);
						}
					}
				}
			}
		}
	}
	
	private File relativize(File file, File sourceDir, File targetDir) {
		String s1 = file.getAbsolutePath();
		String s2 = sourceDir.getAbsolutePath();
		String s3 = targetDir.getAbsolutePath().concat(s1.replace(s2, ""));
		return new File(s3);
	}
	
	private void copyFile(File sourceFile, File targetFile) {
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		File targetFileToCopy;
		
		try {
			if (targetFile.exists()) {
				targetFileToCopy = new File(generateFileName(targetFile));
			} else {
				targetFileToCopy = targetFile;
			}
			targetFileToCopy.createNewFile();
			logger.info("Target file name is " + targetFileToCopy.getAbsolutePath());

			fis = new FileInputStream(sourceFile.getAbsolutePath());
			fos = new FileOutputStream(targetFileToCopy.getAbsolutePath());
			
//			logger.warning("New target file name is " + targetFileToCopy.getAbsolutePath());
			
			ByteBuffer bb = ByteBuffer.allocate(10 * 1024 * 1024);
			int bytesRead;
			int bytesWrite;
			while (fis.available() > 0) {
				bytesRead = fis.getChannel().read(bb);
				logger.info(bytesRead + " bytes was read from " + sourceFile.getAbsolutePath() );
				bb.flip();
//				logger.info("flip buffer");
				bytesWrite = fos.getChannel().write(bb);
				logger.info(bytesWrite + " bytes was write to " + targetFileToCopy.getAbsolutePath());
				
				bb.rewind();
//				logger.info("rewind buffer");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			logger.warning("Problem with paste file from " + source + " to " + target);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (fos != null) {
					fos.close();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String generateFileName(File targetFile) {
		int suffix = 1;
		String[] newNameAndExtension = targetFile.getName().split("\\.");
		String targetPath = targetFile.getAbsolutePath();
		
		while (targetFile.exists()) {
			logger.info("File already exists");
			
			final String copiedFileName = String.format("%s(%d).%s", newNameAndExtension[0], suffix++,
				newNameAndExtension[1]);
			
			if (Arrays.stream(Objects.requireNonNull(new File(user.getCurrentLocalPath()).listFiles()))
				.map(File::getName)
				.noneMatch(n -> n.equals(copiedFileName))
			) {
				targetPath = user.getCurrentLocalPath() + File.separator + copiedFileName;
				break;
			}
		}
		
		return targetPath;
	}
	
	private void pasteFromServerToClient() {
		getServerFileList("download");
		
		if (isCutModeEnabled) {
			getServerFileList("rm " + source);
			isCutModeEnabled = false;
		}
	}
	
	private void pasteFromClientToServer() {
		upload(new File(source).getName()); //TODO если директория
		
		if (isCutModeEnabled) {
			delete(source);
			isCutModeEnabled = false;
		}
	}
	
	@FXML
	private void cut() {
		isCutModeEnabled = true;
		copy();
	}
	
	@FXML
	private void delete() {
		if (selectedTableView == localFileList) {
			delete(selectedPath);
			logger.info("Delete file from client");
			getLocalFileList();
		} else if (selectedTableView == serverFileList) {
			getServerFileList("rm " + selectedFileName);
			logger.info("Delete file from server");
		}
	}
	
	private void delete(String path) {
		File fileToDelete = new File(path);
		if (fileToDelete.exists()) {
			if (fileToDelete.isFile()) {
				boolean d = fileToDelete.delete();
				logger.info("Delete file from client is " + d);
			} else {
				deleteDirectory(fileToDelete);
			}
		}
		getLocalFileList();
	}

	private void deleteDirectory(File source) {
		
		File[] fileList = source.listFiles();
		assert fileList != null;
		Arrays.stream(fileList)
			.forEach(f -> {
				logger.info("Delete " + f.getPath());
				delete(f.getAbsolutePath());
			});
		
		boolean b = source.delete();
		logger.warning(source + " delete is " + b);
	}
	
	@FXML
	void makeDir() throws Exception {
		new InputNameDialog(this);
	}
	
	protected void makeDir(String name) throws IOException {
		if (name != null) {
			if (selectedTableView == localFileList) {
				Files.createDirectory(Paths.get(user.getCurrentLocalPath(), name));
				getLocalFileList();
			} else if (selectedTableView == serverFileList) {
				getServerFileList("mkdir " + name);
			}
		}
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
		if (selectedTableView == localFileList ) {
			upload(selectedFileName);
		}
	}
	
	
	private void upload(String fileName) {

		String localPath = user.getCurrentLocalPath();
		File file = new File(localPath + File.separator + fileName);
		if (file.isFile()) {
			long fileSize = file.length();
			try {
				String command = "upload " + fileName + " " + fileSize;
				writeCommand(command);
				String answer = read();
				if ("/upload accepted".equals(answer)) {
					sendFile(file);
					
					answer = read();
					if (answer.startsWith("/upload complete")) {
						logger.info("answer from server: " + answer);
					} else if ("/upload failed".equals(answer)) {
						logger.info("answer from server: " + answer);
					}
					getServerFileList("ls");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (file.isDirectory()){
			//TODO
		}
	}
	
	private void getLocalFileList() {
		Path currentPath = Paths.get(user.getCurrentLocalPath());
		List<FileProperties> result = new ArrayList<>();
		
		try {
			String[] s = new File(currentPath.toString()).list();
			if (s != null) {
				if (s.length > 0) {
					result = Files.list(currentPath)
						.map(p -> new FileProperties(p.getFileName().toString(),
							getFileExtension(p),
							new File(p.toString()).length(),
							new Date(new File(p.toString()).lastModified())))
						.collect(Collectors.toList());
				}
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
	private void getServerFileList(String command) {
		logger.warning("Command is " + command);
		List<FileProperties> result = new ArrayList<>();
		
		try {
			writeCommand(command);
			String fileList = read();
			if (!" ".equals(fileList)) {
				result = Arrays.stream(fileList.split("<>"))
					.map(s -> s.split(";;"))
					.map(s -> new FileProperties(s[0], s[1], Long.parseLong(s[2]), new Date(Long.parseLong(s[3]))))
					.collect(Collectors.toList());
			}
			logger.info("filelist = " + fileList);
			
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
		if (tv != null) {
			if (tv.getSelectionModel().getSelectedItem() != null) {
				selectedFileName = tv.getSelectionModel().getSelectedItem().getName();
			}
		} else selectedFileName = "";
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
			getLocalFileList();
		}
	}
	
	@FXML
	private void serverListUp() {
		getServerFileList("cd ..");
	}
}
