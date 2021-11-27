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
      <h2>Receiving data ye </h2>
      <br>
      <img id="photo" src="https://cdn.pixabay.com/photo/2013/07/13/11/44/penguin-158551__340.png" >

</br>
</br>
<button onclick='sockJsConnect();'> Connect to server</button>
</br>

  </body>
  <script>
    const url = " ";
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
            var element = document.getElementById("photo");
            element.setAttribute("src",'data:image/png;base64,' +msg.body)
        });

        stompClient.send(
          "/app/greeting",
          {},
          JSON.stringify({ name: "REEEEEEE" })
        );
      });
    }
  </script>
</html>
