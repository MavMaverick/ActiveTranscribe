# OpenTranscribe

OpenTranscribe is an Android application that uses Android Accessibility Services to extract captioning text from Google's Live Transcribe android app and stream it in real time to another device over a WebSocket.

By using this method, we can take full advantage of Google's Live Transcribe app's accuracy, speed, and ability to caption offline without needing to make cloud API calls.

OpenTranscribe is able to mimic the exact behavior of how Live Transcribe displays text on screen, knowing when new text is being generated, when it's finalized, and displaying the modifications that have been made to previous words.

Please note the GIFs haven't been lined up perfectly and I'll be uploading a better example:

![JSE2](https://github.com/user-attachments/assets/205ed57a-3b3c-43b3-acc7-f45af46656aa)

![JSE](https://github.com/user-attachments/assets/3b70d941-85bf-4e05-bc2e-a6b845ef43d4)


## Installation
1. Clone this repository.
   ```sh
   git clone https://github.com/MavMaverick/OpenTranscribe.git
