function webcamCon() {
  var video = document.querySelector("video");
  console.log("HERE !!!!!!!");
  var contraints = {
    video: true,
    audio: false,
  };

  navigator.mediaDevices
    .getUserMedia(contraints)
    .then((stream) => {
      video.srcObject = stream;
      video.play();
    })
    .catch((err) => console.log("w1 error"));
}


