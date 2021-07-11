package com.vorobyov.cloudstorage.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
	private final String ICON_PATH = "src" + File.separator + "images" + File.separator + "32x32" + File.separator;
	
	private final Socket socket;
	private final DataOutputStream out;
	private final DataInputStream in;
	
	private String[] buttonsNames = {
		"Download", "Upload", "Copy", "Paste", "Cut", "Delete", "Rename", "Search", "Make_dir"
	};
	private JButton[] buttons = new JButton[buttonsNames.length];
	
	private final int WINDOW_BORDER_SIZE = this.getInsets().left;
	private final int WINDOW_HEAD_HEIGHT = this.getInsets().bottom;
	
	private Dimension listPreferredSize = new Dimension(350, 450);
	private Dimension windowSize = new Dimension(2 * WINDOW_BORDER_SIZE + 2 * listPreferredSize.width + 6 * 8 + 32,
		listPreferredSize.height + WINDOW_BORDER_SIZE * 8 + WINDOW_HEAD_HEIGHT + 120);
	
	public Client() throws IOException {
		socket = new Socket("localhost", 5000);
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream(socket.getInputStream());

		// ---------- Frame ----------

		this.setSize(windowSize);
		this.setResizable(false);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// ---------- Меню ----------
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu[] menus = {
				new JMenu("File"),
				new JMenu("Edit"),
				new JMenu("About")
		};
		
		JMenuItem[][] menuItems = {
				{   // File
						new JMenuItem("Exit"),
				},
				{   // Edit
						new JMenuItem("Copy"),
						new JMenuItem("Paste"),
						new JMenuItem("Paste"),
						new JMenuItem("Delete")
				},
				{   // Help
						new JMenuItem("About application")
				}
		};
		
		for (int i = 0; i < menus.length; i++) {
			menuBar.add(menus[i]);
			for (int j = 0; j < menuItems[i].length; j++) {
				menus[i].add(menuItems[i][j]);
			}
		}
		
		// ---------- Панели ----------
		JPanel mainPanel = new JPanel(new FlowLayout());
		
		JPanel serverPanel = new JPanel();
		serverPanel.setBackground(Color.red);
		serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.Y_AXIS));
//		JList serverList = new JList();
//		serverList.setPreferredSize(listPreferredSize);
//		serverList.setListData(new File("server").list());
//		serverPanel.add(serverList);
		
		// Таблица с заголовком
		String[] columns = {"Name", "Type", "Size", "Change date"};
		File dirContent = new File("server");
		File[] files = dirContent.listFiles();
		String[][] serverTableData = new String[dirContent.listFiles().length][columns.length];
		
		for (int i = 0; i < dirContent.list().length; i ++) {
			String[] fileName = files[i].getName().split("\\.", 2);
			serverTableData[i][0] = fileName[0];
			serverTableData[i][1] = fileName[1];
			serverTableData[i][2] = files[i].length() + " bytes";
			
			// TODO: дату создания/изменения хранить в БД
//			serverTableData[i][3] =
		}
		JTable serverFilesTable = new JTable(serverTableData, columns);
		serverFilesTable.getTableHeader().setAlignmentY(Component.TOP_ALIGNMENT);
		serverFilesTable.setRowSelectionAllowed(true);
		serverFilesTable.setShowGrid(false);
		serverFilesTable.getTableHeader().setReorderingAllowed(false);
		serverPanel.add(serverFilesTable.getTableHeader());
		serverPanel.add(serverFilesTable);

		
 		JPanel centralPanel = new JPanel();
 		centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
		centralPanel.setBackground(Color.yellow);
		
		JPanel userPanel = new JPanel();
		userPanel.setBackground(Color.green);
		JList userList = new JList();
		userList.setPreferredSize(listPreferredSize);
		userList.setListData(new File("client").list());
		userPanel.add(userList);
		
		mainPanel.add(serverPanel);
		mainPanel.add(centralPanel);
		mainPanel.add(userPanel);
		
		JPanel southPanel = new JPanel();
		
		// ---------- Кнопки ----------
		
		for (int i = 0; i < buttons.length; i++) {
			buttons[i] = new JButton();
			buttons[i].setMargin(new Insets(0, 0, 0, 0));
			buttons[i].setIcon(new ImageIcon(ICON_PATH + buttonsNames[i] + ".png"));
			centralPanel.add(buttons[i]);
			buttons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
			buttons[i].setAlignmentY(Component.CENTER_ALIGNMENT);
			if (i < buttons.length - 1) {
				centralPanel.add(new JSeparator());
			}
		}
		
		// ---------- Разное ----------
		
		JButton btnSend = new JButton("SEND");
		JTextField textField = new JTextField(20);

		btnSend.addActionListener(a -> {
			// limit: 2 позволяет корректно работать с файлами, содержащими пробелы в названии
			String[] cmd = textField.getText().split(" ", 2);
			
			if ("upload".equals(cmd[0])) {
				upload(cmd[1]);
			} else if ("download".equals(cmd[0])) {
				download(cmd[1]);
			}
		});
		
		this.getContentPane().add(BorderLayout.NORTH, menuBar);
//		this.getContentPane().add(BorderLayout.WEST, serverPanel);
//		this.getContentPane().add(BorderLayout.EAST, userPanel);
		this.getContentPane().add(BorderLayout.CENTER, mainPanel);
		this.getContentPane().add(BorderLayout.SOUTH, southPanel);
		
		southPanel.add(textField);
		southPanel.add(btnSend);
		
		
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				sendMessage("exit");
			}
		});
		
		this.setVisible(true);
	}

	private void download(String filename) {
		try {
			out.writeUTF("download");
			out.writeUTF(filename);
			if ("File not found".equals(in.readUTF())) {
				throw new FileNotFoundException("File not found");
			}

			File file = new File("client" + File.separator + in.readUTF());
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);

			long size = in.readLong();
			byte[] buffer = new byte[8 * 1024];
			long remainingSize = size;

			while (remainingSize != 0) {
				int read = in.read(buffer);
				remainingSize -= read;
				fos.write(buffer, 0, read);
			}

			fos.close();
			String status = null;
			if (size == file.length()) {
				status = "OK";
			} else {
				status = "Error. File was corrupted";
			}

			System.out.println("Downloading status: " + status);
			out.writeUTF(status);
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void upload(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (!file.exists()) {
				throw  new FileNotFoundException();
			}

			long fileLength = file.length();
			FileInputStream fis = new FileInputStream(file);

			out.writeUTF("upload");
			out.writeUTF(filename);
			out.writeLong(fileLength);

			int read = 0;
			byte[] buffer = new byte[8 * 1024];
			while ((read = fis.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			out.flush();

			String status = in.readUTF();
			System.out.println("sending status: " + status);
			fis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendMessage(String message) {
		try {
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
