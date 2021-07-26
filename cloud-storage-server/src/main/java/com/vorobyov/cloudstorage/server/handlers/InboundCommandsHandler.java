package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

public class InboundCommandsHandler extends SimpleChannelInboundHandler<String> {
	
	Logger logger = Logger.getLogger("server.handlers.InboundCommandsHandler");
	String userName;
	String currentPath;
	String sortBy;
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println("CommandsHandler.channelRead0");
		
		String command = msg
				.replace("\r", "")
				.replace("\n", "");
		System.out.println(command);
		
		if (command.startsWith("set_user_name ")) {
			ctx.pipeline().remove(InboundAuthHandler.class);
			ctx.write(setUpUser(command));
//			setUpUser(command);
			
		} else if (command.startsWith("ls")) {
			ctx.writeAndFlush(getFilesList("ls " + sortBy, currentPath));
		} else if (command.startsWith("touch ")) {
			ctx.writeAndFlush(createFile(command, currentPath)); //TODO убрать currentPath - будет передаваться от клиента
		} else if (command.startsWith("mkdir ")) {
			ctx.writeAndFlush(makeDirectory(command, currentPath));
		} else if (command.startsWith("cd ")) {
			ctx.writeAndFlush(changeDirectory(command, currentPath));
		} else if (command.startsWith("rm ")) {
			ctx.writeAndFlush(remove(command, currentPath));
		} else if (command.startsWith("copy ")) {
			ctx.writeAndFlush(copy(command, currentPath));
		} else if (command.startsWith("cat ")) {
			ctx.writeAndFlush(viewFile(command));
		} else if (command.startsWith("changenick ")) { //TODO: удалить метод
			ctx.writeAndFlush(changeUserName(command));
		} else if (command.startsWith("rename ")) {
			ctx.writeAndFlush(rename(command));
		} else if (command.startsWith("sort ")) {
			ctx.writeAndFlush(getFilesList(command, currentPath));
		} else if (command.startsWith("move ")) {
			ctx.writeAndFlush(move(command));
//		} else if (command.startsWith("download ")) {
//			ctx.writeAndFlush(download(command));
		} else if (command.startsWith("upload ")) {
//			ctx.writeAndFlush(upload(command + " C:\\images.pdf " + Files.size(Paths.get("C:\\images.pdf")), ctx));
			upload(command, ctx);
		} else if (command.startsWith("search ")) {
			ctx.writeAndFlush(search(command.replaceFirst("search ", "")));
		} else if (command.startsWith("disconnect ")) {
			disconnect(command);
		} else {
			ctx.write(command);
		}
	}
	
	private void disconnect(String command) {
	
	}
	
	/**
	 * Метод для загрузка файла с клиента на сервер.
	 * @param command Строка вида "upload путь_к_файлу_на_сервере размер_файла"
	 * @return возвращает содержимое текущей директории
	 */
	private String upload(String command, ChannelHandlerContext context) throws IOException {
		String[] s = command.split(" ", 3);
		String filePath = Paths.get("server", s[1]).toString();
		long fileSize = Long.parseLong(s[2]);
		context.pipeline().addFirst(new InboundUploadFileHandler(filePath, fileSize));
		
		return getFilesList("_ ".concat(sortBy), currentPath);
	}
	
	/**
	 * Метод для поиска файлов и директорий, имена которых содержат указанную последовательность символов
	 * @param charSequence Последовательность символов, которую требуется найти.
	 * @return Возвращает список имён файлов и директорий, содержащих charSequence
	 * @throws IOException
	 */
	private String search(String charSequence) throws IOException {
		List<String> result = null;
		String[] s = new File(currentPath).list();
		
		List<Path> walkResult = new ArrayList<>();
		Files.walkFileTree(Paths.get(currentPath), new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				walkResult.add(dir);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					walkResult.add(file);
				return FileVisitResult.CONTINUE;
			}
			
		});
		
		if (!walkResult.isEmpty()) {
			result = walkResult.stream()
				.filter(r -> r.getFileName().toString().matches("(.*)" + charSequence + "(.*)"))
				.sorted((p1, p2) -> {
					int r = 0;
					if ("name".equals(sortBy)) {
						String name1 = new File(p1.toString()).getName();
						String name2 = new File(p2.toString()).getName();
						r = name1.compareTo(name2);
					} else if ("type".equals(sortBy)) {
						String type1 = getFileExtension(Paths.get(p1.toString()));
						String type2 = getFileExtension(Paths.get(p2.toString()));
						r = type1.compareTo(type2);
					} else if ("size".equals(sortBy)) {
						Long size1 = new File(p1.toString()).length();
						Long size2 = new File(p2.toString()).length();
						r = size1.compareTo(size2);
					} else if ("date".equals(sortBy)) {
						Date date1 = new Date(new File(p1.toString()).lastModified());
						Date date2 = new Date(new File(p2.toString()).lastModified());
						r = date1.compareTo(date2);
					}
					return r;
				})
				.map(p -> new File(p.toString()).getName())
				.collect(Collectors.toList());
		}
		return result.toString();
	}
	
	private String move(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);
		if (s.length < 3) {
			return "Wrong command";
		}
		String sourcePath = s[1];
		String targetPath = s[2];
		
		if (Files.isDirectory(Paths.get(sourcePath))) {
			moveDirectory(sourcePath, targetPath);
			
		} else {
			String fileName = new File(sourcePath).getName();
			Files.move(Paths.get(currentPath, sourcePath), Paths.get(currentPath, targetPath, fileName));
		}
		return getFilesList("_ " + sortBy, currentPath);
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
	
	private String rename(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);
		if (s.length < 3) {
			return "Wrong command";
		}
		String oldName = s[1];
		String newName = s[2];
		
		Files.move(Paths.get(currentPath, oldName), Paths.get(currentPath, newName));
		return getFilesList("_ " + sortBy, currentPath);
	}
	
	private String setUpUser(String command) {
		String[] s = command.split(" ", 2);
		userName = s[1];
		currentPath = Paths.get("server", userName).toString();
		sortBy = "name";
		return getFilesList("_ ".concat(sortBy), currentPath);
	}
	
	/**
	 * Сортирует и возвращает содержимое текущей директории
	 * @param command Команда вида "(ls | sort) (name | size | type | date)")
	 * @param currentPath Текущая_директория
	 * @return Возвращает содержимое текущей директории в виде строки (пример: [file1.txt, file2.txt, dir1, dir2])
	 */
	private String getFilesList(String command, String currentPath) {
		List<String> result = new ArrayList<>();
		String[] params = command.split(" ", 2);
		sortBy = params[1];
		try {
			String[] s = new File(currentPath).list();
			if (s != null && s.length > 0) {
				if ("name".equals(sortBy)) {
					result = Files.list(Paths.get(currentPath))
						.map(p -> new File(p.toString()).getName())
						.sorted().collect(Collectors.toList());
				} else if ("type".equals(sortBy)) {
					result = Files.list(Paths.get(currentPath))
						.sorted(
							(p1, p2) -> {
								String type1 = getFileExtension(p1);
								String type2 = getFileExtension(p2);
								return type1.compareTo(type2);
							})
						.map(p -> new File(p.toString()).getName())
						.collect(Collectors.toList());
				} else if ("size".equals(sortBy)) {
					result = Files.list(Paths.get(currentPath))
						.sorted(
							(p1, p2) -> {
								Long size1 = new File(p1.toString()).length();
								Long size2 = new File(p2.toString()).length();
								return size1.compareTo(size2);
							})
						.map(p -> new File(p.toString()).getName())
						.collect(Collectors.toList()
						);
				} else if ("date".equals(sortBy)) {
					result = Files.list(Paths.get(currentPath))
						.sorted(
							(p1, p2) -> {
								Date date1 = new Date(new File(p1.toString()).lastModified());
								Date date2 = new Date(new File(p2.toString()).lastModified());
								return date1.compareTo(date2);
							})
						.map(p -> new File(p.toString()).getName())
						.collect(Collectors.toList());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString();
	}
	
	private String getFileExtension(Path path) {
		String pathFile = path.toString();
		int lastIndexOf = pathFile.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return pathFile.substring(lastIndexOf);
	}
	
	// Изменение имени пользователя
	private String changeUserName(String command) throws IOException {
		String[] arguments = command.split(" ", 2);
		if (arguments.length < 2) {
			return "";
		}
		String newUserName = arguments[1];
		
		copyDirectory(Paths.get("server", userName), Paths.get("server", newUserName));
		remove("rm " + userName, "$root$");
		userName = newUserName;
		currentPath = Paths.get("server", userName).toString();
		return "Name changed to " + userName;
	}
	
	// Просмотр текстовых файлов
	private String viewFile(String command) throws IOException {
		String[] arguments = command.split(" ", 2);
		if (arguments.length < 2) {
			return "Wrong command!";
		}
		Path filePath = Paths.get(currentPath, arguments[1]);
		if (Files.exists(filePath)) {
			return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
		}
		return "";
	}
	
	// копирование файлов / директории
	private String copy(String command, String currentPath) {
		String[] arguments = command.split(" ", 3);
		if (arguments.length < 3) {
			return "";
		}
		
		try {
			Path source = Paths.get(currentPath, arguments[1].trim());
			Path target = Paths.get(currentPath, arguments[2].trim());
			
			if ("".equals(source.toString()) || "".equals(target.toString())) {
				return "";
			}
			
			if (Files.isRegularFile(source)) {
				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
			} else if (Files.isDirectory(source)){
				copyDirectory(source, target);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return getFilesList("_ ".concat(sortBy), currentPath);
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
	private String remove(String command, String currentPath) throws IOException {
		Path target;
		
		String[] arguments = command.split(" ", 2);
		if (arguments.length < 2) {
			return "Wrong command!";
		}
		
		if ("$root$".equals(currentPath)) {
			target = Paths.get("server", userName);
		} else {
			target = Paths.get(currentPath, arguments[1].trim());
			if (currentPath.equals(target.toString())) {
				return "Wrong command!";
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
		} else {
			return "File / directory not found";
		}
		return getFilesList("_ ".concat(sortBy), currentPath);
	}
	
	// изменение текущей директории
	private String changeDirectory(String command, String currentPath) {
		String msg = "Wrong command";
		String[] arguments = command.split(" ", 2);
		if (arguments.length < 2) {
			return msg;
		}
		String path = arguments[1].trim();
		if ("".equals(path)) {
			return msg;
		}
		
		if ("~".equals(path)) {
			this.currentPath = Paths.get("server", userName).toString();
			msg = getFilesList("_ ".concat(sortBy), this.currentPath);
		} else if ("..".equals(path)) {
			if (Paths.get(currentPath).getParent().startsWith(Paths.get("server", userName))) {
				this.currentPath = Paths.get(currentPath).getParent().toString();
				msg = getFilesList("_ ".concat(sortBy), this.currentPath);
			}
		} else {
			if (Paths.get(currentPath, path).normalize().startsWith(Paths.get("server", userName))
						&& Files.isDirectory(Paths.get(currentPath, path))
						&& Files.exists(Paths.get(currentPath, path))) {
				
				this.currentPath = Paths.get(currentPath, path).normalize().toString();
				msg = getFilesList("_ ".concat(sortBy), this.currentPath);
			} else {
				msg = "Wrong path!\n\r";
			}
		}
		return msg;
	}
	
	// создание файла относительно текущей директории
	private String createFile(String command, String currentPath) {
		String[] arguments = command.split(" ", 2);
		Path path = Paths.get(currentPath, arguments[1]);
		try {
			if (path.startsWith(Paths.get("server", userName))) {
				if (Files.notExists(path)) {
					Files.createFile(path);
				} else {
					return "File already exists!\n\r";
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return getFilesList("_ ".concat(sortBy), currentPath);
	}
	
	/**
	 * Создаёт директорию
	 * @param command строка вида "mkdir currentPath/имя_директории"
	 * @param currentPath
	 * @return
	 */
	private String makeDirectory(String command, String currentPath) {
		String[] arguments = command.split(" ", 2);
		if (arguments.length < 2) {
			return "Wrong path";
		}
		String pathArg = arguments[1].trim();
		Path path = Paths.get(currentPath, pathArg);
		try {
			if (path.startsWith(Paths.get("server", userName).toString())) {
				Files.createDirectories(path);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return getFilesList("_ ".concat(sortBy), currentPath);
	}
}
