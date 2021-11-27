package com.example.demo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.pngencoder.PngEncoder;

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

class Message {
	public String name;
	public String yeet;

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
}

@Controller
public class MainController {

	@Autowired
	private SimpMessagingTemplate broker;

	private byte[] latestImage;
	private String type;

	private Predictor<Image, DetectedObjects> predictor;

	public MainController() throws ModelNotFoundException, MalformedModelException, IOException {
		// auto config using spring for this is weird****
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

//	// apply modle
//	@GetMapping(value = "/applyModel", produces = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
//		@ResponseBody
//		public byte[] processImg(@RequestParam("image") MultipartFile multipartFile)
//				throws IOException, TranslateException {
//			byte[] byteArr = multipartFile.getBytes();
//			InputStream is = new ByteArrayInputStream(byteArr);
//			// BufferedImage imgBuff = ImageIO.read(is);
//			Image img = ImageFactory.getInstance().fromInputStream(multipartFile.getInputStream());
//			DetectedObjects objects = predictor.predict(img);
//			img.drawBoundingBoxes(objects);
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			img.save(out, "png");
//			return addWaterMark(out);
//
//		}


//	@MessageMapping("/uploadImg")
//	@SendTo("/topic/ImgReceivers")
//	public String process(String blob) throws InterruptedException {
//		System.out.println(blob.getBytes().length);
//		return "someone sent a blob with - "+blob.getBytes().length;
//	}

	@MessageMapping("/uploadImg")
	@SendTo("/topic/ImgReceivers")
	public synchronized String process(String blob) throws InterruptedException, IOException, TranslateException {
		long start =System.currentTimeMillis();

		System.out.println("size in 1 - "+ blob.getBytes().length);
		
		byte[] byteArr = Base64.getDecoder().decode(blob);
	//	System.out.println("size in 2 - "+ byteArr.length);
		
		

		InputStream is = new ByteArrayInputStream(byteArr);
		Image img = ImageFactory.getInstance().fromInputStream(is);
		DetectedObjects objects = predictor.predict(img);
		img.drawBoundingBoxes(objects);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		img.save(out, "png");
		
		
	//	byte[] data = out.toByteArray();
		//System.out.println("size out 1 - "+ out.toByteArray().length);
		
	//	byte [] res = byteArr;
		byte [] res = addWaterMark(out);
//		byte [] res = out.toByteArray();
//		System.out.println("size out 0 - "+ res.length);

		
		

		String encoded = Base64.getEncoder().encodeToString(res);
		System.out.println("size out 2 - "+ encoded.getBytes().length);
		System.out.println("Time taken  "+ (System.currentTimeMillis() - start) + " ");

		System.out.println("");
		return encoded;
		//return "someone sent a blob with - " + blob.getBytes().length;
	}

	@MessageMapping("/greeting")
	@SendTo("/topic/greetingReceivers")
	public String process(Message msg) throws InterruptedException {
		Thread.sleep(100);
		return msg.name + " says hello";
	}

	
	@RequestMapping("/")
	public ModelAndView home() {
		return new ModelAndView("home");
	}
	
	@RequestMapping("/r")
	public ModelAndView home1() {
		return new ModelAndView("Receiver");
	}
	
	@RequestMapping("/b")
	public ModelAndView home2() {
		return new ModelAndView("Broadcaster");
	}
	
	@RequestMapping("/testWebcam")
	public ModelAndView home3() {
		return new ModelAndView("webcamTest");
	}

	@CrossOrigin(origins = "*", allowedHeaders = "*", originPatterns = "*")
	@RequestMapping("/dataUpload")
	public String dataUpload(@RequestParam(name = "image") MultipartFile blob) throws IOException {
		System.out.println("blob size = " + blob.getSize());
		System.out.println(blob.getContentType());
		type = blob.getContentType();
		// blob.
		if (latestImage != null) {
			synchronized (latestImage) {
				latestImage = Base64.getEncoder().encode(blob.getBytes());
			}
		} else {
			latestImage = Base64.getEncoder().encode(blob.getBytes());
		}

		broker.convertAndSend("/topic/greeting", "blob size = " + blob.getSize());
		return "blob size = " + blob.getSize();
	}


	@RequestMapping(value = "/latestImg2")
	@CrossOrigin(origins = "*", allowedHeaders = "*", originPatterns = "*")
	@ResponseBody
	public ResponseEntity<byte[]> getPreview1(HttpServletResponse response) throws java.nio.file.NoSuchFileException {
		ResponseEntity<byte[]> result = null;
		try {
			byte[] image = latestImage;
			String type = "image/png";
			response.setStatus(HttpStatus.OK.value());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.parseMediaType(type));
			headers.setContentLength(image.length);
			result = new ResponseEntity<byte[]>(image, headers, HttpStatus.OK);
		} catch (Exception e) {
			System.out.println(e);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		return result;
	}

//    @RequestMapping(value = "/latestImg3")
//    @ResponseBody public ResponseEntity<byte[]> getPreview2(HttpServletResponse response) throws java.nio.file.NoSuchFileException {
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("video/x-matroska;codecs=avc1"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=video_%s.%s", 1, "mkv"))
//                .body(latestImage);
//    }

//    @RequestMapping(value = "/{id}/preview3", method = RequestMethod.GET)
//    @ResponseBody public FileSystemResource getPreview3(@PathVariable("id") String id, HttpServletResponse response) {
//        String path = repositoryService.findVideoLocationById(id);
//        return new FileSystemResource(path);
//    }
	
	
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
