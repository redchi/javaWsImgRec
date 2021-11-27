<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.2/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>

    <title>Document</title>
  </head>
  <body>
      <h2>sending data yo</h2>
        <video></video>
      <br>
      <button onclick='init();'>start webcam</button>
    <br>
      <br>
    <button id = "startB"disabled>take a snapshot</button>
  
</br>
</br>
<button onclick='sockJsConnect();'>SockJs Connect</button>
</br>

  </body>
  <script>
    const url = "http://localhost:8080";
    const vid = document.querySelector("video");
    const fps = 2;

    function init(){
    navigator.mediaDevices
      .getUserMedia({ video: true }) // request cam
      .then((stream) => {
        vid.srcObject = stream; // don't use createObjectURL(MediaStream)
        return vid.play(); // returns a Promise
      })
      .then(() => {
        // enable the button
        const btn = document.getElementById("startB");
        btn.disabled = false;
        btn.onclick = (e) => {
          setInterval(() => {
            takeASnap().then(download);
          }, 1000 / fps);
        };
      });
    }


    function takeASnap() {
      const canvas = document.createElement("canvas"); // create a canvas
      const ctx = canvas.getContext("2d"); // get its context
      canvas.width = vid.videoWidth; // set its size to the one of the video
      canvas.height = vid.videoHeight;
      ctx.drawImage(vid, 0, 0); // the video
      return new Promise((res, rej) => {
        canvas.toBlob(res, "image/jpeg"); // request a Blob from the canvas
      });
    }
    function download(blob) {
      // uses the <a download> to download a Blob
      console.log(blob);
      //   let a = document.createElement("a");
      //   a.href = URL.createObjectURL(blob);
      //   a.download = "screenshot.jpg";
      //   document.body.appendChild(a);
      //   a.click();

    //   var fd = new FormData();
    //   fd.append("image", blob);
    //   console.log(fd);
    //   $.ajax({
    //     url: url + "/dataUpload",
    //     data: fd,
    //     processData: false,
    //     contentType: false,
    //     type: "POST",
    //     success: function (data) {
    //       alert(data);
    //     },
    //   });
      //  console.log(JSON.stringify(blob));
        console.log(stompClient);

    var res;
      var reader = new FileReader();
            reader.readAsDataURL(blob); 
            //async
        reader.onloadend = function() {
        var base64data = reader.result;                
        res = base64data.split(",")[1];
        stompClient.send("/app/uploadImg", {}, res);
        }     
        
    }


  

    var socket;
    var stompClient;
    
    function sockJsConnect() {
      socket = new SockJS(url + "/ImgReg");
      stompClient = Stomp.over(socket);
      stompClient.connect({}, (frame) => {
        stompClient.subscribe("/topic/greetingReceivers", (msg) => {
          console.log("server msg: " + msg.body);
        });

        stompClient.subscribe("/topic/ImgReceivers", (msg) => {
        //  console.log("server msg: " + msg.body);
        });

        stompClient.send(
          "/app/greeting",
          {},
          JSON.stringify({ name: "Tammie" })
        );
        stompClient.send("/app/greeting", {}, JSON.stringify({ name: "josh" }));
      });
    }
  </script>
</html>
    