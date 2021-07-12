package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CommandsHandler extends SimpleChannelInboundHandler<String> {

	String userName;
	String currentPath;
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		AuthHandler.usersOnline.remove(AuthHandler.users.get(ctx.channel()));
		System.out.println("client disconnected: " + ctx.channel());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		String command = msg
				.replace("\r", "")
				.replace("\n", "");
		System.out.println(command); //TODO
		
		if (command.startsWith("set_user_name ")) {
			setUpUser(command);
		} else if (command.startsWith("ls")) {
			ctx.writeAndFlush(getFilesList("ls name", currentPath)); // TODO: добавить в конец каждой команды вызов ls
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
//		} else if (command.startsWith("move ")) {
//			ctx.writeAndFlush(move(command));
//		} else if (command.startsWith("download ")) {
//			ctx.writeAndFlush(download(command));
//		} else if (command.startsWith("upload ")) {
//			ctx.writeAndFlush(upload(command));
//		} else if (command.startsWith("search ")) {
//			ctx.writeAndFlush(search(command));
		} else {
			ctx.writeAndFlush(msg);
		}
		
		String startOfLine = "!newline!" + currentPath.replaceFirst("server", "") + "> "; //TODO удалить
		ctx.writeAndFlush(startOfLine);
	}
	
	private String rename(String command) throws IOException {
		String[] s = command.trim().split(" ", 3);
		if (s.length < 3) {
			return "Wrong command";
		}
		String oldName = s[1];
		String newName = s[2];
		
		Files.move(Paths.get(oldName), Paths.get(newName));
		return "\n\r";
	}
	
	private void setUpUser(String command) {
		String[] s = command.split(" ", 2);
		userName = s[1];
		currentPath = Paths.get("server", userName).toString();
	}
	
	// Получение списка файлов и папок в текущей директории
	private String getFilesList(String param, String currentPath) throws IOException {
		List<String> result = null;
		String[] params = param.split(" ", 3);
		
		String[] s = new File(currentPath).list();
		if (s != null && s.length > 0) {
			if ("name".equals(params[1])) {
				result = Files.list(Paths.get(currentPath))
					.map(p -> new File(p.toString()).getName())
					.sorted().collect(Collectors.toList());
			} else if ("type".equals(params[1])) {
				result = Files.list(Paths.get(currentPath))
					.sorted(
						(p1, p2) -> {
							String type1 = getFileExtension(p1);
							String type2 = getFileExtension(p2);
							return type1.compareTo(type2);
						})
					.map(p -> new File(p.toString()).getName())
					.collect(Collectors.toList());
			} else if ("size".equals(params[1])) {
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
			} else if ("date".equals(params[1])) {
				result = Files.list(Paths.get(currentPath))
					.sorted(
						(p1, p2) -> {
							Date date1 = new Date(new File(p1.toString()).lastModified());
							Date date2 = new Date(new File(p2.toString()).lastModified());
							return date1.compareTo(date2);
						})
					.map(p -> new File(p.toString()).getName())
					.collect(Collectors.toList()
				);
			}
			return result.toString();
		} else {
			return "";
		}
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
				return "File copied";
			} else if (Files.isDirectory(source)){
				copyDirectory(source, target);
				return "Directory copied";
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return "Something wrong";
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
				
				return Files.exists(target) ? "Something wrong" : "Directory deleted";
			} else {
				return Files.deleteIfExists(target) ? "File deleted" : "Delete filed";
			}
		} else {
			return "File / directory not found";
		}
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
			msg = "";
		} else if ("..".equals(path)) {
			if (Paths.get(currentPath).getParent().startsWith(Paths.get("server", userName))) {
				this.currentPath = Paths.get(currentPath).getParent().toString();
				msg = "";
			}
		} else {
			if (Paths.get(currentPath, path).normalize().startsWith(Paths.get("server", userName))
						&& Files.isDirectory(Paths.get(currentPath, path))
						&& Files.exists(Paths.get(currentPath, path))) {
				
				this.currentPath = Paths.get(currentPath, path).normalize().toString();
				msg = "";
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
		return "";
	}
	
	// создание директории
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
		return "";
	}
}
