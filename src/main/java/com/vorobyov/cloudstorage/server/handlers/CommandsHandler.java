package com.vorobyov.cloudstorage.server.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import static java.nio.file.FileVisitResult.CONTINUE;

public class CommandsHandler extends SimpleChannelInboundHandler<String> {
	private static final String[] commandList = {
		"\tls         view all files from current directory",
		"\ttouch      create new file",
		"\tmkdir      create new directory",
		"\tcd         (path | ~ | ..) change current directory to path, to root or one level up",
		"\trm         (filename / dir_name) remove file / directory",
		"\tcopy       (src) (target) copy file or directory from src path to target path",
		"\tcat        (filename) view text file",
		"\tchangenick (nickname) change user's nickname",
		"\n\r"
	}; // TODO
	
	String userName = "User1";
	String currentPath = Paths.get("server", userName).toString();
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("client connected: " + ctx.channel());
		if (Files.exists(Paths.get(userName))) {
			Files.createDirectories(Paths.get(userName));
		}
		ctx.read().flush();
		ctx.writeAndFlush("Hello, " + userName + "!");
		ctx.writeAndFlush("Enter --help for support info");
	}
	
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("client disconnected: " + ctx.channel());
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		
		String command = msg
				.replace("\r", "")
				.replace("\n", "");
		System.out.println(command); //TODO
		
		if ("--help".equals(command)) {
			for (String c : commandList) {
				ctx.writeAndFlush(c);
			}
		} else if ("ls".equals(command)) {
			ctx.writeAndFlush(getFilesList(currentPath));
		} else if (command.startsWith("touch ")) {
			ctx.writeAndFlush(createFile(command, currentPath));
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
		} else if (command.startsWith("changenick ")) {
			ctx.writeAndFlush(changeUserName(command));
		}
		
		String startOfLine = "!newline!" + currentPath.replaceFirst("server", "") + "> ";
		ctx.writeAndFlush(startOfLine);
	}
	
	// Получение списка файлов и папок в текущей директории
	private String getFilesList(String currentPath) {
		String[] servers = new File(currentPath).list();
		if (servers != null && servers.length > 0) {
			Arrays.sort(servers);
			return String.join(" ", servers).concat("\n\r");
		} else {
			return "\n\r";
		}
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
