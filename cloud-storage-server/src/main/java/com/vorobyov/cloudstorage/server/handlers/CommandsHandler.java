package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.FileProperties;
import com.vorobyov.cloudstorage.server.utils.UserRegistration;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CommandsHandler extends SimpleChannelInboundHandler<String> {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	String userName;
	String currentPath;
	Path from;
	Path to;
	private final Path storageRoot = Paths.get("cloud-storage-server/userDataStorage");
	private final Path storageRecycleBin = storageRoot.resolve("recycleBin");
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
	}
	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		logger.info("Client " + ctx.channel().remoteAddress() + " disconnected.");
		clearRecycleBin();
		UserRegistration.usersOnline.remove(UserRegistration.addressUser.get(ctx.channel().remoteAddress()));
		UserRegistration.addressUser.remove(ctx.channel().remoteAddress());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		logger.info("Command from client: " + msg);
		
		String command = msg
				.replace("\r", "")
				.replace("\n", "");
		if (command.startsWith("setCurrentPath ")) {
			ctx.fireChannelRead(setCurrentPath(command));
		} else if (command.startsWith("setUserName ")) {
			userName = command.replaceFirst("setUserName ", "");
			currentPath = storageRoot.resolve(userName).toString();
		} else if (command.startsWith("ls")) {
			ctx.fireChannelRead(getFilesList());
		} else if (command.startsWith("open ")) {
			ctx.fireChannelRead(open(command));
		} else if (command.startsWith("touch ")) {
			ctx.fireChannelRead(createFile(command)); //TODO убрать currentPath - будет передаваться от клиента
		} else if (command.startsWith("mkdir ")) {
			ctx.fireChannelRead(makeDirectory(command));
		} else if (command.startsWith("cd ")) {
			ctx.fireChannelRead(changeDirectory(command));
		} else if (command.startsWith("rm ")) {
			ctx.fireChannelRead(remove(command));
		} else if (command.startsWith("copy ")) {
			ctx.fireChannelRead(copy(command));
		}else if (command.startsWith("paste ")) {
			ctx.fireChannelRead(paste());
		} else if (command.startsWith("cat ")) {
			ctx.fireChannelRead(viewFile(command));
		} else if (command.startsWith("rename ")) {
			ctx.fireChannelRead(rename(command));
		} else if (command.startsWith("move ")) {
			ctx.fireChannelRead(move(command));
//		} else if (command.equals("download")) {
//			ctx.fireChannelRead(download(command));
		} else if (command.startsWith("upload ")) {
			ctx.fireChannelRead(upload(command, ctx));
		} else if (command.startsWith("search ")) {
			ctx.fireChannelRead(search(command.replaceFirst("search ", "")));
		} else if (command.startsWith("disconnect ")) {
			disconnect();
		} else {
			ctx.fireChannelRead(command);
		}
	}
	

	private String open(String command) {
		String name = command.replaceFirst("open ", "");
		Path p = Paths.get(currentPath, name);
		if (Files.isDirectory(p)) {
			
			setCurrentPath("setCurrentPath " + p);
			logger.info("Current path is set to " + p);
			
		} else {
			
			// send file
			
		}
		return getFilesList();
	}
	
	private void disconnect( ) {
	
	}
	
	/**
	 * Метод для загрузки файла с клиента на сервер.
	 * @param command Строка вида "upload путь_к_файлу_на_сервере размер_файла"
	 * @return возвращает содержимое текущей директории
	 */
	private String upload(String command, ChannelHandlerContext ctx) {
		String[] s = command.split(" ", 3);
		Path filePath = Paths.get(currentPath, s[1]);
		long fileSize = Long.parseLong(s[2]);
		
		ctx.pipeline().get(UploadFileHandler.class).setFileToWrite(filePath);
		ctx.pipeline().get(UploadFileHandler.class).setFileSize(fileSize);
		ctx.pipeline().get(ByteBufToByteArrayHandler.class).expectData();
		
		return "/upload accepted";
	}
	
	/**
	 * Метод для поиска файлов и директорий, имена которых содержат указанную последовательность символов
	 * @param charSequence Последовательность символов, которую требуется найти.
	 * @return Возвращает список файлов и директорий, содержащих charSequence
	 */
	private List<FileProperties> search(String charSequence) throws IOException {
		List<FileProperties> result = null;
		
		List<Path> walkResult = new ArrayList<>();
		Files.walkFileTree(Paths.get(currentPath), new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
				walkResult.add(dir);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					walkResult.add(file);
				return FileVisitResult.CONTINUE;
			}
			
		});
		
		if (!walkResult.isEmpty()) {
			result = walkResult.stream()
				.filter(r -> r.getFileName().toString().matches("(.*)" + charSequence + "(.*)"))
				.map(p -> new FileProperties(p.getFileName().toString(),
					getFileExtension(p),
					new File(p.toString()).length(),
					new Date(new File(p.toString()).lastModified())))
				.collect(Collectors.toList());
		}
		return result;
	}
	
	/**
	 *
	 * @param command Команда формата "move source_path target_path"
	 * @return Возвращает список файлов текущей директории
	 * @throws IOException выбрасывает исключение
	 */
	private String move(String command) throws IOException {
		String s = command.trim().split(" ")[1];

		Path sourcePath = from;
		Path targetPath = Paths.get(currentPath, s);
		
		if (Files.isDirectory(sourcePath)) {
			moveDirectory(sourcePath, targetPath);
			
		} else {
			Files.move(sourcePath, targetPath);
		}
		return getFilesList();
	}
	
	private void moveDirectory (Path source, Path target) {
		
		try {
			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Path targetDir = target.resolve(source.relativize(dir));
					try {
						Files.createDirectories(targetDir);
					} catch (FileAlreadyExistsException e) {
						if (!Files.isDirectory(targetDir))
							throw e;
					}
					return CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.move(file, target.resolve(source.relativize(file)));
					return CONTINUE;
				}
				
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.deleteIfExists(dir);
					return CONTINUE;
				}
			});
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private String rename(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);

		String oldName = s[1];
		String newName = s[2];
		
		Files.move(Paths.get(currentPath, oldName), Paths.get(currentPath, newName));
		return getFilesList();
	}
	
	private String setCurrentPath(String command) {
		String[] s = command.split(" ", 2);
		String path = s[1];
		if (path.startsWith(storageRoot.resolve(userName).toString())) {
			currentPath = path;
		} else {
			currentPath = storageRoot.resolve(path).toString(); //TODO currentPath ????
		}
		logger.info("current path is " + currentPath);
		
		return "OK"; //TODO ??????????
	}
	
	/**
	 * Возвращает содержимое текущей директории
	 * @return Возвращает содержимое текущей директории в виде List<FileProperties>
	 */
	private String getFilesList() {
		String result = " ";
		
		try {
			if (Files.list(Paths.get(currentPath)).count() != 0) {
				result = Files.list(Paths.get(currentPath))
					.map(p -> new FileProperties(p.getFileName().toString(),
						getFileExtension(p),
						new File(p.toString()).length(),
						new Date(new File(p.toString()).lastModified())))
					.map(FileProperties::toString)
					.collect(Collectors.joining("<>"));
			} else {
				result = " ";
			}
			
//			logger.info("File list:  " + result);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String getFileExtension(Path path) {
		String pathFile = path.toString();
		int lastIndexOf = pathFile.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "dir";
		}
		return pathFile.substring(lastIndexOf);
	}
	
	// Просмотр текстовых файлов
	//TODO сделать на клиенте + отправка на клиент
	private String viewFile(String command) throws IOException {
		String[] arguments = command.split(" ", 2);

		Path filePath = Paths.get(currentPath, arguments[1]);
		if (Files.exists(filePath)) {
			return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
		}
		return "";
	}
	
	// копирование файлов / директории
	private String copy(String command) {
		String fileToCopyName = command.split(" ", 2)[1];
		from = Paths.get(currentPath, fileToCopyName);
		logger.info("file " + from + " copied");
		return "file copied";
	}
	
	private String paste() {
		
		try {
			Path to = Paths.get(currentPath + from.getFileName());
			
			if (Files.isDirectory(from)){
				copyDirectory(from, to);
				logger.info("Directory pasted");
			} else { //TODO если файл с таким именем существует...
				
				Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES); //TODO
				logger.info("File pasted");
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
		return getFilesList();
	}
	
	// копирование директории
	private void copyDirectory(Path source, Path target) {
		try {
			Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Path targetDir = target.resolve(source.relativize(dir));
					try {
						Files.copy(dir, targetDir);
					} catch (FileAlreadyExistsException e) {
						if (!Files.isDirectory(targetDir))
							throw e;
					}
					return CONTINUE;
				}
				
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.copy(file, target.resolve(source.relativize(file)));
					return CONTINUE;
				}
			});
		} catch(IOException e){
			e.printStackTrace();
		}
	}
	
	// Удаление файла / директории
	private String remove(String command) {

		String name = command.split(" ", 2)[1];

		Path start = Paths.get(currentPath, name);
		if (Files.exists(start)) {
			logger.warning("Start deleting " + start);
		}

		try {

			if (Files.isDirectory(start)) {

				Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						if (Files.exists(file)) {
							logger.warning("File " + file + " not deleted!!!");
						}
						return CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

						if (exc == null) {

							Path moveTo = storageRecycleBin.resolve(userName).resolve(dir.getFileName());
							Files.createDirectories(moveTo.getParent());
//							if (!Files.exists(moveTo)){
								Files.move(dir, moveTo, StandardCopyOption.REPLACE_EXISTING);
//							}
							Files.delete(moveTo);

							return CONTINUE;
						} else {
							throw exc;
						}
					}
				});

			} else {
				Files.delete(start);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return getFilesList();
	}
	
	private String clearRecycleBin() {

		Path start = storageRecycleBin.resolve(userName);
		
		if (Files.exists(start)) {
			try {
				Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
					
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						
						if (Files.exists(file)) {
							logger.warning("File " + file + " not deleted!!!");
						}
						return CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						
						if (exc == null) {
							
							Path toBeDeleted = dir.getParent().resolve("_tobedeleted");
							Files.move(dir, toBeDeleted);
							Files.delete(toBeDeleted);
							if (Files.exists(toBeDeleted)) {
								logger.warning("File " + dir + " not deleted!!!");
							}
							return CONTINUE;
						} else {
							throw exc;
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return getFilesList();
	}
	
	// изменение текущей директории
	private String changeDirectory(String command) {
		String[] arguments = command.split(" ", 2);
		String path = arguments[1].trim();
		
		if ("~".equals(path)) {
			currentPath = storageRoot.resolve(userName).toString();
		} else if ("..".equals(path)) {
			if (Paths.get(currentPath).getParent().startsWith(storageRoot.resolve(userName))) {
				currentPath = Paths.get(currentPath).getParent().toString();
			}
		} else {
			if (Paths.get(currentPath, path).normalize().startsWith(storageRoot.resolve(userName))
						&& Files.isDirectory(Paths.get(currentPath, path))
						&& Files.exists(Paths.get(currentPath, path))) {

				currentPath = Paths.get(path).normalize().toString();
			}
		}
		return getFilesList();
	}
	
	// создание файла относительно текущей директории
	private String createFile(String command) {
		String[] arguments = command.split(" ", 2);
		Path path = Paths.get(currentPath, arguments[1]);
		try {
			if (path.startsWith(storageRoot.resolve(userName))) {
				if (Files.notExists(path)) {
					Files.createFile(path);
					
				} else {
//					return "File already exists!\n\r"; //TODO
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return getFilesList();
	}
	
	/**
	 * Создаёт директорию
	 * @param command строка вида "mkdir currentPath/имя_директории"
	 * @return List<FileProperties>
	 */
	private String makeDirectory(String command) {
		String[] arguments = command.split(" ", 2);
		String pathArg = arguments[1].trim();
		Path path = Paths.get(currentPath, pathArg);
		try {
			if (path.startsWith(storageRoot.resolve(userName).toString())) {
				Files.createDirectories(path);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
		return getFilesList();
	}
}
