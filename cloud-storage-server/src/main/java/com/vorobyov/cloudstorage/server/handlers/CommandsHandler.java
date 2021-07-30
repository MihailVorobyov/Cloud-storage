package com.vorobyov.cloudstorage.server.handlers;

import com.vorobyov.cloudstorage.server.utils.FileProperties;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CommandsHandler extends SimpleChannelInboundHandler<String> {
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	String userName;
	String currentPath;
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		String command = msg
				.replace("\r", "")
				.replace("\n", "");
		logger.info("Command from client: " + command);
		if (command.startsWith("set_user_name ")) {
			ctx.writeAndFlush(setUpUser(command));
		} else if (command.startsWith("ls")) {
			ctx.writeAndFlush(getFilesList());
		} else if (command.startsWith("touch ")) {
			ctx.writeAndFlush(createFile(command)); //TODO убрать currentPath - будет передаваться от клиента
		} else if (command.startsWith("mkdir ")) {
			ctx.writeAndFlush(makeDirectory(command));
		} else if (command.startsWith("cd ")) {
			ctx.writeAndFlush(changeDirectory(command));
		} else if (command.startsWith("rm ")) {
			ctx.writeAndFlush(remove(command, currentPath));
		} else if (command.startsWith("copy ")) {
			ctx.writeAndFlush(copy(command));
		} else if (command.startsWith("cat ")) {
			ctx.writeAndFlush(viewFile(command));
		} else if (command.startsWith("rename ")) {
			ctx.writeAndFlush(rename(command));
		} else if (command.startsWith("move ")) {
			ctx.writeAndFlush(move(command));
//		} else if (command.startsWith("download ")) {
//			ctx.writeAndFlush(download(command));
		} else if (command.startsWith("upload ")) {
			ctx.writeAndFlush(upload(command, ctx));
		} else if (command.startsWith("search ")) {
			ctx.writeAndFlush(search(command.replaceFirst("search ", "")));
		} else if (command.startsWith("disconnect ")) {
			disconnect();
		} else {
			ctx.write(command);
		}
	}
	
	private void disconnect( ) {
	
	}
	
	/**
	 * Метод для загрузка файла с клиента на сервер.
	 * @param command Строка вида "upload путь_к_файлу_на_сервере размер_файла"
	 * @return возвращает содержимое текущей директории
	 */
	private List<FileProperties> upload(String command, ChannelHandlerContext ctx) {
		String[] s = command.split(" ", 3);
		Path filePath = Paths.get("server", s[1]); //TODO перенести в UploadHandler
		long fileSize = Long.parseLong(s[2]);
		
		ctx.pipeline().get(UploadFileHandler.class).setFileToWrite(filePath);
		ctx.pipeline().get(UploadFileHandler.class).setFileSize(fileSize);
		
		ctx.pipeline().write("/upload accepted");
		return getFilesList();
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
	
	private List<FileProperties> move(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);

		String sourcePath = s[1];
		String targetPath = s[2];
		
		if (Files.isDirectory(Paths.get(sourcePath))) {
			moveDirectory(sourcePath, targetPath);
			
		} else {
			String fileName = new File(sourcePath).getName();
			Files.move(Paths.get(currentPath, sourcePath), Paths.get(currentPath, targetPath, fileName));
		}
		return getFilesList();
	}
	
	private void moveDirectory (String src, String trg) {
		Path source = Paths.get(src);
		Path target = Paths.get(trg);
		
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
	
	private List<FileProperties> rename(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);

		String oldName = s[1];
		String newName = s[2];
		
		Files.move(Paths.get(currentPath, oldName), Paths.get(currentPath, newName));
		return getFilesList();
	}
	
	private List<FileProperties> setUpUser(String command) {
		String[] s = command.split(" ", 2);
		userName = s[1];
		currentPath = Paths.get("server", userName).toString(); //TODO currentPath ????
		return getFilesList();
	}
	
	/**
	 * Возвращает содержимое текущей директории
	 * @return Возвращает содержимое текущей директории в виде List<FileProperties>
	 */
	private String getFilesList() {
		String result = "";

		try {
			result = Files.list(Paths.get(currentPath))
				.map(p -> new FileProperties(p.getFileName().toString(),
					getFileExtension(p),
					new File(p.toString()).length(),
					new Date(new File(p.toString()).lastModified())))
				.map(fileProperties -> fileProperties.toString())
				.collect(Collectors.joining("<>"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String getFileExtension(Path path) {
		String pathFile = path.toString();
		int lastIndexOf = pathFile.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
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
	private List<FileProperties> copy(String command) {
		String[] arguments = command.split(" ", 3);
		
		try {
			//TODO пути заключить в кавычки
			Path source = Paths.get(currentPath, arguments[1].trim());
			Path target = Paths.get(currentPath, arguments[2].trim());
			
			if (Files.isRegularFile(source)) { //TODO если файл с таким именем существет...
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); //TODO
			} else if (Files.isDirectory(source)){
				copyDirectory(source, target);
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
	private List<FileProperties> remove(String command, String currentPath) throws IOException {
		Path target;
		
		String[] arguments = command.split(" ", 2);
		
		if ("$root$".equals(currentPath)) {
			target = Paths.get("server", userName);
		} else {
			target = Paths.get(currentPath, arguments[1].trim());
			if (currentPath.equals(target.toString())) {
				logger.warning("Wrong command!");
			}
		}
		
		if (Files.exists(target)) {
			if (Files.isDirectory(target)) {
				Files.walkFileTree(target, new SimpleFileVisitor<Path>() {
					
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return CONTINUE;
					}
				});
			} else {
				Files.deleteIfExists(target);
			}
		}
		return getFilesList();
	}
	
	// изменение текущей директории
	private List<FileProperties> changeDirectory(String command) {
		String[] arguments = command.split(" ", 2);
		String path = arguments[1].trim();
		
		if ("~".equals(path)) {
			this.currentPath = Paths.get("server", userName).toString();
		} else if ("..".equals(path)) {
			if (Paths.get(currentPath).getParent().startsWith(Paths.get("server", userName))) {
				this.currentPath = Paths.get(currentPath).getParent().toString();
			}
		} else {
			if (Paths.get(currentPath, path).normalize().startsWith(Paths.get("server", userName))
						&& Files.isDirectory(Paths.get(currentPath, path))
						&& Files.exists(Paths.get(currentPath, path))) {
				
				this.currentPath = Paths.get(currentPath, path).normalize().toString();
			}
		}
		
		return getFilesList();
	}
	
	// создание файла относительно текущей директории
	private List<FileProperties> createFile(String command) {
		String[] arguments = command.split(" ", 2);
		Path path = Paths.get(currentPath, arguments[1]);
		try {
			if (path.startsWith(Paths.get("server", userName))) {
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
	private List<FileProperties> makeDirectory(String command) {
		String[] arguments = command.split(" ", 2);

		String pathArg = arguments[1].trim();
		Path path = Paths.get(currentPath, pathArg);
		try {
			if (path.startsWith(Paths.get("server", userName).toString())) {
				Files.createDirectories(path);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		}
		return getFilesList();
	}
}
