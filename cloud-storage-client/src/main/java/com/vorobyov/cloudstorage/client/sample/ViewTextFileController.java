package com.vorobyov.cloudstorage.client.sample;


import javafx.fxml.FXML;
import javafx.scene.chart.BubbleChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

public class ViewTextFileController {
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	protected Stage window;
	protected File textFile;
	
	@FXML   Label fileNameLabel;
	@FXML	TextArea text;
	@FXML	Button closeButton;
	
	protected void init() {
		logger.info("start");
		text.setWrapText(true);
		RandomAccessFile raf = null;
		
		try {
			raf = new RandomAccessFile(textFile, "r");
			String line;
			
			while ((line = raf.readLine()) != null) {
				text.appendText(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@FXML
	public void cancel() {
		text.clear();
		window.hide();
	}
}
