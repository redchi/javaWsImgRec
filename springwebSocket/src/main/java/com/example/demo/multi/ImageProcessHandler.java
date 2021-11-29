package com.example.demo.multi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

class ImageProcessTask implements Runnable {

	public ImageProcessTask(ImageData data) {

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}

@Component
public class ImageProcessHandler {

	private List<ImageData> allRequests;
	private long latestTaskCompTime;

	private volatile boolean isBusy;

	@Autowired
	private SimpMessagingTemplate broker;

	private Predictor<Image, DetectedObjects> predictor;

	public ImageProcessHandler() throws ModelNotFoundException, MalformedModelException, IOException {
		allRequests = new ArrayList<ImageData>();
		isBusy = false;
		String backbone;
		if ("TensorFlow".equals(Engine.getDefaultEngineName())) {
			backbone = "mobilenet_v2";
		} else {
			backbone = "resnet50";
		}

		Criteria<Image, DetectedObjects> criteria = Criteria.builder().optApplication(Application.CV.OBJECT_DETECTION)
				.setTypes(Image.class, DetectedObjects.class).optFilter("backbone", backbone)
				.optEngine(Engine.getDefaultEngineName()).optProgress(new ProgressBar()).build();
		ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);
		predictor = model.newPredictor();

	}

	@Scheduled(fixedRate = 100)
	public void checkRequests() {
		synchronized (allRequests) {
			int requestMaxSize = 5;
			AtomicInteger count = new AtomicInteger();
			if (allRequests.size() > requestMaxSize) {
				List<ImageData> newlist = allRequests.stream()
						.sorted(Comparator.comparingLong((ImageData img) -> (img.getTimeStamp())).reversed())
						.filter((i) -> {
							if (count.get() < requestMaxSize) {
								count.set(count.get() + 1);
								return true;
							}
							return false;
						}).toList();
				allRequests.clear();
				allRequests = newlist;
			}

		}
	}

	public synchronized void ProcessImage(ImageData imgData) throws IOException, TranslateException {
		isBusy = true;
		long start = System.currentTimeMillis();
		byte[] byteArr = Base64.getDecoder().decode(imgData.getB64Data());
		InputStream is = new ByteArrayInputStream(byteArr);
		Image img = ImageFactory.getInstance().fromInputStream(is);
		DetectedObjects objects = predictor.predict(img);
		img.drawBoundingBoxes(objects);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		img.save(out, "png");
		byte[] res = addWaterMark(out);
		String encoded = Base64.getEncoder().encodeToString(res);
		System.out.println("Time taken  " + (System.currentTimeMillis() - start) + " ");
		broker.convertAndSend("/topic/ImgReceivers", encoded);
		isBusy = false;
	}

	private byte[] addWaterMark(ByteArrayOutputStream out) throws IOException {
		ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());
		BufferedImage image = ImageIO.read(input);
		int height = 50;
		int y = image.getHeight() + height / 2;

		BufferedImage newImage = new BufferedImage(image.getWidth(), (image.getHeight()) + height,
				BufferedImage.TYPE_INT_RGB);
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
	
	public boolean isBusy() {
		return isBusy;
	}
}
