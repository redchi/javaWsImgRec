package com.example.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.Classifications.Classification;
import ai.djl.modality.cv.*;
import ai.djl.modality.cv.output.*;
import ai.djl.modality.cv.output.DetectedObjects.DetectedObject;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.SingleShotDetectionTranslator;
import ai.djl.modality.cv.util.*;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.*;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class OD2 {

	
	public static void main(String[] args) throws ModelNotFoundException, MalformedModelException, TranslateException, IOException {
		t1();
	}
	
	public static void t1() throws TranslateException, IOException, ModelNotFoundException, MalformedModelException {
		System.out.println("#1 " +System.currentTimeMillis());
		Path imageFile = Paths.get("src/test/resources/dog_bike_car.jpg");
        Image img = ImageFactory.getInstance().fromFile(imageFile);
        System.out.println("#2 " +System.currentTimeMillis());
		Translator<Image, DetectedObjects> translator = SingleShotDetectionTranslator.builder()
	            .addTransform(new ToTensor())
	            .optSynsetUrl("https://mysynset.txt")
	            .build();
		
        System.out.println("#3 "  +System.currentTimeMillis());

		Criteria<Image,DetectedObjects> crit = Criteria.builder()
				.setTypes(Image.class, DetectedObjects.class)
				.optApplication(Application.CV.OBJECT_DETECTION)
				  .optArtifactId("ssd")
				    .optProgress(new ProgressBar())
				.build();
        System.out.println("#4 " +System.currentTimeMillis());

		ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(crit);
		Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        System.out.println("#4 " +System.currentTimeMillis());
		DetectedObjects objects =  predictor.predict(img);
		for(Classification object:objects.items()) {
			System.out.println(object);
		}
		System.out.println("#5 " +System.currentTimeMillis());

		String type ="png";
		
		img.drawBoundingBoxes(objects);
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		img.save(out1, type);
		
		System.out.println("#6 " +System.currentTimeMillis());

		System.out.println(System.currentTimeMillis());
		InputStream in = new ByteArrayInputStream(out1.toByteArray());
		//BufferedImage newBi = ImageIO.read(in);
	//	ImageIO.write(newBi, type, new File("src/test/resources/test1."+type) );
		
	
		//File input = new File("src/test/resources/test1."+type);
        File output = new File("src/test/resources/test1.jpg");

        BufferedImage image = ImageIO.read(in);
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        result.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
        ImageIO.write(result, "jpg", output);
		
		System.out.println("#7 " +System.currentTimeMillis());
		System.out.println("here");
	}
	
	
	
	private byte[] addWaterMark(ByteArrayOutputStream out) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
		BufferedImage image = ImageIO.read(input);
		int height = 50;
		int y = image.getHeight() + height / 2;

		BufferedImage newImage = new BufferedImage(image.getWidth(), (image.getHeight()) + height, BufferedImage.TYPE_INT_RGB);
		Graphics g = newImage.getGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, image.getWidth(), image.getHeight() + height);
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);

		String pattern = "dd-MM-yyy HH:mm:ss z";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		String date = simpleDateFormat.format(new Date());

		g.setColor(Color.white);
		g.setFont(g.getFont().deriveFont(16f));
		g.drawString("By Asim Younas ^_^   Date - " + date, 3, y);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(newImage, "jpg", baos);
		byte[] bytes = baos.toByteArray();
		return bytes;
	}
	
}
