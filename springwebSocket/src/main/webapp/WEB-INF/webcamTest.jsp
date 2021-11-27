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

    <h2>Checking mediaDevices</h2>
    <br>
    <h2 id="text">? Unkown ?</h2>
    <br>
    <button onclick='test();'>TEST</button>
    <br>
    <br>


</body>
<script>

    function test() {
        var disp = document.getElementById("text")
        if ('mediaDevices' in navigator && 'getUserMedia' in navigator.mediaDevices) {
            console.log("Let's get this party started")
            disp.innerHTML = "Working!"
        }
        else {
            disp.innerHTML = "Not working :("
        }
    }

</script>

</html>
    